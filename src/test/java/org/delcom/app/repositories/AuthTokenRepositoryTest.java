package org.delcom.app.repositories;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
// --- Tambahkan Import ---
import org.springframework.boot.test.mock.mockito.MockBean;
import org.delcom.app.configs.AuthContext;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuthTokenRepositoryTest {

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    // --- PERBAIKAN: Tambahkan ini ---
    @MockBean
    private AuthContext authContext;
    // --------------------------------

    // ... (biarkan kode test method apa adanya) ...
}