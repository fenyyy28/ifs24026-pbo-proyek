package org.delcom.app.configs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testApiResponse() {
        String status = "success";
        String message = "Operation successful";
        String data = "Sample Data";

        ApiResponse<String> response = new ApiResponse<>(status, message, data);

        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void testApiResponseWithNullData() {
        ApiResponse<Object> response = new ApiResponse<>("error", "Failed", null);

        assertEquals("error", response.getStatus());
        assertEquals("Failed", response.getMessage());
        assertNull(response.getData());
    }
}