package org.delcom.app.controllers;

import org.delcom.app.dto.ItemForm;
import org.delcom.app.entities.Item;
import org.delcom.app.entities.User;
import org.delcom.app.services.ItemService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.delcom.app.dto.ChartData;
import java.util.List;
import java.util.ArrayList;

import jakarta.validation.Valid;
import java.util.UUID;

@Controller
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // --- HELPER: Ambil User dari Session ---
    private User getSessionUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }
    // ----------------------------------------

    @GetMapping
    public String list(Model model) {
        User user = getSessionUser();
        if (user == null) return "redirect:/auth/login";

        model.addAttribute("items", itemService.getAllItems(user));
        return ConstUtil.TEMPLATE_ITEMS_INDEX;
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("itemForm", new ItemForm());
        model.addAttribute("isEdit", false);
        return ConstUtil.TEMPLATE_ITEMS_FORM;
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("itemForm") ItemForm form,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        
        // 1. Ambil User dari Session
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        // 2. Cek Validasi
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return ConstUtil.TEMPLATE_ITEMS_FORM;
        }

        try {
            // 3. Simpan (Kirim user session)
            itemService.saveItem(form, user, null);
            redirectAttributes.addFlashAttribute("success", "Barang berhasil ditambahkan");
            return "redirect:/items";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Gagal menyimpan: " + e.getMessage());
            model.addAttribute("isEdit", false); // FIX: Add isEdit attribute
            return ConstUtil.TEMPLATE_ITEMS_FORM;
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable UUID id, Model model) {
        User user = getSessionUser();
        if (user == null) return "redirect:/auth/login";

        Item item = itemService.getItem(id, user);
        if (item == null) return "redirect:/items";

        ItemForm form = new ItemForm();
        form.setName(item.getName());
        form.setCategory(item.getCategory());
        form.setPrice(item.getPrice());
        form.setQuantity(item.getQuantity());
        form.setDescription(item.getDescription());
        form.setStorageLocation(item.getStorageLocation());
        form.setItemCondition(item.getItemCondition());
        
        model.addAttribute("itemForm", form);
        model.addAttribute("itemId", id);
        model.addAttribute("currentImage", item.getImageUrl());
        model.addAttribute("isEdit", true);
        return ConstUtil.TEMPLATE_ITEMS_FORM;
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable UUID id,
                         @Valid @ModelAttribute("itemForm") ItemForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        
        User user = getSessionUser();
        if (user == null) return "redirect:/auth/login";

        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            return ConstUtil.TEMPLATE_ITEMS_FORM;
        }
        
        try {
            itemService.saveItem(form, user, id);
            redirectAttributes.addFlashAttribute("success", "Barang berhasil diperbarui");
            return "redirect:/items";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("isEdit", true);  // FIX: Add isEdit attribute
            model.addAttribute("itemId", id);    // FIX: Add itemId attribute
            return ConstUtil.TEMPLATE_ITEMS_FORM;
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        User user = getSessionUser();
        if (user != null) {
            itemService.deleteItem(id, user);
            redirectAttributes.addFlashAttribute("success", "Barang berhasil dihapus");
        }
        return "redirect:/items";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        User user = getSessionUser();
        if (user == null) return "redirect:/auth/login";

        Item item = itemService.getItem(id, user);
        if (item == null) return "redirect:/items";
        model.addAttribute("item", item);
        return ConstUtil.TEMPLATE_ITEMS_DETAIL;
    }

    @GetMapping("/chart")
    public String chartPage(Model model) {
        User user = getSessionUser();
        if (user == null) return "redirect:/auth/login";

        // 1. Ambil data mentah dari Service
        List<ChartData> chartDataList = itemService.getChartData(user);

        // 2. Pisahkan Label (Kategori) dan Value (Total Harga) agar mudah dibaca JS
        List<String> labels = new ArrayList<>();
        List<Number> values = new ArrayList<>();

        for (ChartData data : chartDataList) {
            labels.add(data.getLabel());
            values.add(data.getValue());
        }

        // 3. Kirim ke Thymeleaf
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartValues", values);

        return ConstUtil.TEMPLATE_ITEMS_CHART;
    }
}