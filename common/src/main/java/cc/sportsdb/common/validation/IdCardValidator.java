package cc.sportsdb.common.validation;

import cc.sportsdb.common.util.RegexUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IdCardValidator implements ConstraintValidator<IsIdCard, String> {

    @Override
    public void initialize(IsIdCard idCard) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && RegexUtil.isMatch(value, RegexUtil.ID_CARD_18);
    }
}
