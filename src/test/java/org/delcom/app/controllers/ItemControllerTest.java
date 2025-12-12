package org.delcom.app.controllers;

import org.delcom.app.dto.ChartData;
import org.delcom.app.dto.ItemForm;
import org.delcom.app.entities.Item;
import org.delcom.app.entities.User;
import org.delcom.app.services.ItemService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@ContextConfiguration(classes = {ItemController.class})
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private User mockUser;
    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", UUID.randomUUID());
        authentication = new UsernamePasswordAuthenticationToken(mockUser, null, Collections.emptyList());
    }

    // ==========================================
    // 1. HAPPY PATH & VALIDATION (Authenticated)
    // ==========================================

    @Test
    @WithMockUser
    void list_ShouldReturnIndexPage() throws Exception {
        given(itemService.getAllItems(any(User.class))).willReturn(List.of(new Item()));

        mockMvc.perform(get("/items")
                .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_ITEMS_INDEX))
                .andExpect(model().attributeExists("items"));
    }

    @Test
    @WithMockUser
    void createForm_ShouldReturnFormPage() throws Exception {
        mockMvc.perform(get("/items/create")
                .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_ITEMS_FORM))
                .andExpect(model().attributeExists("itemForm"))
                .andExpect(model().attribute("isEdit", false));
    }

    @Test
    @WithMockUser
    void save_Success_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/items/save")
                .with(csrf())
                .with(authentication(authentication))
                .param("name", "New Item")
                .param("category", "Electronics")
                .param("price", "1000")
                .param("quantity", "10")
                .param("description", "Desc")
                .param("storageLocation", "Warehouse A")
                .param("itemCondition", "Baru"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"))
                .andExpect(flash().attributeExists("success"));

        verify(itemService).saveItem(any(ItemForm.class), any(User.class), isNull());
    }

    @Test
    @WithMockUser
    void save_ValidationError_ShouldReturnForm() throws Exception {
        mockMvc.perform(post("/items/save")
                .with(csrf())
                .with(authentication(authentication))
                .param("name", "") // Invalid
                .param("price", "1000"))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_ITEMS_FORM))
                .andExpect(model().attributeHasFieldErrors("itemForm", "name"));
    }

    @Test
    @WithMockUser
    void save_Exception_ShouldReturnFormWithError() throws Exception {
        doThrow(new RuntimeException("Database Error"))
                .when(itemService).saveItem(any(), any(), any());

        mockMvc.perform(post("/items/save")
                .with(csrf())
                .with(authentication(authentication))
                .param("name", "Valid Item")
                .param("category", "Cat")
                .param("price", "1000")
                .param("quantity", "1")
                .param("storageLocation", "B")
                .param("itemCondition", "Good"))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_ITEMS_FORM))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser
    void editForm_Success() throws Exception {
        UUID itemId = UUID.randomUUID();
        Item item = new Item();
        ReflectionTestUtils.setField(item, "id", itemId);
        item.setName("Old");
        
        given(itemService.getItem(eq(itemId), any(User.class))).willReturn(item);

        mockMvc.perform(get("/items/edit/" + itemId)
                .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_ITEMS_FORM))
                .andExpect(model().attribute("isEdit", true));
    }

    @Test
    @WithMockUser
    void editForm_NotFound_ShouldRedirect() throws Exception {
        UUID itemId = UUID.randomUUID();
        given(itemService.getItem(eq(itemId), any(User.class))).willReturn(null);

        mockMvc.perform(get("/items/edit/" + itemId)
                .with(authentication(authentication)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));
    }

    @Test
    @WithMockUser
    void update_Success() throws Exception {
        UUID itemId = UUID.randomUUID();

        mockMvc.perform(post("/items/update/" + itemId)
                .with(csrf())
                .with(authentication(authentication))
                .param("name", "Updated")
                .param("category", "Cat")
                .param("price", "2000")
                .param("quantity", "5")
                .param("storageLocation", "C")
                .param("itemCondition", "Baik"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));
        
        verify(itemService).saveItem(any(ItemForm.class), any(User.class), eq(itemId));
    }

    @Test
    @WithMockUser
    void update_ValidationError() throws Exception {
        UUID itemId = UUID.randomUUID();
        mockMvc.perform(post("/items/update/" + itemId)
                .with(csrf())
                .with(authentication(authentication))
                .param("name", "")) // Invalid
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_ITEMS_FORM));
    }

    @Test
    @WithMockUser
    void update_Exception() throws Exception {
        UUID itemId = UUID.randomUUID();
        doThrow(new RuntimeException("Update Failed"))
                .when(itemService).saveItem(any(), any(), any());

        mockMvc.perform(post("/items/update/" + itemId)
                .with(csrf())
                .with(authentication(authentication))
                .param("name", "Valid")
                .param("price", "500")
                .param("quantity", "1")
                .param("category", "E")
                .param("storageLocation", "D")
                .param("itemCondition", "Baru"))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_ITEMS_FORM))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser
    void delete_Success() throws Exception {
        UUID itemId = UUID.randomUUID();
        mockMvc.perform(post("/items/delete/" + itemId)
                .with(csrf())
                .with(authentication(authentication)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"))
                .andExpect(flash().attributeExists("success"));

        verify(itemService).deleteItem(eq(itemId), any(User.class));
    }

    @Test
    @WithMockUser
    void detail_Success() throws Exception {
        UUID itemId = UUID.randomUUID();
        Item item = new Item();
        given(itemService.getItem(eq(itemId), any(User.class))).willReturn(item);

        mockMvc.perform(get("/items/detail/" + itemId)
                .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_ITEMS_DETAIL));
    }

    @Test
    @WithMockUser
    void detail_NotFound() throws Exception {
        UUID itemId = UUID.randomUUID();
        given(itemService.getItem(eq(itemId), any(User.class))).willReturn(null);

        mockMvc.perform(get("/items/detail/" + itemId)
                .with(authentication(authentication)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));
    }

    @Test
    @WithMockUser
    void chartPage_Success() throws Exception {
        ChartData data = mock(ChartData.class);
        when(data.getLabel()).thenReturn("Test");
        when(data.getValue()).thenReturn(100L);
        given(itemService.getChartData(any(User.class))).willReturn(List.of(data));

        mockMvc.perform(get("/items/chart")
                .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_ITEMS_CHART));
    }

    // ==========================================
    // 2. EDGE CASE: Wrong Principal Type
    // ==========================================

    @Test
    @WithMockUser
    void whenPrincipalIsNotUserEntity_ShouldRedirect() throws Exception {
        // Auth exists, but Principal is String ("anonymousUser")
        UsernamePasswordAuthenticationToken anonAuth = 
            new UsernamePasswordAuthenticationToken("anonymousUser", null, Collections.emptyList());

        mockMvc.perform(get("/items").with(authentication(anonAuth)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
        
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/items/delete/" + id).with(csrf()).with(authentication(anonAuth)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));
        verify(itemService, never()).deleteItem(any(), any());
    }

    // ==========================================
    // 3. CRITICAL: AUTH IS NULL (Bypassing Filter)
    // ==========================================

    @Test
    void whenAuthIsNull_ShouldRedirectToLogin() throws Exception {
        // Menggunakan StandaloneSetup untuk melewati Spring Security Filters sepenuhnya.
        // Ini menjamin request masuk ke Controller dengan SecurityContext kosong.
        MockMvc standaloneMvc = MockMvcBuilders
                .standaloneSetup(new ItemController(itemService))
                .build();
        
        SecurityContextHolder.clearContext(); // Pastikan konteks bersih

        // 1. List
        standaloneMvc.perform(get("/items"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        // 2. Save
        standaloneMvc.perform(post("/items/save")
                .param("name", "X").param("price", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        // 3. Edit
        standaloneMvc.perform(get("/items/edit/" + UUID.randomUUID()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        // 4. Update
        standaloneMvc.perform(post("/items/update/" + UUID.randomUUID())
                .param("name", "X").param("price", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        // 5. Detail
        standaloneMvc.perform(get("/items/detail/" + UUID.randomUUID()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        // 6. Chart
        standaloneMvc.perform(get("/items/chart"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        // 7. Delete (Spesifik: redirect ke items)
        standaloneMvc.perform(post("/items/delete/" + UUID.randomUUID()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));

        // Verifikasi bahwa service tidak dipanggil
        verify(itemService, never()).deleteItem(any(), any());
    }
}