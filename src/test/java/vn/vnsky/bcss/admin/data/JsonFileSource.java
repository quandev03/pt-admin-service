package vn.vnsky.bcss.admin.data;

import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(JsonArgumentsProvider.class)
public @interface JsonFileSource {

    String[] resources() default {};

    String[] files() default {};

    String encoding() default "UTF-8";

    TargetType targetType() default TargetType.BYTES;

    enum TargetType {
        BYTES, STRING, NODE
    }

}
