package org.delcom.app.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChartDataTest {

    @Test
    void testChartDataInterface() {
        // Membuat implementasi anonim dari interface untuk testing
        String expectedLabel = "Electronics";
        Long expectedValue = 150L;

        ChartData data = new ChartData() {
            @Override
            public String getLabel() {
                return expectedLabel;
            }

            @Override
            public Long getValue() {
                return expectedValue;
            }
        };

        // Verifikasi bahwa method interface bisa dipanggil dan mengembalikan nilai
        assertEquals(expectedLabel, data.getLabel());
        assertEquals(expectedValue, data.getValue());
    }
}