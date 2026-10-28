package com.taller.auth.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @ParameterizedTest
    @ValueSource(strings = {"Ab1", "abcdefg1", "Abcdefgh"})
    void weakPassword_isRejected(String password) {
        RegisterRequest request = new RegisterRequest("carlos", password, "MECANICO");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void strongPassword_isAccepted() {
        RegisterRequest request = new RegisterRequest("carlos", "Pass123!", "MECANICO");

        assertTrue(validator.validate(request).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"MECANICO", "JEFE_TALLER", "ADMIN"})
    void wellFormedRole_isAccepted(String role) {
        RegisterRequest request = new RegisterRequest("carlos", "Pass123!", role);

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void nullRole_isRejected() {
        RegisterRequest request = new RegisterRequest("carlos", "Pass123!", null);

        assertFalse(validator.validate(request).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "mecanico", "JEFE TALLER", "MECANICO1"})
    void malformedRole_isRejected(String role) {
        RegisterRequest request = new RegisterRequest("carlos", "Pass123!", role);

        assertFalse(validator.validate(request).isEmpty());
    }
}
