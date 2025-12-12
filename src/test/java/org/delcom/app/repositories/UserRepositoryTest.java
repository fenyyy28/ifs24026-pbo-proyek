package org.delcom.app.repositories;

import org.delcom.app.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// --- Tambahkan Import ---
import org.springframework.boot.test.mock.mockito.MockBean;
import org.delcom.app.configs.AuthContext;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // --- PERBAIKAN: Tambahkan ini ---
    @MockBean
    private AuthContext authContext;
    // --------------------------------

    // ... (biarkan kode test method apa adanya) ...
}