package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.service.ProductService;

import java.security.Principal;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String listProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ProductDTO> productPage = productService.getAllProducts(keyword, pageable);

        model.addAttribute("productPage", productPage);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("pageTitle", "Quản Lý Sản Phẩm - Shop");

        return "products/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new ProductDTO());
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Thêm Sản Phẩm Mới - Shop");
        return "products/form";
    }

    @PostMapping("/create")
    public String processCreate(
            @Valid @ModelAttribute("product") ProductDTO productDTO,
            BindingResult bindingResult,
            @RequestParam("imageFile") MultipartFile imageFile,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (imageFile == null || imageFile.isEmpty()) {
            bindingResult.rejectValue("imageFile", "error.product", "Vui lòng chọn ảnh cho sản phẩm");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("pageTitle", "Thêm Sản Phẩm Mới - Shop");
            return "products/form";
        }

        try {
            productService.createProduct(productDTO, imageFile, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
            return "redirect:/products";
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Lỗi khi tạo sản phẩm: " + e.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("pageTitle", "Thêm Sản Phẩm Mới - Shop");
            return "products/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        ProductDTO productDTO = productService.getProductById(id);
        model.addAttribute("product", productDTO);
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Chỉnh Sửa Sản Phẩm - " + productDTO.getName());
        return "products/form";
    }

    @PostMapping("/edit/{id}")
    public String processEdit(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("product") ProductDTO productDTO,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Chỉnh Sửa Sản Phẩm");
            return "products/form";
        }

        try {
            productService.updateProduct(id, productDTO, imageFile, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
            return "redirect:/products";
        } catch (Exception e) {
            log.error("Error updating product ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("errorMessage", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Chỉnh Sửa Sản Phẩm");
            return "products/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(
            @PathVariable("id") Long id,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            productService.deleteProduct(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            log.error("Error deleting product ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProductGet(
            @PathVariable("id") Long id,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        return deleteProduct(id, principal, redirectAttributes);
    }
}
