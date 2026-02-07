package com.gymdiary.user.service;

import com.gymdiary.user.client.KeycloakAuthClient;
import com.gymdiary.user.configuration.KeycloakProperties;
import com.gymdiary.user.dto.*;
import com.gymdiary.user.exception.AuthenticationFailedException;
import com.gymdiary.user.exception.IdentityServiceProviderException;
import com.gymdiary.user.exception.InvalidTokenException;
import com.gymdiary.user.exception.UserAlreadyExistsException;
import com.gymdiary.user.model.UserProfile;
import com.gymdiary.user.repository.UserProfileRepository;
import com.gymdiary.user.util.KeycloakHelper;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserProfileRepository userProfileRepository;
    private final Keycloak keycloakClient;
    private final KeycloakHelper keycloakHelper;
    private final KeycloakProperties keycloakProperties;
    private final KeycloakAuthClient authClient;

    @Transactional
    public void register(RegisterRequest request) {
        UserRepresentation user = keycloakHelper.createUserRepresentation(request);
        UsersResource usersResource = keycloakClient.realm(keycloakProperties.realm()).users();

        try (Response response = usersResource.create(user)) {
            switch (response.getStatus()) {
                case 201 -> {
                    String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                    log.info("User created in Keycloak with ID: {}", userId);

                    try {
                        UserProfile profile = UserProfile.builder()
                                .keycloakId(UUID.fromString(userId))
                                .displayName(request.username())
                                .gender(request.gender())
                                .birthdate(request.birthdate())
                                .preferredUnit("KG")
                                .build();

                        userProfileRepository.save(profile);
                    } catch (DataAccessException e) {
                        usersResource.get(userId).remove();
                        log.error("Database save failed, Keycloak user deleted: {}", userId);
                        throw new IdentityServiceProviderException("Could not complete user profile creation in database");
                    }
                }
                case 409 -> throw new UserAlreadyExistsException("Username or email already taken");
                default -> throw new IdentityServiceProviderException("Keycloak error: " + response.getStatusInfo().getReasonPhrase());
            }
        }
    }

    public TokenResponse login(LoginRequest request) {
        try {
            KeycloakTokenResponse response = authClient.getToken(new KeycloakLoginRequest(
                    "password",
                    keycloakProperties.clientId(),
                    keycloakProperties.clientSecret(),
                    request.username(),
                    request.password()));
            return new TokenResponse(
                    response.accessToken(),
                    response.refreshToken(),
                    response.expiresIn(),
                    response.tokenType()
            );
        } catch (feign.FeignException.BadRequest | feign.FeignException.Unauthorized e) {
            log.error("Login failed for user {}: {}", request.username(), e.contentUTF8());
            throw new AuthenticationFailedException("Invalid username or password");
        } catch (Exception e) {
            log.error("Unexpected error during login", e);
            throw new IdentityServiceProviderException("Authentication service is temporarily unavailable");
        }
    }

    public TokenResponse refreshToken(RefreshRequest refreshRequest) {
        try {
            KeycloakTokenResponse response = authClient.refreshToken(new KeycloakRefreshRequest(
                    "refresh_token",
                    refreshRequest.refreshToken(),
                    keycloakProperties.clientId(),
                    keycloakProperties.clientSecret()
                    )
            );
            return new TokenResponse(
                    response.accessToken(),
                    response.refreshToken(),
                    response.expiresIn(),
                    response.tokenType()
            );
        } catch (Exception e) {
            log.error("Refresh failed: {}", e.getMessage());
            throw new InvalidTokenException("Session expired or refresh token is invalid");
        }
    }
}