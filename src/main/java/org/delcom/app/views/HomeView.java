package org.delcom.app.views;

import org.delcom.app.entities.Item;
import org.delcom.app.entities.User;
import org.delcom.app.services.ItemService; // Menggunakan ItemService
import org.delcom.app.services.UserService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
public class HomeView {

    private final UserService userService;
    private final ItemService itemService; // Ganti CookieService ke ItemService

    public HomeView(UserService userService, ItemService itemService) {
        this.userService = userService;
        this.itemService = itemService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // 1. Ambil Authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;

        // Cek apakah principal valid dan bukan anonymous
        if (auth != null && auth.getPrincipal() instanceof User) {
            user = (User) auth.getPrincipal();
            // Refresh data user dari DB agar sinkron (jika ada update di sesi lain)
            user = userService.getUserById(user.getId());
        }

        model.addAttribute("user", user);

        if (user != null) {
            // 2. Ambil semua data item milik user
            List<Item> items = itemService.getAllItems(user);

            if (items == null) {
                items = Collections.emptyList();
            }

            // 3. Hitung Statistik Dashboard
            
            // Total Jenis Barang (Jumlah item distinct di list)
            int totalItemTypes = items.size();

            // Total Seluruh Stok (Sum of quantity)
            int totalQuantity = items.stream()
                    .mapToInt(Item::getQuantity)
                    .sum();

            // Total Nilai Aset (Sum of Price * Quantity)
            BigDecimal totalAssetValue = items.stream()
                    .filter(i -> i.getPrice() != null && i.getQuantity() != null)
                    .map(i -> i.getPrice().multiply(new BigDecimal(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Total Kategori Unik
            long totalCategories = items.stream()
                    .map(Item::getCategory)
                    .distinct()
                    .count();

            // 4. Masukkan ke Model
            model.addAttribute("totalItemTypes", totalItemTypes);
            model.addAttribute("totalQuantity", totalQuantity);
            model.addAttribute("totalAssetValue", totalAssetValue);
            model.addAttribute("totalCategories", totalCategories);
            
            // Kirim 5 barang yang baru diupdate/ditambahkan untuk tabel ringkas
            model.addAttribute("recentItems", items.stream().limit(5).toList());

        } else {
            // Default value jika tidak login
            model.addAttribute("totalItemTypes", 0);
            model.addAttribute("totalQuantity", 0);
            model.addAttribute("totalAssetValue", BigDecimal.ZERO);
            model.addAttribute("totalCategories", 0);
        }

        return ConstUtil.TEMPLATE_PAGES_HOME;
    }
}