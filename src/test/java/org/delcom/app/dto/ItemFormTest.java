package org.delcom.app.dto;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ItemFormTest {

    @Test
    void testSettersAndGetters() {
        // Setup data
        String name = "Laptop Gaming";
        String category = "Electronics";
        Integer quantity = 10;
        BigDecimal price = new BigDecimal("15000000");
        String storageLocation = "Warehouse A";
        String itemCondition = "New";
        String description = "High end laptop";
        
        // Mock MultipartFile karena itu interface
        MultipartFile mockImage = mock(MultipartFile.class);

        // Instantiate
        ItemForm form = new ItemForm();

        // Call Setters
        form.setName(name);
        form.setCategory(category);
        form.setQuantity(quantity);
        form.setPrice(price);
        form.setStorageLocation(storageLocation);
        form.setItemCondition(itemCondition);
        form.setDescription(description);
        form.setImage(mockImage);

        // Verify Getters return the same values
        assertEquals(name, form.getName());
        assertEquals(category, form.getCategory());
        assertEquals(quantity, form.getQuantity());
        assertEquals(price, form.getPrice());
        assertEquals(storageLocation, form.getStorageLocation());
        assertEquals(itemCondition, form.getItemCondition());
        assertEquals(description, form.getDescription());
        assertEquals(mockImage, form.getImage());
    }
}