package vn.iotstar.service;

public interface OtpService {

    String generateAndSaveOtp(String email, String tokenType);

    boolean verifyOtp(String email, String rawOtp, String tokenType);

    void invalidateOtp(String email, String tokenType);
}
