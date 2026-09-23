package vn.iotstar.service;

import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.dto.VerifyOtpDTO;

public interface AuthService {

    void registerUser(RegisterDTO registerDTO);

    boolean verifyRegisterOtp(VerifyOtpDTO verifyOtpDTO);

    void resendRegisterOtp(String email);

    void sendForgotPasswordOtp(ForgotPasswordDTO forgotPasswordDTO);

    boolean resetPasswordWithOtp(ResetPasswordDTO resetPasswordDTO);
}
