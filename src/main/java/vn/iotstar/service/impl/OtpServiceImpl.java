package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.service.OtpService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String generateAndSaveOtp(String email, String tokenType) {
        invalidateOtp(email, tokenType);

        int randomNum = secureRandom.nextInt(1000000);
        String rawOtp = String.format("%06d", randomNum);

        String hashedOtp = passwordEncoder.encode(rawOtp);

        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otpHash(hashedOtp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .failedAttempts(0)
                .used(false)
                .tokenType(tokenType)
                .build();

        otpTokenRepository.save(otpToken);
        log.info("Generated new OTP token for email: {}, tokenType: {}", email, tokenType);

        return rawOtp;
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String rawOtp, String tokenType) {
        OtpToken otpToken = otpTokenRepository
                .findTopByEmailAndTokenTypeAndUsedFalseOrderByCreatedAtDesc(email, tokenType)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã OTP hoặc mã đã được sử dụng. Vui lòng yêu cầu mã mới."));

        if (otpToken.isExpired()) {
            otpToken.setUsed(true);
            otpTokenRepository.save(otpToken);
            throw new IllegalArgumentException("Mã OTP đã hết hạn (chỉ có hiệu lực trong 5 phút). Vui lòng yêu cầu mã mới.");
        }

        if (otpToken.isMaxAttemptsReached()) {
            otpToken.setUsed(true);
            otpTokenRepository.save(otpToken);
            throw new IllegalArgumentException("Bạn đã vượt quá 5 lần thử. Mã OTP đã bị hủy, vui lòng yêu cầu mã mới.");
        }

        boolean matches = passwordEncoder.matches(rawOtp, otpToken.getOtpHash());
        if (!matches) {
            int attempts = otpToken.getFailedAttempts() + 1;
            otpToken.setFailedAttempts(attempts);
            otpTokenRepository.save(otpToken);

            int remaining = 5 - attempts;
            if (remaining <= 0) {
                otpToken.setUsed(true);
                otpTokenRepository.save(otpToken);
                throw new IllegalArgumentException("Bạn đã nhập sai 5 lần. Mã OTP đã bị hủy, vui lòng gửi lại mã mới.");
            }
            throw new IllegalArgumentException("Mã OTP không chính xác. Bạn còn " + remaining + " lần thử.");
        }

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);
        log.info("OTP verified successfully for email: {}, tokenType: {}", email, tokenType);
        return true;
    }

    @Override
    @Transactional
    public void invalidateOtp(String email, String tokenType) {
        List<OtpToken> activeTokens = otpTokenRepository.findByEmailAndTokenTypeAndUsedFalse(email, tokenType);
        for (OtpToken token : activeTokens) {
            token.setUsed(true);
        }
        otpTokenRepository.saveAll(activeTokens);
    }
}
