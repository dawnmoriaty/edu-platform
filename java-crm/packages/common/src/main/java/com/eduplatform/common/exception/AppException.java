package com.eduplatform.common.exception;

import com.eduplatform.common.constant.ErrorCode;
import lombok.Getter;

/**
 * AppException - Exception chuẩn hóa cho toàn bộ hệ thống
 */
@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String customMessage;
    private final Object[] args;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
        this.args = null;
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.args = null;
    }

    public AppException(ErrorCode errorCode, String customMessage, Object... args) {
        super(String.format(customMessage, args));
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.args = args;
    }

    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.customMessage = null;
        this.args = null;
    }

    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    public int getCode() {
        return errorCode.getCode();
    }

    public String getErrorMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}
