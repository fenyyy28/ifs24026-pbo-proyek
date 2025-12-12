package org.delcom.app.configs;

import org.delcom.app.entities.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AuthContextTest {

    @Test
    void testAuthContext() {
        AuthContext context = new AuthContext();

        // Awalnya harus kosong
        assertNull(context.getAuthUser());
        assertFalse(context.isAuthenticated());

        // Set User
        User mockUser = mock(User.class);
        context.setAuthUser(mockUser);

        // Verifikasi setelah set
        assertEquals(mockUser, context.getAuthUser());
        assertTrue(context.isAuthenticated());
        
        // Set null lagi
        context.setAuthUser(null);
        assertFalse(context.isAuthenticated());
    }
}