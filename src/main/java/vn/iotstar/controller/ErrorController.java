package vn.iotstar.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        int statusCode = 500;
        String errorTitle = "Đã xảy ra lỗi hệ thống";
        String errorDescription = "Xin lỗi, đã xảy ra lỗi trong quá trình xử lý yêu cầu của bạn.";

        if (status != null) {
            try {
                statusCode = Integer.parseInt(status.toString());

                if (statusCode == HttpStatus.NOT_FOUND.value()) {
                    errorTitle = "404 - Không Tìm Thấy Trang";
                    errorDescription = "Trang bạn đang tìm kiếm không tồn tại hoặc đã bị di chuyển.";
                } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                    errorTitle = "403 - Quyền Truy Cập Bị Từ Chối";
                    errorDescription = "Bạn không có quyền truy cập vào tài nguyên này.";
                } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    errorTitle = "500 - Lỗi Máy Chủ Nội Bộ";
                    errorDescription = (message != null && !message.toString().isBlank())
                            ? message.toString()
                            : "Máy chủ đang gặp sự cố kỹ thuật. Vui lòng thử lại sau.";
                }
            } catch (NumberFormatException ignored) {
            }
        }

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorTitle", errorTitle);
        model.addAttribute("errorDescription", errorDescription);
        model.addAttribute("pageTitle", "Lỗi - Shop");

        return "error";
    }
}
