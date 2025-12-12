package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthTokenTest {

    @Test
    void testConstructorsAndGetters() {
        // Test Constructor Parameterized
        UUID userId = UUID.randomUUID();
        String tokenStr = "sample-token-123";
        
        AuthToken authToken = new AuthToken(userId, tokenStr);

        assertEquals(userId, authToken.getUserId());
        assertEquals(tokenStr, authToken.getToken());
        assertNotNull(authToken.getCreatedAt()); // CreatedAt diset di constructor
    }

    @Test
    void testNoArgConstructorAndSetters() {
        // Test Constructor Kosong
        AuthToken authToken = new AuthToken();
        
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "new-token";
        
        authToken.setId(id);
        authToken.setUserId(userId);
        authToken.setToken(token);

        assertEquals(id, authToken.getId());
        assertEquals(userId, authToken.getUserId());
        assertEquals(token, authToken.getToken());
    }

    @Test
    void testPrePersistLifecycle() throws Exception {
        // Karena method onCreate protected, kita gunakan Reflection atau asumsi package sama.
        // Di sini kita gunakan Reflection untuk memastikan bisa dijalankan dimanapun.
        AuthToken authToken = new AuthToken();
        
        // Pastikan null sebelum onCreate
        // (Note: field private, tidak bisa dicek langsung tanpa reflection, 
        // tapi kita bisa cek getter jika logic getter standar)
        
        Method onCreate = AuthToken.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(authToken);

        assertNotNull(authToken.getCreatedAt());
    }
}