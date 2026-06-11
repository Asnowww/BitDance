package com.bitdance.iam.service;

public interface SmsSender {

    void sendCode(String phone, String code);
}
