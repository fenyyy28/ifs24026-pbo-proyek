package org.delcom.app.repositories;

import org.delcom.app.dto.ChartData;
import org.delcom.app.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    
    // Cari berdasarkan kolom userId (UUID)
    List<Item> findByUserIdOrderByUpdatedAtDesc(UUID userId);
    
    Item findByIdAndUserId(UUID id, UUID userId);

    // Query Chart: Hitung total nilai per kategori berdasarkan userId
    @Query("SELECT i.category as label, SUM(i.price * i.quantity) as value " +
           "FROM Item i WHERE i.userId = ?1 GROUP BY i.category")
    List<ChartData> getInventoryValueByCategory(UUID userId);
}