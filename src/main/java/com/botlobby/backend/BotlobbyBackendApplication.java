package com.botlobby.backend;

import com.botlobby.backend.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

// Ez a Java-alkalmazas belepesi pontja (a main metodus). @SpringBootApplication egyben
// harom annotaciot von ossze: @Configuration (ez az osztaly maga is konfiguraciot adhat),
// @EnableAutoConfiguration (Spring Boot automatikusan beallitja a felismert fuggosegek
// alapjan pl. a beagyazott Tomcat szervert, JPA-t, stb.), es @ComponentScan (a
// com.botlobby.backend csomagban es alcsomagjaiban levo osszes @Component/@Service/
// @Repository/@Controller osztalyt automatikusan megtalalja es Bean-kent regisztralja).
@SpringBootApplication
// A JwtProperties egy @ConfigurationProperties record (lasd security/JwtProperties.java) -
// ez az annotacio mondja meg Springnek, hogy TENYLEGESEN hozza is letre Bean-kent, az
// application.yml "botlobby.jwt.*" ertekeivel feltoltve. @ConfigurationProperties
// onmagaban NEM eleg, ezt valahol expliciten be kell kapcsolni.
@EnableConfigurationProperties(JwtProperties.class)
public class BotlobbyBackendApplication {

    public static void main(String[] args) {
        // Elinditja a teljes Spring kontextust: beolvassa a konfiguraciot, letrehozza az
        // osszes Bean-t (repository-k, service-ek, controllerek, security-lanc, stb.), majd
        // elinditja a beagyazott webszervert (lasd application.yml server.port).
        SpringApplication.run(BotlobbyBackendApplication.class, args);
    }
}
