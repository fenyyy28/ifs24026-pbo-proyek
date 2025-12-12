package org.delcom.app.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private AuthContext authContext;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    // Untuk Mocking Static Method (JwtUtil)
    private MockedStatic<JwtUtil> jwtUtilMockedStatic;

    @BeforeEach
    void setUp() {
        jwtUtilMockedStatic = mockStatic(JwtUtil.class);
    }

    @AfterEach
    void tearDown() {
        jwtUtilMockedStatic.close();
    }

    // --- Helper untuk Mock Response Writer ---
    private StringWriter mockResponseWriter() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        return stringWriter;
    }

    @Test
    void testPreHandle_PublicEndpoint_ReturnsTrue() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        boolean result = authInterceptor.preHandle(request, response, new Object());
        assertTrue(result);
    }

    @Test
    void testPreHandle_ErrorEndpoint_ReturnsTrue() throws Exception {
        when(request.getRequestURI()).thenReturn("/error");
        boolean result = authInterceptor.preHandle(request, response, new Object());
        assertTrue(result);
    }

    @Test
    void testPreHandle_NoToken_ReturnsFalse() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn(null);
        mockResponseWriter();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(401);
    }

    @Test
    void testPreHandle_InvalidTokenFormat_ReturnsFalse() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Basic xyz123");
        mockResponseWriter();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(401);
    }

    // --- TEST BARU UNTUK COVERAGE 100% ---
    @Test
    void testPreHandle_TokenIsEmpty_ReturnsFalse() throws Exception {
        // Scenario: Header ada "Bearer " tapi kosong isinya
        // extractToken akan return "" (empty string), bukan null
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer "); 
        mockResponseWriter();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        // Pesan error tetap sama, tapi ini memicu cabang logic 'token.isEmpty()'
        verify(response).setStatus(401); 
    }
    // -------------------------------------

    @Test
    void testPreHandle_JwtInvalid_ReturnsFalse() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
        
        jwtUtilMockedStatic.when(() -> JwtUtil.validateToken(anyString(), anyBoolean())).thenReturn(false);
        mockResponseWriter();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(401);
    }

    @Test
    void testPreHandle_UserIdExtractionNull_ReturnsFalse() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        String token = "valid.token.nodata";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        jwtUtilMockedStatic.when(() -> JwtUtil.validateToken(token, true)).thenReturn(true);
        jwtUtilMockedStatic.when(() -> JwtUtil.extractUserId(token)).thenReturn(null);
        
        mockResponseWriter();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(401);
    }

    @Test
    void testPreHandle_TokenNotFoundInDB_ReturnsFalse() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        String token = "valid.token";
        UUID userId = UUID.randomUUID();
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        jwtUtilMockedStatic.when(() -> JwtUtil.validateToken(token, true)).thenReturn(true);
        jwtUtilMockedStatic.when(() -> JwtUtil.extractUserId(token)).thenReturn(userId);

        when(authTokenService.findUserToken(userId, token)).thenReturn(null);
        
        mockResponseWriter();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(401);
    }

    @Test
    void testPreHandle_UserNotFound_ReturnsFalse() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        String token = "valid.token";
        UUID userId = UUID.randomUUID();
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        jwtUtilMockedStatic.when(() -> JwtUtil.validateToken(token, true)).thenReturn(true);
        jwtUtilMockedStatic.when(() -> JwtUtil.extractUserId(token)).thenReturn(userId);

        AuthToken mockAuthToken = new AuthToken();
        mockAuthToken.setUserId(userId);
        when(authTokenService.findUserToken(userId, token)).thenReturn(mockAuthToken);

        when(userService.getUserById(userId)).thenReturn(null);
        
        mockResponseWriter();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(404);
    }

    @Test
    void testPreHandle_Success() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        String token = "valid.token.good";
        UUID userId = UUID.randomUUID();
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        jwtUtilMockedStatic.when(() -> JwtUtil.validateToken(token, true)).thenReturn(true);
        jwtUtilMockedStatic.when(() -> JwtUtil.extractUserId(token)).thenReturn(userId);

        AuthToken mockAuthToken = new AuthToken();
        mockAuthToken.setUserId(userId);
        when(authTokenService.findUserToken(userId, token)).thenReturn(mockAuthToken);

        User mockUser = new User();
        mockUser.setId(userId);
        when(userService.getUserById(userId)).thenReturn(mockUser);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(authContext).setAuthUser(mockUser);
    }
}