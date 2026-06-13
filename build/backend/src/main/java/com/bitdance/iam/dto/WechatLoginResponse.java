package com.bitdance.iam.dto;

public record WechatLoginResponse(
    String token,
    UserSummary user,
    boolean passwordRequired,
    boolean bindPhoneRequired,
    String bindToken,
    long bindExpiresIn
) {
    public static WechatLoginResponse loggedIn(LoginResponse login) {
        return new WechatLoginResponse(login.token(), login.user(), login.passwordRequired(), false, null, 0);
    }

    public static WechatLoginResponse phoneBindingRequired(String bindToken, long bindExpiresIn) {
        return new WechatLoginResponse(null, null, false, true, bindToken, bindExpiresIn);
    }
}
