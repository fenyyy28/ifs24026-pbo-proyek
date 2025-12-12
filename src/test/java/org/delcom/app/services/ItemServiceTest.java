package org.delcom.app.services;

import org.delcom.app.dto.ItemForm;
import org.delcom.app.entities.Item;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ItemService itemService;

    @Test
    void testGetAllItems() {
        User user = new User();
        user.setId(UUID.randomUUID());
        
        when(itemRepository.findByUserIdOrderByUpdatedAtDesc(user.getId())).thenReturn(List.of(new Item()));
        
        List<Item> items = itemService.getAllItems(user);
        assertFalse(items.isEmpty());
    }

    @Test
    void testGetItem() {
        UUID itemId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        Item item = new Item();
        
        when(itemRepository.findByIdAndUserId(itemId, user.getId())).thenReturn(item);
        
        assertEquals(item, itemService.getItem(itemId, user));
    }

    @Test
    void testSaveItem_CreateNew_NoImage() throws IOException {
        User user = new User();
        user.setId(UUID.randomUUID());
        ItemForm form = new ItemForm();
        form.setName("New Item");
        form.setPrice(BigDecimal.TEN);
        
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> {
            Item s = i.getArgument(0);
            s.setId(UUID.randomUUID()); // Simulasi ID generate dari DB
            return s;
        });

        Item result = itemService.saveItem(form, user, null);

        assertEquals("New Item", result.getName());
        assertEquals(user.getId(), result.getUserId());
        assertNull(result.getImageUrl());
        
        verify(fileStorageService, never()).storeFile(any(), any());
    }

    // --- TEST BARU 1: Create dengan Image (Cover kondisi item.getImageUrl() == null di block upload) ---
    @Test
    void testSaveItem_CreateNew_WithImage() throws IOException {
        User user = new User();
        user.setId(UUID.randomUUID());
        
        ItemForm form = new ItemForm();
        form.setName("New With Image");
        
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        form.setImage(mockFile);

        // Mock save pertama kali (return item tanpa image url)
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> {
            Item s = i.getArgument(0);
            if(s.getId() == null) s.setId(UUID.randomUUID());
            return s;
        });
        
        when(fileStorageService.storeFile(eq(mockFile), any())).thenReturn("cover_new.jpg");

        Item result = itemService.saveItem(form, user, null);

        assertEquals("cover_new.jpg", result.getImageUrl());
        
        // Verifikasi tidak ada deleteFile dipanggil (karena item baru belum punya gambar)
        verify(fileStorageService, never()).deleteFile(anyString());
        // Verifikasi storeFile dipanggil
        verify(fileStorageService).storeFile(eq(mockFile), any());
    }

    // --- TEST BARU 2: Image Empty (Cover kondisi !form.getImage().isEmpty() == False) ---
    @Test
    void testSaveItem_WithEmptyImage() throws IOException {
        User user = new User();
        user.setId(UUID.randomUUID());
        ItemForm form = new ItemForm();
        form.setName("Item Empty Image");
        
        MultipartFile mockFile = mock(MultipartFile.class);
        // Kondisi file ada objeknya tapi kosong (0 byte)
        when(mockFile.isEmpty()).thenReturn(true); 
        form.setImage(mockFile);

        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArgument(0));

        itemService.saveItem(form, user, null);

        // Pastikan logic upload dilewati
        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    void testSaveItem_UpdateExisting_WithImage() throws IOException {
        UUID itemId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        
        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setUserId(user.getId());
        existingItem.setImageUrl("old_image.jpg");

        ItemForm form = new ItemForm();
        form.setName("Updated Name");
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false); 
        form.setImage(mockFile);

        when(itemRepository.findByIdAndUserId(itemId, user.getId())).thenReturn(existingItem);
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArgument(0));
        when(fileStorageService.storeFile(eq(mockFile), any())).thenReturn("new_image.jpg");

        Item result = itemService.saveItem(form, user, itemId);

        assertEquals("new_image.jpg", result.getImageUrl());
        
        // Verifikasi file lama dihapus
        verify(fileStorageService).deleteFile("old_image.jpg");
    }

    @Test
    void testSaveItem_Update_NotFound() {
        User user = new User();
        user.setId(UUID.randomUUID());
        ItemForm form = new ItemForm();
        UUID itemId = UUID.randomUUID();

        when(itemRepository.findByIdAndUserId(itemId, user.getId())).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            itemService.saveItem(form, user, itemId);
        });
    }

    @Test
    void testDeleteItem_SuccessWithImage() {
        UUID itemId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        
        Item item = new Item();
        item.setImageUrl("pic.jpg");

        when(itemRepository.findByIdAndUserId(itemId, user.getId())).thenReturn(item);

        itemService.deleteItem(itemId, user);

        verify(fileStorageService).deleteFile("pic.jpg");
        verify(itemRepository).delete(item);
    }

    // --- TEST BARU 3: Delete Item Tanpa Gambar (Cover kondisi item.getImageUrl() == null) ---
    @Test
    void testDeleteItem_SuccessNoImage() {
        UUID itemId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        
        Item item = new Item();
        item.setImageUrl(null); // Tidak punya gambar

        when(itemRepository.findByIdAndUserId(itemId, user.getId())).thenReturn(item);

        itemService.deleteItem(itemId, user);

        // Verifikasi deleteFile TIDAK dipanggil
        verify(fileStorageService, never()).deleteFile(anyString());
        // Tapi item tetap dihapus dari DB
        verify(itemRepository).delete(item);
    }
    
    @Test
    void testDeleteItem_NotFound() {
        UUID itemId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        when(itemRepository.findByIdAndUserId(itemId, user.getId())).thenReturn(null);

        itemService.deleteItem(itemId, user);

        verify(itemRepository, never()).delete(any());
    }

    @Test
    void testGetChartData() {
        User user = new User();
        user.setId(UUID.randomUUID());
        
        itemService.getChartData(user);
        verify(itemRepository).getInventoryValueByCategory(user.getId());
    }
}