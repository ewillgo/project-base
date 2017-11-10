package cc.sportsdb.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {UUIDValidator.class})
public @interface IsUUID {
    String message() default "Not a UUID";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
