package org.delcom.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

public class ItemForm {
    @NotBlank(message = "Nama barang harus diisi")
    private String name;

    @NotBlank(message = "Kategori harus diisi")
    private String category;

    @NotNull(message = "Jumlah harus diisi")
    @Min(value = 1, message = "Minimal jumlah 1")
    private Integer quantity;

    @NotNull(message = "Harga harus diisi")
    @Min(value = 0, message = "Harga tidak boleh minus")
    private BigDecimal price;

    @NotBlank(message = "Lokasi penyimpanan harus diisi")
    private String storageLocation;

    @NotBlank(message = "Kondisi barang harus dipilih")
    private String itemCondition;

    private String description;
    
    private MultipartFile image; 

    // Getters Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public String getItemCondition() { return itemCondition; }
    public void setItemCondition(String itemCondition) { this.itemCondition = itemCondition; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public MultipartFile getImage() { return image; }
    public void setImage(MultipartFile image) { this.image = image; }
}