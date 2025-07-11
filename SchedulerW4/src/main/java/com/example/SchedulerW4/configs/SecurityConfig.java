package com.example.SchedulerW4.configs;

import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final UserRepository userRepository;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("=== Security Filter Chain Configuration ===");
        System.out.println("Configuring security for admin endpoints");

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/debug/**").permitAll() // Add debug endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        // CHANGED: Use hasAuthority instead of hasRole for more explicit matching
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/users/**").hasAuthority("ROLE_USER")
                        .requestMatchers("/providers/**").hasAuthority("ROLE_PROVIDER")
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        System.out.println("=== End Security Filter Chain Configuration ===");
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the UserDetailsService bean, which now handles both
     * database users and the hardcoded admin user.
     * Spring will inject the PasswordEncoder bean automatically here.
     *
     * @param passwordEncoder Spring will inject this bean (the one defined above)
     * @return an implementation of UserDetailsService
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        final String ADMIN_EMAIL = "admin@gmail.com"; // Placeholder for actual admin email
        final String ADMIN_RAW_PASSWORD = "admin123"; // Placeholder for actual admin password
        final String adminEncodedPassword = passwordEncoder.encode(ADMIN_RAW_PASSWORD);

        System.out.println("Hardcoded Admin Encoded Password (from UserDetailsService bean): " + adminEncodedPassword);
        System.out.println("Admin Credentials: Email - " + ADMIN_EMAIL + ", Password - " + ADMIN_RAW_PASSWORD);
        System.out.println("WARNING: Hardcoding credentials is not recommended for production environments.");

        return username -> {
            if (ADMIN_EMAIL.equalsIgnoreCase(username)) {
                return org.springframework.security.core.userdetails.User.builder()
                        .username(ADMIN_EMAIL)
                        .password(adminEncodedPassword)
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .disabled(false)
                        .build();
            } else {
                User user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
                return org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .disabled(false)
                        .build();
            }
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService(passwordEncoder()));
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
