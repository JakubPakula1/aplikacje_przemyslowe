package com.github.jakubpakula1.lab.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TechCorpEmailValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TechCorpEmail {
    String message() default "Email musi posiadać domenę @techcorp.com";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

