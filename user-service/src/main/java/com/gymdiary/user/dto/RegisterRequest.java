package com.gymdiary.user.dto;

import java.time.LocalDate;

public record RegisterRequest(
        String username,
        String password,
        String email,
        String gender,
        LocalDate birthdate,
        String preferredUnit
) {
}