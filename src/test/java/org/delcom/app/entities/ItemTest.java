package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void testGettersAndSetters() throws NoSuchFieldException, IllegalAccessException {
        Item item = new Item();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("10000.00");

        item.setId(id);
        item.setUserId(userId);
        item.setName("Laptop");
        item.setCategory("Electronics");
        item.setQuantity(5);
        item.setPrice(price);
        item.setStorageLocation("Shelf A");
        item.setItemCondition("New");
        item.setDescription("Gaming Laptop");
        item.setImageUrl("http://img.com/laptop.jpg");

        assertEquals(id, item.getId());
        assertEquals(userId, item.getUserId());
        assertEquals("Laptop", item.getName());
        assertEquals("Electronics", item.getCategory());
        assertEquals(5, item.getQuantity());
        assertEquals(price, item.getPrice());
        assertEquals("Shelf A", item.getStorageLocation());
        assertEquals("New", item.getItemCondition());
        assertEquals("Gaming Laptop", item.getDescription());
        assertEquals("http://img.com/laptop.jpg", item.getImageUrl());

        // Test User relation getter (Read-only field via Hibernate usually, but test getter)
        // Kita inject manual via Reflection karena tidak ada setter public untuk User object
        User mockUser = new User();
        Field userField = Item.class.getDeclaredField("user");
        userField.setAccessible(true);
        userField.set(item, mockUser);
        
        assertEquals(mockUser, item.getUser());
    }

    @Test
    void testPrePersist_DefaultCondition() throws Exception {
        // Case 1: itemCondition NULL -> harus jadi "Baik"
        Item item = new Item();
        item.setItemCondition(null);

        Method onCreate = Item.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(item);

        assertNotNull(item.getCreatedAt());
        assertNotNull(item.getUpdatedAt());
        assertEquals("Baik", item.getItemCondition(), "Default condition should be set to 'Baik'");
    }

    @Test
    void testPrePersist_ExistingCondition() throws Exception {
        // Case 2: itemCondition TIDAK NULL -> harus tetap sama
        Item item = new Item();
        item.setItemCondition("Rusak Ringan");

        Method onCreate = Item.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(item);

        assertEquals("Rusak Ringan", item.getItemCondition(), "Condition should not be overwritten");
    }

    @Test
    void testPreUpdate() throws Exception {
        Item item = new Item();
        
        // Init createdAt/updatedAt
        Method onCreate = Item.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(item);
        
        LocalDateTime initialUpdate = item.getUpdatedAt();
        Thread.sleep(10); // Pause biar waktu beda

        Method onUpdate = Item.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(item);

        assertTrue(item.getUpdatedAt().isAfter(initialUpdate));
    }
}