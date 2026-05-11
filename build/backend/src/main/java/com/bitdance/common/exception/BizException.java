package com.bitdance.common.exception;

public class BizException extends RuntimeException {

    private final String code;

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static BizException of(String code, String message) {
        return new BizException(code, message);
    }
}
