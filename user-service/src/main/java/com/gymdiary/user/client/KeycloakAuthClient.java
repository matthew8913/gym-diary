package com.gymdiary.user.client;

import com.gymdiary.user.configuration.FeignFormConfig;
import com.gymdiary.user.dto.KeycloakLoginRequest;
import com.gymdiary.user.dto.KeycloakRefreshRequest;
import com.gymdiary.user.dto.KeycloakTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "keycloak-auth",
        url = "${keycloak.server-url}",
        configuration = FeignFormConfig.class
)
public interface KeycloakAuthClient {

    @PostMapping(value = "/realms/${keycloak.realm}/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KeycloakTokenResponse getToken(KeycloakLoginRequest request);

    @PostMapping(value = "/realms/${keycloak.realm}/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KeycloakTokenResponse refreshToken(KeycloakRefreshRequest request);
}