package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WebMvcConfigTest {

    @Mock
    private AuthInterceptor authInterceptor;

    @InjectMocks
    private WebMvcConfig webMvcConfig;

    @Test
    void testAddInterceptors() {
        MockitoAnnotations.openMocks(this);

        InterceptorRegistry registry = mock(InterceptorRegistry.class);
        InterceptorRegistration registration = mock(InterceptorRegistration.class);

        when(registry.addInterceptor(any())).thenReturn(registration);
        when(registration.addPathPatterns(anyString())).thenReturn(registration);
        when(registration.excludePathPatterns(anyString())).thenReturn(registration);

        webMvcConfig.addInterceptors(registry);

        verify(registry).addInterceptor(authInterceptor);
        verify(registration).addPathPatterns("/api/**");
    }

    @Test
    void testAddResourceHandlers() {
        MockitoAnnotations.openMocks(this);
        // Set properti uploadDir
        ReflectionTestUtils.setField(webMvcConfig, "uploadDir", "uploads");

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);

        when(registry.addResourceHandler(anyString())).thenReturn(registration);
        when(registration.addResourceLocations(anyString())).thenReturn(registration);

        webMvcConfig.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/uploads/**");
        // Verify path location logic calls
        verify(registration).addResourceLocations(contains("file:///"));
    }
}