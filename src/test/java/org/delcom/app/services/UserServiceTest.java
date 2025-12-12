package org.delcom.app.services;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser() {
        User user = new User("John", "john@mail.com", "pass");
        // Gunakan any() agar fleksibel terhadap instance user yang dibuat di service
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser("John", "john@mail.com", "pass");

        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals("john@mail.com", result.getEmail());
    }

    @Test
    void testGetUserByEmail_Found() {
        String email = "test@mail.com";
        User user = new User("Test", email, "pass");
        
        when(userRepository.findFirstByEmail(email)).thenReturn(Optional.of(user));
        
        User result = userService.getUserByEmail(email);
        assertEquals(user, result);
    }

    @Test
    void testGetUserByEmail_NotFound() {
        String email = "unknown@mail.com";
        when(userRepository.findFirstByEmail(email)).thenReturn(Optional.empty());
        
        User result = userService.getUserByEmail(email);
        assertNull(result);
    }

    @Test
    void testGetUserById() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        // Case 1: Found
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        User result = userService.getUserById(id);
        assertEquals(id, result.getId());

        // Case 2: Not Found (Ini yang sebelumnya error)
        // Kita gunakan any(UUID.class) untuk menangani ID acak apa saja
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        
        // Pastikan pemanggilan di bawah ini menggunakan ID yang berbeda dari case 1, 
        // atau Mockito akan bingung. Karena any() menangkap segalanya,
        // urutan stubbing sangat penting atau pisahkan ke test method berbeda.
        // TAPI, cara paling aman adalah memisahkan test method. 
        // Untuk sekarang, kita panggil dengan ID baru:
        assertNull(userService.getUserById(UUID.randomUUID()));
    }

    @Test
    void testUpdateUser_Success() {
        UUID id = UUID.randomUUID();
        User existingUser = new User("Old", "old@mail.com", "pass");
        existingUser.setId(id);
        
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User updated = userService.updateUser(id, "New", "new@mail.com");
        
        assertNotNull(updated);
        // Pastikan logika di service Anda memang mengupdate objek existingUser
        assertEquals("New", updated.getName()); 
        assertEquals("new@mail.com", updated.getEmail());
    }

    @Test
    void testUpdateUser_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.updateUser(id, "New", "new@mail.com");
        assertNull(result);
    }

    @Test
    void testUpdatePassword_Success() {
        UUID id = UUID.randomUUID();
        User existingUser = new User("Name", "email", "oldPass");
        existingUser.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updatePassword(id, "newPass");
        
        assertNotNull(result);
        assertEquals("newPass", result.getPassword());
    }

    @Test
    void testUpdatePassword_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        
        User result = userService.updatePassword(id, "newPass");
        assertNull(result);
    }
}