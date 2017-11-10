package cc.sportsdb.common.validation;

import cc.sportsdb.common.util.RegexUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UUIDValidator implements ConstraintValidator<IsUUID, String> {

    @Override
    public void initialize(IsUUID isUUID) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && RegexUtil.isMatch(value, RegexUtil.UUID);
    }
}
