package org.delcom.app.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RequestLoggingFilterTest {

    @Test
    void testDoFilterInternal() throws ServletException, IOException {
        RequestLoggingFilter filter = new RequestLoggingFilter();
        
        // Inject properti value karena tidak jalan via @Autowired di unit test murni
        ReflectionTestUtils.setField(filter, "port", 8080);
        ReflectionTestUtils.setField(filter, "livereload", false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        request.setMethod("GET");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // Test status 200 (Green)
        response.setStatus(200);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);

        // Test status 400 (Yellow)
        response.setStatus(400);
        filter.doFilterInternal(request, response, filterChain);

        // Test status 500 (Red)
        response.setStatus(500);
        filter.doFilterInternal(request, response, filterChain);

        // Test status 100 (Cyan - default else)
        response.setStatus(100);
        filter.doFilterInternal(request, response, filterChain);
    }
    
    @Test
    void testDoFilterInternal_WellKnown() throws ServletException, IOException {
        // Test exclude URL /.well-known
        RequestLoggingFilter filter = new RequestLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/.well-known/acme-challenge");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }
}