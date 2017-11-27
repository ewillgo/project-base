package cc.sportsdb.common.validation;

import cc.sportsdb.common.util.RegexUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UrlValidator implements ConstraintValidator<IsURL, String> {

    @Override
    public void initialize(IsURL url) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && RegexUtil.isMatch(value, RegexUtil.URL);
    }
}
