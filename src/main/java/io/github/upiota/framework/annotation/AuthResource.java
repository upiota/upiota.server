package io.github.upiota.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthResource {
	String name();
	AuthResourceType type() default AuthResourceType.URL;
	Authority authority();
	String parentCode() default "";
}
