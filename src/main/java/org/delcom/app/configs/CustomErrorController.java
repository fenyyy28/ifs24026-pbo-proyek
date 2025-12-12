package org.delcom.app.configs;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        // Pastikan Anda punya file 'error.html' di folder 'src/main/resources/templates/'
        // Jika tidak ada, buat file kosong bernama error.html di sana.
        return "error";
    }
}