package com.botlobby.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "botlobby.jwt")
public record JwtProperties(String secret, long expirationMs) {
}
