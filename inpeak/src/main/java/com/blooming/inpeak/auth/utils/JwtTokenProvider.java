package com.blooming.inpeak.auth.utils;

import com.blooming.inpeak.member.domain.Member;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer:inpeak}")
    private String issuer;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String makeToken(Member member, Duration expiredAt) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiredAt.toMillis());

        return Jwts.builder()
            .header()
            .type("JWT")
            .and()
            .issuer(issuer)
            .issuedAt(now)
            .expiration(expiry)
            .subject(String.valueOf(member.getId()))
            .signWith(secretKey)
            .compact();
    }

    /**
     * 토큰에서 사용자 ID 추출
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public String getUserIdFromToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    /**
     * 토큰 유효성 검증
     *
     * @param token 검증할 토큰
     * @return 유효성 여부
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long getExpirationTime(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getExpiration()
            .getTime();
    }
}
