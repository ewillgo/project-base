package cc.sportsdb.common.database.annotation;

import java.lang.annotation.*;

/**
 * Change datasource
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataSource {
    String value() default "";
}
