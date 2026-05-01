package com.resale.platform.service;

import com.resale.platform.dto.request.SmsLoginRequest;
import com.resale.platform.dto.response.LoginResponse;

import java.util.List;
import java.util.Map;

public interface SmsService {

    String sendLoginCode(String mobile);

    LoginResponse loginBySms(SmsLoginRequest request);

    boolean isMobileRegistered(String mobile);

    List<Map<String, Object>> getRecentSmsCodes();

    boolean verifyCode(String mobile, String code);
}
