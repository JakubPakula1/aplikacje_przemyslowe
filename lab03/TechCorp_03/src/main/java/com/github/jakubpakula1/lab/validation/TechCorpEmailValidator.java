package com.github.jakubpakula1.lab.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TechCorpEmailValidator implements ConstraintValidator<TechCorpEmail, String> {

    private static final String TECHCORP_DOMAIN = "@techcorp.com";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return value.toLowerCase().endsWith(TECHCORP_DOMAIN);
    }
}

