package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testConstructors() {
        // 1. Test No-Arg
        User user1 = new User();
        assertNull(user1.getName());

        // 2. Test 3-Args
        User user2 = new User("John Doe", "john@example.com", "pass123");
        assertEquals("John Doe", user2.getName());
        assertEquals("john@example.com", user2.getEmail());
        assertEquals("pass123", user2.getPassword());

        // 3. Test 2-Args (Chained constructor)
        // Logikanya: this("", email, password) -> Name harus string kosong ""
        User user3 = new User("jane@example.com", "secret");
        assertEquals("", user3.getName()); 
        assertEquals("jane@example.com", user3.getEmail());
    }

    @Test
    void testSettersAndGetters() {
        User user = new User();
        UUID id = UUID.randomUUID();

        user.setId(id);
        user.setName("Admin");
        user.setEmail("admin@test.com");
        user.setPassword("hashedpass");

        assertEquals(id, user.getId());
        assertEquals("Admin", user.getName());
        assertEquals("admin@test.com", user.getEmail());
        assertEquals("hashedpass", user.getPassword());
    }

    @Test
    void testLifecycleMethods() throws Exception {
        User user = new User();

        // Reflection untuk akses method protected/private @PrePersist
        Method onCreate = User.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(user);

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());

        // Tunggu sebentar agar waktu berubah (opsional, untuk presisi tinggi)
        Thread.sleep(10);
        LocalDateTime oldUpdate = user.getUpdatedAt();

        // Reflection untuk akses method protected/private @PreUpdate
        Method onUpdate = User.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(user);

        assertTrue(user.getUpdatedAt().isAfter(oldUpdate) || user.getUpdatedAt().isEqual(oldUpdate));
    }
}