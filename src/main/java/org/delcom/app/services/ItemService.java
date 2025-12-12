package org.delcom.app.services;

import org.delcom.app.dto.ChartData;
import org.delcom.app.dto.ItemForm;
import org.delcom.app.entities.Item;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final FileStorageService fileStorageService;

    public ItemService(ItemRepository itemRepository, FileStorageService fileStorageService) {
        this.itemRepository = itemRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Item> getAllItems(User user) {
        // Pass UUID user
        return itemRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
    }

    public Item getItem(UUID id, User user) {
        return itemRepository.findByIdAndUserId(id, user.getId());
    }

    @Transactional
    public Item saveItem(ItemForm form, User user, UUID itemId) throws IOException {
        Item item;
        if (itemId != null) {
            // Update
            item = itemRepository.findByIdAndUserId(itemId, user.getId());
            if (item == null) throw new RuntimeException("Item not found");
        } else {
            // Create Baru
            item = new Item();
            // PENTING: Set userId UUID secara eksplisit
            item.setUserId(user.getId());
        }

        // Mapping Data Form ke Entity
        item.setName(form.getName());
        item.setCategory(form.getCategory());
        item.setQuantity(form.getQuantity());
        item.setPrice(form.getPrice());
        item.setDescription(form.getDescription());
        item.setStorageLocation(form.getStorageLocation()); // Field Baru
        item.setItemCondition(form.getItemCondition());     // Field Baru

        item = itemRepository.save(item);

        // Upload Gambar
        if (form.getImage() != null && !form.getImage().isEmpty()) {
            if (item.getImageUrl() != null) {
                fileStorageService.deleteFile(item.getImageUrl());
            }
            String filename = fileStorageService.storeFile(form.getImage(), item.getId());
            item.setImageUrl(filename);
            itemRepository.save(item);
        }

        return item;
    }

    @Transactional
    public void deleteItem(UUID id, User user) {
        Item item = itemRepository.findByIdAndUserId(id, user.getId());
        if (item != null) {
            if (item.getImageUrl() != null) {
                fileStorageService.deleteFile(item.getImageUrl());
            }
            itemRepository.delete(item);
        }
    }

    public List<ChartData> getChartData(User user) {
        return itemRepository.getInventoryValueByCategory(user.getId());
    }
}