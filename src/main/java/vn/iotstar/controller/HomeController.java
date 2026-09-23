package vn.iotstar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.UserService;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        long totalUsers = userService.countTotalUsers();
        long totalProducts = productService.countTotalProducts();

        Page<ProductDTO> recentProducts = productService.getAllProducts(
                null,
                PageRequest.of(0, 6, Sort.by("id").descending())
        );

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("recentProducts", recentProducts.getContent());
        model.addAttribute("pageTitle", "Trang Chủ - Hệ Thống Shop");

        return "home";
    }
}
