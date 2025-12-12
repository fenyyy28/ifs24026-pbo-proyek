package org.delcom.app.views;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.configs.SecurityConfig;
import org.delcom.app.entities.Item;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.ItemService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeView.class)
@Import(SecurityConfig.class)
class HomeViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private AuthTokenService authTokenService; 

    @MockBean
    private AuthContext authContext; // Wajib ada agar ApplicationContext tidak crash

    @Test
    @WithAnonymousUser
    void home_Anonymous_RedirectsToLogin() throws Exception {
        // PERBAIKAN: Gunakan exact match '/auth/login' agar test tidak fail
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void home_LoggedIn_ReturnsCalculatedStats() throws Exception {
        User mockUser = new User("Admin", "admin@mail.com", "pass");
        mockUser.setId(UUID.randomUUID());

        // Setup Mock Service
        when(userService.getUserById(any())).thenReturn(mockUser);

        Item item1 = new Item();
        item1.setCategory("Elektronik"); item1.setQuantity(10); item1.setPrice(new BigDecimal("1000"));
        Item item2 = new Item();
        item2.setCategory("Elektronik"); item2.setQuantity(5); item2.setPrice(new BigDecimal("2000"));
        
        when(itemService.getAllItems(mockUser)).thenReturn(List.of(item1, item2));

        // Setup Security Context dengan AUTHORITY agar tidak kena 403 Forbidden
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                mockUser, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) 
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_PAGES_HOME))
                .andExpect(model().attribute("totalItemTypes", 2))
                .andExpect(model().attribute("totalQuantity", 15))
                .andExpect(model().attribute("totalAssetValue", new BigDecimal("20000")));
    }
    
    @Test
    void home_LoggedIn_NullItems_HandlesGracefully() throws Exception {
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        
        when(userService.getUserById(any())).thenReturn(mockUser);
        when(itemService.getAllItems(mockUser)).thenReturn(null);

        // Setup Security Context dengan AUTHORITY
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                mockUser, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalQuantity", 0));
    }
}