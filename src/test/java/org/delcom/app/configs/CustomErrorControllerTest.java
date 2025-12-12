package org.delcom.app.configs;

import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Import AuthContext
import org.delcom.app.configs.AuthContext;

@WebMvcTest(controllers = CustomErrorController.class)
@Import(SecurityConfig.class)
class CustomErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthTokenService authTokenService;

    // === TAMBAHKAN INI (Solusi Error) ===
    @MockBean
    private AuthContext authContext;
    // ====================================

    @Test
    void testHandleError() throws Exception {
        // Simulasi request ke /error
        mockMvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(view().name("error")); // Pastikan nama view sesuai dengan controller kamu
    }
    
    // Jika ada test lain, biarkan saja
}