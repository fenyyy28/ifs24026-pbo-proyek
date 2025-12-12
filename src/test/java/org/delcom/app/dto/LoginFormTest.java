package org.delcom.app.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginFormTest {

    @Test
    void testLoginForm() {
        // Instantiate
        LoginForm loginForm = new LoginForm();

        // Setup data
        String email = "test@example.com";
        String password = "securepassword";
        boolean rememberMe = true;

        // Set Values
        loginForm.setEmail(email);
        loginForm.setPassword(password);
        loginForm.setRememberMe(rememberMe);

        // Assert Getters
        assertEquals(email, loginForm.getEmail());
        assertEquals(password, loginForm.getPassword());
        assertTrue(loginForm.isRememberMe()); // Cek boolean getter

        // Cek false condition untuk boolean
        loginForm.setRememberMe(false);
        assertFalse(loginForm.isRememberMe());
    }
}