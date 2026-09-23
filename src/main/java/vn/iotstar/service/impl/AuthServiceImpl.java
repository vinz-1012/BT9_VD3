package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.dto.VerifyOtpDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.EmailService;
import vn.iotstar.service.OtpService;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;

    @Override
    @Transactional
    public void registerUser(RegisterDTO registerDTO) {
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu và xác nhận mật khẩu không trùng khớp");
        }

        if (userRepository.existsByUsername(registerDTO.getUsername().trim())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại, vui lòng chọn tên khác");
        }

        if (userRepository.existsByEmail(registerDTO.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã được đăng ký trong hệ thống");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .username(registerDTO.getUsername().trim())
                .fullName(registerDTO.getFullName().trim())
                .email(registerDTO.getEmail().trim().toLowerCase())
                .phone(registerDTO.getPhone() != null ? registerDTO.getPhone().trim() : null)
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .enabled(false)
                .roles(roles)
                .build();

        userRepository.save(user);

        String otpCode = otpService.generateAndSaveOtp(user.getEmail(), "REGISTER");
        emailService.sendOtpEmail(user.getEmail(), otpCode, "Đăng ký tài khoản");

        log.info("Registered user: {} with pending OTP verification", user.getUsername());
    }

    @Override
    @Transactional
    public boolean verifyRegisterOtp(VerifyOtpDTO verifyOtpDTO) {
        String email = verifyOtpDTO.getEmail().trim().toLowerCase();
        otpService.verifyOtp(email, verifyOtpDTO.getOtpCode().trim(), "REGISTER");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản"));

        user.setEnabled(true);
        userRepository.save(user);

        log.info("Activated account for user: {}", user.getUsername());
        return true;
    }

    @Override
    @Transactional
    public void resendRegisterOtp(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email này"));

        if (user.isEnabled()) {
            throw new IllegalArgumentException("Tài khoản này đã được kích hoạt trước đó");
        }

        String otpCode = otpService.generateAndSaveOtp(normalizedEmail, "REGISTER");
        emailService.sendOtpEmail(normalizedEmail, otpCode, "Gửi lại mã kích hoạt tài khoản");
        log.info("Resent register OTP to: {}", normalizedEmail);
    }

    @Override
    @Transactional
    public void sendForgotPasswordOtp(ForgotPasswordDTO forgotPasswordDTO) {
        String email = forgotPasswordDTO.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản nào khớp với email đã nhập"));

        String otpCode = otpService.generateAndSaveOtp(email, "RESET_PASSWORD");
        emailService.sendOtpEmail(email, otpCode, "Đặt lại mật khẩu");
        log.info("Sent forgot password OTP to: {}", email);
    }

    @Override
    @Transactional
    public boolean resetPasswordWithOtp(ResetPasswordDTO resetPasswordDTO) {
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không trùng khớp");
        }

        String email = resetPasswordDTO.getEmail().trim().toLowerCase();
        otpService.verifyOtp(email, resetPasswordDTO.getOtpCode().trim(), "RESET_PASSWORD");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản để đặt lại mật khẩu"));

        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        userRepository.save(user);

        log.info("Successfully reset password for user: {}", user.getUsername());
        return true;
    }
}
