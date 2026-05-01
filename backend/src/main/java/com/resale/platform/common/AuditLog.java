package com.resale.platform.common;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    String action() default "";

    String module() default "";

    String description() default "";
}
