package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    @GetMapping
    public String listUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserDTO> userPage = userService.getAllUsers(keyword, pageable);

        long totalUsers = userService.countTotalUsers();

        model.addAttribute("userPage", userPage);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalElements", userPage.getTotalElements());
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("pageTitle", "Quản Trị Người Dùng - Shop");

        return "users/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        UserDTO userDTO = new UserDTO();
        userDTO.setEnabled(true);
        List<Role> allRoles = roleRepository.findAll();

        model.addAttribute("user", userDTO);
        model.addAttribute("allRoles", allRoles);
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Thêm Người Dùng Mới - Shop");
        return "users/form";
    }

    @PostMapping("/create")
    public String processCreate(
            @Valid @ModelAttribute("user") UserDTO userDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("isEdit", false);
            model.addAttribute("pageTitle", "Thêm Người Dùng Mới - Shop");
            return "users/form";
        }

        try {
            userService.createUser(userDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm người dùng mới thành công! (Mật khẩu mặc định: 123456)");
            return "redirect:/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("isEdit", false);
            model.addAttribute("pageTitle", "Thêm Người Dùng Mới - Shop");
            return "users/form";
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi tạo người dùng: " + e.getMessage());
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("isEdit", false);
            model.addAttribute("pageTitle", "Thêm Người Dùng Mới - Shop");
            return "users/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        UserDTO userDTO = userService.getUserById(id);
        List<Role> allRoles = roleRepository.findAll();

        model.addAttribute("user", userDTO);
        model.addAttribute("allRoles", allRoles);
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Chỉnh Sửa Người Dùng - " + userDTO.getUsername());
        return "users/form";
    }

    @PostMapping("/edit/{id}")
    public String processEdit(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("user") UserDTO userDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Chỉnh Sửa Người Dùng");
            return "users/form";
        }

        try {
            userService.updateUser(id, userDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin người dùng thành công!");
            return "redirect:/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Chỉnh Sửa Người Dùng");
            return "users/form";
        } catch (Exception e) {
            log.error("Error updating user ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi cập nhật người dùng: " + e.getMessage());
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Chỉnh Sửa Người Dùng");
            return "users/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa người dùng thành công!");
        } catch (Exception e) {
            log.error("Error deleting user ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa người dùng: " + e.getMessage());
        }
        return "redirect:/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUserGet(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        return deleteUser(id, redirectAttributes);
    }
}
