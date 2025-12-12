package org.delcom.app.views;

import org.delcom.app.configs.AuthContext; // <--- JANGAN LUPA IMPORT INI
import org.delcom.app.configs.SecurityConfig;
import org.delcom.app.dto.LoginForm;
import org.delcom.app.dto.RegisterForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthView.class)
@Import(SecurityConfig.class)
class AuthViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthTokenService authTokenService;

    // === PERBAIKAN DI SINI ===
    // Tambahkan MockBean untuk AuthContext agar error "UnsatisfiedDependencyException" hilang
    @MockBean
    private AuthContext authContext;
    // =========================

    // --- LOGIN ---

    @Test
    @WithAnonymousUser
    void showLogin_Anonymous_Success() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN))
                .andExpect(model().attributeExists("loginForm"));
    }

    @Test
    @WithMockUser // Simulasi User Login via Spring Security
    void showLogin_LoggedIn_RedirectsHome() throws Exception {
        // NOTE: Jika Controller kamu mengecek `authContext.isAuthenticated()` untuk redirect,
        // kamu mungkin perlu menambahkan: when(authContext.isAuthenticated()).thenReturn(true);
        
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithAnonymousUser
    void postLogin_Success() throws Exception {
        User mockUser = new User("User", "test@mail.com", new BCryptPasswordEncoder().encode("pass"));
        when(userService.getUserByEmail("test@mail.com")).thenReturn(mockUser);

        mockMvc.perform(post("/auth/login/post")
                .with(csrf())
                .flashAttr("loginForm", new LoginForm() {{
                    setEmail("test@mail.com");
                    setPassword("pass");
                }}))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithAnonymousUser
    void postLogin_ValidationErrors() throws Exception {
        mockMvc.perform(post("/auth/login/post")
                .with(csrf())
                .flashAttr("loginForm", new LoginForm())) // Kosong -> Error
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN));
    }

    @Test
    @WithAnonymousUser
    void postLogin_UserNotFound() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(null);

        mockMvc.perform(post("/auth/login/post")
                .with(csrf())
                .flashAttr("loginForm", new LoginForm() {{
                    setEmail("unknown@mail.com");
                    setPassword("pass");
                }}))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("loginForm", "email"))
                .andExpect(view().name(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN));
    }

    @Test
    @WithAnonymousUser
    void postLogin_WrongPassword() throws Exception {
        User mockUser = new User("User", "test@mail.com", new BCryptPasswordEncoder().encode("pass"));
        when(userService.getUserByEmail("test@mail.com")).thenReturn(mockUser);

        mockMvc.perform(post("/auth/login/post")
                .with(csrf())
                .flashAttr("loginForm", new LoginForm() {{
                    setEmail("test@mail.com");
                    setPassword("wrongpass");
                }}))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("loginForm", "email"))
                .andExpect(view().name(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN));
    }

    // --- REGISTER ---

    @Test
    @WithAnonymousUser
    void showRegister_Anonymous_Success() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER));
    }
    
    @Test
    @WithMockUser
    void showRegister_LoggedIn_RedirectsHome() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithAnonymousUser
    void postRegister_Success() throws Exception {
        when(userService.getUserByEmail("new@mail.com")).thenReturn(null);
        when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(new User());

        mockMvc.perform(post("/auth/register/post")
                .with(csrf())
                .flashAttr("registerForm", new RegisterForm() {{
                    setName("New User");
                    setEmail("new@mail.com");
                    setPassword("pass");
                }}))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithAnonymousUser
    void postRegister_ValidationError() throws Exception {
        mockMvc.perform(post("/auth/register/post")
                .with(csrf())
                .flashAttr("registerForm", new RegisterForm())) // Kosong
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER));
    }

    @Test
    @WithAnonymousUser
    void postRegister_EmailExists() throws Exception {
        when(userService.getUserByEmail("exist@mail.com")).thenReturn(new User());

        mockMvc.perform(post("/auth/register/post")
                .with(csrf())
                .flashAttr("registerForm", new RegisterForm() {{
                    setName("User");
                    setEmail("exist@mail.com");
                    setPassword("pass");
                }}))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registerForm", "email"))
                .andExpect(view().name(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER));
    }

    @Test
    @WithAnonymousUser
    void postRegister_CreateUserFail() throws Exception {
        when(userService.getUserByEmail("fail@mail.com")).thenReturn(null);
        when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(null); // Simulasi DB Error

        mockMvc.perform(post("/auth/register/post")
                .with(csrf())
                .flashAttr("registerForm", new RegisterForm() {{
                    setName("User");
                    setEmail("fail@mail.com");
                    setPassword("pass");
                }}))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registerForm", "email"))
                .andExpect(view().name(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER));
    }

    @Test
    void logout() throws Exception {
        mockMvc.perform(get("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }
}