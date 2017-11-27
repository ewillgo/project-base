package cc.sportsdb.common.validation;

import cc.sportsdb.common.util.RegexUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<IsPhone, String> {

    @Override
    public void initialize(IsPhone phone) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && RegexUtil.isMatch(value, RegexUtil.PHONE);
    }
}
