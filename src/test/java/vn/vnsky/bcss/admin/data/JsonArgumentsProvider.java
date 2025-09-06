package vn.vnsky.bcss.admin.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.AnnotationBasedArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class JsonArgumentsProvider extends AnnotationBasedArgumentsProvider<JsonFileSource> {

    private final InputStreamProvider inputStreamProvider;

    private final ObjectMapper objectMapper;

    JsonArgumentsProvider() {
        this(DefaultInputStreamProvider.INSTANCE, new ObjectMapper());
    }

    JsonArgumentsProvider(InputStreamProvider inputStreamProvider, ObjectMapper objectMapper) {
        this.inputStreamProvider = inputStreamProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    protected Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext, JsonFileSource jsonFileSource) {
        Stream<String> pathStream = Arrays.stream(jsonFileSource.resources());
        Stream<Source> resources = pathStream.map(this.inputStreamProvider::classpathResource);
        pathStream = Arrays.stream(jsonFileSource.files());
        Stream<Source> files = pathStream.map(this.inputStreamProvider::file);
        List<Source> sources = Stream.concat(resources, files).toList();
        return (Preconditions.notEmpty(sources, "Resources or files must not be empty"))
                .stream()
                .map(source -> source.open(extensionContext))
                .map(inputStream -> {
                    try {
                        return this.objectMapper.readTree(inputStream);
                    } catch (IOException ex) {
                        throw new JsonParsingException("Failed to parse json resource/file", ex);
                    }
                })
                .flatMap(jsonNode -> {
                    if (jsonNode.isArray()) {
                        ArrayNode arrayNode = (ArrayNode) jsonNode;
                        Iterator<JsonNode> elementNodeIterator = arrayNode.elements();
                        return Stream.generate(() -> null)
                                .takeWhile(x -> elementNodeIterator.hasNext())
                                .map(e -> elementNodeIterator.next());
                    } else {
                        return Stream.of(jsonNode);
                    }
                })
                .map(jsonNode -> {
                    switch (jsonFileSource.targetType()) {
                        case BYTES -> {
                            return () -> {
                                try {
                                    return new Object[] {this.objectMapper.writeValueAsBytes(jsonNode)};
                                } catch (JsonProcessingException ex) {
                                    throw new JsonParsingException("Failed to write json node", ex);
                                }
                            };
                        }
                        case STRING -> {
                            try {
                                return Arguments.of(this.objectMapper.writeValueAsString(jsonNode));
                            } catch (JsonProcessingException ex) {
                                throw new JsonParsingException("Failed to write json node", ex);
                            }
                        }
                        default -> {
                            return Arguments.of(jsonNode);
                        }
                    }
                })
                ;
    }

    @FunctionalInterface
    private interface Source {
        InputStream open(ExtensionContext var1);
    }

    interface InputStreamProvider {
        InputStream openClasspathResource(Class<?> var1, String var2);

        InputStream openFile(String var1);

        default Source classpathResource(String path) {
            return context -> this.openClasspathResource(context.getRequiredTestClass(), path);
        }

        default Source file(String path) {
            return context -> this.openFile(path);
        }
    }

    private static class DefaultInputStreamProvider implements InputStreamProvider {
        private static final DefaultInputStreamProvider INSTANCE = new DefaultInputStreamProvider();

        private DefaultInputStreamProvider() {
        }

        public InputStream openClasspathResource(Class<?> baseClass, String path) {
            Preconditions.notBlank(path, () -> "Classpath resource [" + path + "] must not be null or blank");
            InputStream inputStream = baseClass.getResourceAsStream(path);
            return Preconditions.notNull(inputStream, () -> "Classpath resource [" + path + "] does not exist");
        }

        public InputStream openFile(String path) {
            Preconditions.notBlank(path, () -> "File [" + path + "] must not be null or blank");

            try {
                return Files.newInputStream(Paths.get(path));
            } catch (IOException e) {
                throw new JUnitException("File [" + path + "] could not be read", e);
            }
        }
    }

}
