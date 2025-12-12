package org.delcom.app.utils;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void testConstructor() {
        // Coverage untuk constructor implicit
        JwtUtil jwtUtil = new JwtUtil();
        assertNotNull(jwtUtil);
        assertNotNull(JwtUtil.getKey());
    }

    @Test
    void testGenerateAndExtractToken_Success() {
        UUID userId = UUID.randomUUID();
        
        // 1. Generate
        String token = JwtUtil.generateToken(userId);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // 2. Extract
        UUID extractedId = JwtUtil.extractUserId(token);
        assertEquals(userId, extractedId);

        // 3. Validate Normal
        assertTrue(JwtUtil.validateToken(token, false));
    }

    @Test
    void testExtractUserId_InvalidToken() {
        // Test parsing token sampah
        String invalidToken = "invalid.token.garbage";
        UUID result = JwtUtil.extractUserId(invalidToken);
        
        // Harus return null (masuk catch Exception)
        assertNull(result);
    }

    @Test
    void testValidateToken_InvalidSignature() {
        // Token format JWT tapi signature salah/garbage
        String garbageToken = "eyJhbGciOiJIUzI1NiJ9.e30.garbageSignature";
        
        boolean isValid = JwtUtil.validateToken(garbageToken, false);
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_Expired() {
        UUID userId = UUID.randomUUID();

        // KITA HARUS MEMBUAT TOKEN YANG EXPIRED SECARA MANUAL
        // Agar signature-nya valid (diterima parser), tapi tanggalnya kadaluwarsa.
        String expiredToken = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date(System.currentTimeMillis() - 100000)) // Dibuat masa lalu
                .expiration(new Date(System.currentTimeMillis() - 1000)) // Expired 1 detik lalu
                .signWith(JwtUtil.getKey()) // Sign pakai key asli app
                .compact();

        // Case 1: ignoreExpired = false
        // Harusnya masuk catch(ExpiredJwtException) -> return false
        boolean isValidStrict = JwtUtil.validateToken(expiredToken, false);
        assertFalse(isValidStrict, "Token expired harusnya invalid jika strict");

        // Case 2: ignoreExpired = true
        // Harusnya masuk catch(ExpiredJwtException) -> return true
        boolean isValidLax = JwtUtil.validateToken(expiredToken, true);
        assertTrue(isValidLax, "Token expired harusnya valid jika ignoreExpired=true");
    }
    
    @Test
    void testValidateToken_Malformed() {
        // Test input null atau kosong
        assertFalse(JwtUtil.validateToken(null, false));
        assertFalse(JwtUtil.validateToken("", false));
    }
}