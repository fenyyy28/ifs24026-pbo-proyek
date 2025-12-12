package org.delcom.app.configs;

import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

// --- 1. Tambahkan Import ---
import org.delcom.app.configs.AuthContext; 

@WebMvcTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthTokenService authTokenService;

    // --- 2. Tambahkan MockBean ini ---
    @MockBean
    private AuthContext authContext;
    // --------------------------------

    // ... (kode test di bawahnya biarkan saja) ...
}