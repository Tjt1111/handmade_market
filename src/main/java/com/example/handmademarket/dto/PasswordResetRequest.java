package com.example.handmademarket.dto;

public class PasswordResetRequest {
    private String account; // 手机号/邮箱/账号
    private String code;    // 验证码（演示固定 123456）
    public  static final String DEFAULT_PASSWORD = "a123456";

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}