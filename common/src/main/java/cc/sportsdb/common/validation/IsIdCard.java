package cc.sportsdb.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {IdCardValidator.class})
public @interface IsIdCard {
    String message() default "Not a id card number.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
