package org.delcom.app.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisterFormTest {

    @Test
    void testRegisterForm() {
        // Instantiate
        RegisterForm registerForm = new RegisterForm();

        // Setup data
        String name = "John Doe";
        String email = "john@example.com";
        String password = "password123";

        // Set Values
        registerForm.setName(name);
        registerForm.setEmail(email);
        registerForm.setPassword(password);

        // Assert Getters
        assertEquals(name, registerForm.getName());
        assertEquals(email, registerForm.getEmail());
        assertEquals(password, registerForm.getPassword());
    }
}