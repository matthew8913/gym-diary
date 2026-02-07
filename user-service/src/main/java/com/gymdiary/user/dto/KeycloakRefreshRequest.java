package com.gymdiary.user.dto;

import feign.form.FormProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakRefreshRequest {
    @FormProperty("grant_type")
    private String grantType;

    @FormProperty("refresh_token")
    private String refreshToken;

    @FormProperty("client_id")
    private String clientId;

    @FormProperty("client_secret")
    private String clientSecret;
}