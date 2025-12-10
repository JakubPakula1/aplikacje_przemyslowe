package com.github.jakubpakula1.lab.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test custom walidatora TechCorpEmail
 */
class TechCorpEmailValidatorTest {

    private final TechCorpEmailValidator validator = new TechCorpEmailValidator();

    @Test
    @DisplayName("TechCorpEmailValidator - poprawny email z @techcorp.com")
    void testValidTechCorpEmail() {
        assertTrue(validator.isValid("john@techcorp.com", null));
        assertTrue(validator.isValid("jane.doe@techcorp.com", null));
        assertTrue(validator.isValid("test.user@techcorp.com", null));
    }

    @Test
    @DisplayName("TechCorpEmailValidator - email z inną domeną")
    void testInvalidDomain() {
        assertFalse(validator.isValid("john@example.com", null));
        assertFalse(validator.isValid("jane@gmail.com", null));
        assertFalse(validator.isValid("test@company.com", null));
    }

    @Test
    @DisplayName("TechCorpEmailValidator - null email (powinien zwrócić true)")
    void testNullEmail() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    @DisplayName("TechCorpEmailValidator - puste pole")
    void testEmptyEmail() {
        assertFalse(validator.isValid("", null));
    }

    @Test
    @DisplayName("TechCorpEmailValidator - case insensitive")
    void testCaseInsensitive() {
        assertTrue(validator.isValid("JOHN@TECHCORP.COM", null));
        assertTrue(validator.isValid("Jane@TechCorp.COM", null));
    }
}

