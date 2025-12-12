package org.delcom.app.services;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.repositories.AuthTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {

    @Mock
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    private AuthTokenService authTokenService;

    @Test
    void testFindUserToken() {
        UUID userId = UUID.randomUUID();
        String tokenStr = "token123";
        AuthToken expectedToken = new AuthToken(userId, tokenStr);

        when(authTokenRepository.findUserToken(userId, tokenStr)).thenReturn(expectedToken);

        AuthToken result = authTokenService.findUserToken(userId, tokenStr);

        assertEquals(expectedToken, result);
        verify(authTokenRepository).findUserToken(userId, tokenStr);
    }

    @Test
    void testCreateAuthToken() {
        AuthToken token = new AuthToken(UUID.randomUUID(), "token");
        when(authTokenRepository.save(token)).thenReturn(token);

        AuthToken result = authTokenService.createAuthToken(token);

        assertEquals(token, result);
        verify(authTokenRepository).save(token);
    }

    @Test
    void testDeleteAuthToken() {
        UUID userId = UUID.randomUUID();
        
        authTokenService.deleteAuthToken(userId);

        verify(authTokenRepository).deleteByUserId(userId);
    }
}