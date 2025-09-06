package vn.vnsky.bcss.admin.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import vn.vnsky.bcss.admin.dto.AppDTO;

import java.io.IOException;

@EqualsAndHashCode(callSuper = true)
public class AppJsonDeserializer extends JsonDeserializer<AppDTO> {

    @Override
    public AppDTO deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode jsonNode = mapper.readTree(jsonParser);
        return AppDTO.builder()
                .id(jsonNode.get(AppDTO.Fields.id).asText())
                .code(jsonNode.get(AppDTO.Fields.code).asText())
                .name(jsonNode.get(AppDTO.Fields.name).asText())
                .ssoProvider(jsonNode.get(AppDTO.Fields.ssoProvider) != null ? jsonNode.get(AppDTO.Fields.ssoProvider).asText() : null)
                .build();
    }

}
