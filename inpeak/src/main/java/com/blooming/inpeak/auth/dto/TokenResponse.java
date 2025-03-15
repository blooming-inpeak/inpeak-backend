package com.blooming.inpeak.auth.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    Long accessTokenExpiresIn,
    Long refreshTokenExpiresIn
) {
}
