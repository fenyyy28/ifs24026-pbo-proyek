package org.delcom.app.controllers.api;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.dto.ChartData;
import org.delcom.app.entities.User;
import org.delcom.app.interceptors.AuthInterceptor;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.ItemService;
import org.delcom.app.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemApiController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ItemApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private AuthContext authContext;

    // Mock dependency untuk Interceptor & Security
    @MockBean
    private AuthInterceptor authInterceptor;
    @MockBean
    private AuthTokenService authTokenService;
    @MockBean
    private UserService userService;

    @Test
    void getStats_ShouldReturnSuccess_WhenDataExists() throws Exception {
        // 1. ARRANGE
        
        // A. Bypass AuthInterceptor
        given(authInterceptor.preHandle(any(), any(), any())).willReturn(true);

        // B. Mock User
        User mockUser = mock(User.class);
        given(authContext.getAuthUser()).willReturn(mockUser);

        // C. Buat Data Chart SECARA MANUAL (Bukan Mockito Mock)
        // Ini agar Jackson bisa mengubahnya menjadi JSON tanpa error
        ChartData data1 = new ChartData() {
            @Override public String getLabel() { return "Item A"; }
            @Override public Long getValue() { return 10L; }
        };

        ChartData data2 = new ChartData() {
            @Override public String getLabel() { return "Item B"; }
            @Override public Long getValue() { return 20L; }
        };

        List<ChartData> mockChartData = Arrays.asList(data1, data2);
        given(itemService.getChartData(mockUser)).willReturn(mockChartData);

        // 2. ACT & 3. ASSERT
        mockMvc.perform(get("/api/items/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Data chart berhasil diambil"))
                .andExpect(jsonPath("$.data[0].label").value("Item A"))
                .andExpect(jsonPath("$.data[0].value").value(10));

        verify(itemService).getChartData(mockUser);
    }
}