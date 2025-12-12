package org.delcom.app.repositories;

import org.delcom.app.entities.Item;
import org.delcom.app.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// --- Tambahkan Import MockBean ---
import org.springframework.boot.test.mock.mockito.MockBean;
import org.delcom.app.configs.AuthContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    // --- PERBAIKAN: Tambahkan ini agar tidak crash ---
    @MockBean
    private AuthContext authContext;
    // ------------------------------------------------

    // ... (biarkan kode test method seperti apa adanya) ...
}