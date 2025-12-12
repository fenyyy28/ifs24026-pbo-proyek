package org.delcom.app.controllers.api;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.dto.ChartData;
import org.delcom.app.services.ItemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemApiController {
    private final ItemService itemService;
    private final AuthContext authContext;

    public ItemApiController(ItemService itemService, AuthContext authContext) {
        this.itemService = itemService;
        this.authContext = authContext;
    }

    // API untuk data Chart JS
    @GetMapping("/stats")
    public ApiResponse<List<ChartData>> getStats() {
        List<ChartData> data = itemService.getChartData(authContext.getAuthUser());
        return new ApiResponse<>("success", "Data chart berhasil diambil", data);
    }
}