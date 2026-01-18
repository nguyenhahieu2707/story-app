package com.ptit.story_speaker.common.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
public enum ErrorCode{
    // === Lỗi Chung ===
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_500", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VAL_400", "Dữ liệu đầu vào không hợp lệ."),

    // === Lỗi Xác thực (Authentication) ===
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_401", "Bạn cần xác thực để truy cập tài nguyên này."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_403", "Bạn không có quyền truy cập tài nguyên này."),
    INVALID_GOOGLE_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_1001", "Google ID Token không hợp lệ hoặc đã hết hạn."),
    INVALID_FACEBOOK_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_1002", "Facebook Access Token không hợp lệ hoặc đã hết hạn."),
    INCORRECT_USERNAME_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_1003", "Tài khoản hoặc mật khẩu không chính xác."),
    NOT_ADMIN_ACCOUNT(HttpStatus.FORBIDDEN, "AUTH_1004", "Tài khoản này không có quyền quản trị."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.FORBIDDEN, "AUTH_1005", "Refresh token không tồn tại trong hệ thống."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "AUTH_1006", "Refresh token đã hết hạn. Vui lòng đăng nhập lại."),

    // === Lỗi Nghiệp vụ (Business) ===
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "BUSS_404", "Không tìm thấy tài nguyên được yêu cầu."),
    EMAIL_EXISTS(HttpStatus.BAD_REQUEST, "BUSS_1001", "Email này đã được sử dụng.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
