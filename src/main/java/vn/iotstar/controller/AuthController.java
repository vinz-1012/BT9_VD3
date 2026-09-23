package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.dto.VerifyOtpDTO;
import vn.iotstar.service.AuthService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "resetSuccess", required = false) String resetSuccess,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("errorMessage", "Tài khoản hoặc mật khẩu không chính xác hoặc tài khoản chưa kích hoạt.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Tài khoản của bạn đã được kích hoạt thành công! Hãy đăng nhập.");
        }
        if (resetSuccess != null) {
            model.addAttribute("successMessage", "Đổi mật khẩu thành công! Hãy đăng nhập bằng mật khẩu mới.");
        }
        model.addAttribute("pageTitle", "Đăng Nhập - Shop");
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        model.addAttribute("pageTitle", "Đăng Ký Tài Khoản - Shop");
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("registerDTO") RegisterDTO registerDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Đăng Ký Tài Khoản - Shop");
            return "auth/register";
        }

        try {
            authService.registerUser(registerDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký thành công! Mã OTP xác thực (hiệu lực 5 phút) đã được gửi đến email của bạn.");
            return "redirect:/verify-otp?email=" + registerDTO.getEmail();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "Đăng Ký Tài Khoản - Shop");
            return "auth/register";
        } catch (Exception e) {
            log.error("Register error: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Đã xảy ra lỗi trong quá trình đăng ký. Vui lòng thử lại.");
            model.addAttribute("pageTitle", "Đăng Ký Tài Khoản - Shop");
            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(
            @RequestParam(value = "email", required = false) String email,
            Model model
    ) {
        VerifyOtpDTO dto = VerifyOtpDTO.builder()
                .email(email != null ? email : "")
                .tokenType("REGISTER")
                .build();
        model.addAttribute("verifyOtpDTO", dto);
        model.addAttribute("pageTitle", "Xác Thực Mã OTP - Shop");
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(
            @Valid @ModelAttribute("verifyOtpDTO") VerifyOtpDTO verifyOtpDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Xác Thực Mã OTP - Shop");
            return "auth/verify-otp";
        }

        try {
            authService.verifyRegisterOtp(verifyOtpDTO);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "Xác Thực Mã OTP - Shop");
            return "auth/verify-otp";
        } catch (Exception e) {
            log.error("Verify OTP error: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Đã xảy ra lỗi trong quá trình xác thực OTP.");
            model.addAttribute("pageTitle", "Xác Thực Mã OTP - Shop");
            return "auth/verify-otp";
        }
    }

    @PostMapping("/resend-register-otp")
    public String resendRegisterOtp(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes
    ) {
        try {
            authService.resendRegisterOtp(email);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Mã OTP mới đã được gửi tới email " + email + ". Vui lòng kiểm tra hộp thư!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/verify-otp?email=" + email;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        model.addAttribute("pageTitle", "Quên Mật Khẩu - Shop");
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @Valid @ModelAttribute("forgotPasswordDTO") ForgotPasswordDTO forgotPasswordDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Quên Mật Khẩu - Shop");
            return "auth/forgot-password";
        }

        try {
            authService.sendForgotPasswordOtp(forgotPasswordDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Mã OTP đặt lại mật khẩu đã được gửi đến email " + forgotPasswordDTO.getEmail());
            return "redirect:/reset-password?email=" + forgotPasswordDTO.getEmail();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "Quên Mật Khẩu - Shop");
            return "auth/forgot-password";
        } catch (Exception e) {
            log.error("Forgot password error: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Không thể gửi OTP đặt lại mật khẩu. Vui lòng thử lại sau.");
            model.addAttribute("pageTitle", "Quên Mật Khẩu - Shop");
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(
            @RequestParam(value = "email", required = false) String email,
            Model model
    ) {
        ResetPasswordDTO dto = ResetPasswordDTO.builder()
                .email(email != null ? email : "")
                .build();
        model.addAttribute("resetPasswordDTO", dto);
        model.addAttribute("pageTitle", "Đặt Lại Mật Khẩu - Shop");
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @Valid @ModelAttribute("resetPasswordDTO") ResetPasswordDTO resetPasswordDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Đặt Lại Mật Khẩu - Shop");
            return "auth/reset-password";
        }

        try {
            authService.resetPasswordWithOtp(resetPasswordDTO);
            return "redirect:/login?resetSuccess=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "Đặt Lại Mật Khẩu - Shop");
            return "auth/reset-password";
        } catch (Exception e) {
            log.error("Reset password error: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi đặt lại mật khẩu. Vui lòng thử lại.");
            model.addAttribute("pageTitle", "Đặt Lại Mật Khẩu - Shop");
            return "auth/reset-password";
        }
    }
}
