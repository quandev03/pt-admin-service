package vn.vnsky.bcss.admin.annotation;

import org.hibernate.annotations.IdGeneratorType;
import vn.vnsky.bcss.admin.util.UlidSequenceGenerator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@IdGeneratorType(UlidSequenceGenerator.class)
@Retention(RUNTIME)
@Target({METHOD, FIELD})
public @interface UlidSequence {
}
