package com.botlobby.backend;

import com.botlobby.backend.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class BotlobbyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotlobbyBackendApplication.class, args);
    }
}
