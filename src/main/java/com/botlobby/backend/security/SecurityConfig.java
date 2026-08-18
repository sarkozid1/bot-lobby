package com.botlobby.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

// A Spring Security kozponti "kapcsolotablaja": itt dol el, mely URL-ek erhetok el
// hitelesites nelkul, hogyan folyik a jelszo-ellenorzes, es honnan enged CORS hivasokat a
// bongeszo. Minden itt beallitott Bean egyutt hatarozza meg a securityFilterChain(...) altal
// osszerakott vegso viselkedest.
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${botlobby.cors.allowed-origins}")
    private String allowedOrigins;

    // A jelszavak SOSEM nyers szovegkent tarolodnak - a BCryptPasswordEncoder hash-eli oket
    // (lasd AuthService.register), es ugyanez a Bean hasonlitja ossze bejelentkezeskor a
    // beirt jelszo hash-et a tarolt hash-sel (lasd authenticationProvider() lent).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * A megengedett origin-eket a botlobby.cors.allowed-origins property (CORS_ALLOWED_ORIGINS env
     * valtozo) adja meg, vesszovel elvalasztva - fejlesztesben a lokalis frontend, production-ben
     * a tenyleges deployolt frontend domain(ek).
     * <p>
     * allowCredentials=true kell, mert a SockJS kliens a "/ws/info" (es a xhr-streaming/polling)
     * kereseit withCredentials=true-val kuldi - allowCredentials=false mellett a bongeszo elutasitja
     * ezeket CORS hibaval, es a chat sosem kapcsolodik.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(parseAllowedOrigins(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Kulon, statikus segedmetoduskent van kiszervezve (nem csak a corsConfigurationSource()
    // belsejeben), mert a WebSocketConfig is ugyanezt a felbontasi logikat hasznalja a STOMP
    // handshake megengedett origin-jeihez - igy a ket helyen sosem csuszhat szet a szabaly.
    public static List<String> parseAllowedOrigins(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    // Osszekoti a CustomUserDetailsService-t (honnan jon a felhasznalo) es a
    // PasswordEncoder-t (hogyan hasonlitjuk ossze a jelszavakat) - ez a Bean vegzi
    // ténylegesen a "helyes-e az email+jelszo parosom" ellenorzest bejelentkezeskor.
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // A Spring Security altal belsőleg osszeallitott AuthenticationManager-t teszi Bean-nek
    // elerhetove - ezt hivja meg kozvetlenul az AuthService.login (authenticationManager.authenticate(...)).
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // A tenyleges szuro-lanc osszeallitasa - ez fut le minden HTTP kerelemre.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // A fent definialt CORS-szabalyokat kapcsoljuk be.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF vedelem kikapcsolva: ez sutialapu (cookie) session-oknel fontos, de itt
                // stateless JWT-t hasznalunk Authorization fejlecben, nincs mit CSRF-fel visszaelni.
                .csrf(AbstractHttpConfigurer::disable)
                // STATELESS: a szerver NEM tarol session-t a felhasznalorol a ket kerelem kozott -
                // minden kerelemnek onmagaban, a sajat JWT-jevel kell "bizonyitania" ki kuldte.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // A bongeszo CORS "preflight" (OPTIONS) kereseit mindig at kell engedni,
                        // meg vedett vegpontok elott is - ezek sosem hordoznak hitelesitest.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Regisztracio/bejelentkezes nyilvanvaloan nem lehet mar hitelesitest igenylo.
                        .requestMatchers("/api/auth/**").permitAll()
                        // A STOMP/WS handshake-et itt engedjuk at; a tenyleges uzenet-szintu auth-ot
                        // (JWT a CONNECT frame-ben) a WebSocketConfig ChannelInterceptor-a vegzi majd.
                        .requestMatchers("/ws/**").permitAll()
                        // Minden mas vegpont hitelesitest igenyel (a JwtAuthenticationFilter altal
                        // beallitott Authentication nelkul 401-et kap a hivo).
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                // A sajat JwtAuthenticationFilter-t a beepitett UsernamePasswordAuthenticationFilter
                // ELE tesszuk a lancban - igy mar akkor rendelkezesre all a hitelesites, mielott
                // Spring Security beepitett logikaja egyaltalan lefutna.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
