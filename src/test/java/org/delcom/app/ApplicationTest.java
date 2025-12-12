package org.delcom.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTest {

    @Test
    void contextLoads() {
        // Test ini memastikan bahwa seluruh konfigurasi Spring Boot valid
        // dan aplikasi bisa start tanpa error (Smoke Test).
        // Jika aplikasi gagal start (misal error config database), test ini akan gagal.
    }

    @Test
    void testMain() {
        // Memanggil method main secara eksplisit untuk mendapatkan coverage 
        // pada baris: SpringApplication.run(Application.class, args);
        Application.main(new String[]{});
    }
}