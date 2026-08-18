package com.botlobby.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

// @ConfigurationProperties(prefix = "botlobby.jwt"): Spring Boot automatikusan kitolti ezt a
// record-ot az application.yml "botlobby.jwt.*" kulcsaibol (secret -> botlobby.jwt.secret,
// expirationMs -> botlobby.jwt.expiration-ms - a kebab-case/camelCase kozotti automatikus
// atalakitas a "relaxed binding" resze). Az application.yml-ben ezek env valtozobol jonnek:
// ${JWT_SECRET} (nincs alapertelmezes - kotelezo beallitani), ${JWT_EXPIRATION_MS:86400000}.
// Ehhez a class-hoz meg kell egy @EnableConfigurationProperties(JwtProperties.class) is
// valahol (lasd BotlobbyBackendApplication.java), hogy Spring egyaltalan Bean-kent letrehozza.
@ConfigurationProperties(prefix = "botlobby.jwt")
public record JwtProperties(String secret, long expirationMs) {
}
