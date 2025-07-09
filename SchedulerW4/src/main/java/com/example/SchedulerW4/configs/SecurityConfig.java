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
@RequiredArgsConstructor // This lombok annotation injects the final fields
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final UserRepository userRepository;
    private final CorsConfigurationSource corsConfigurationSource;
    // private final PasswordEncoder passwordEncoder; // <--- REMOVE THIS FIELD INJECTION!

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/admin/**").hasRole(User.Role.ADMIN.name())
                        .requestMatchers("/users/**").hasRole(User.Role.USER.name())
                        .requestMatchers("/providers/**").hasRole(User.Role.PROVIDER.name())
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").hasRole(User.Role.ADMIN.name())
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider()) // This will implicitly use the PasswordEncoder bean
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // This bean method is correctly defined and will be found by Spring.
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
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) { // Keep injecting PasswordEncoder here
        final String ADMIN_EMAIL = AuthenticationService.ADMIN_EMAIL; // Use static final from AuthService
        final String ADMIN_RAW_PASSWORD = AuthenticationService.ADMIN_RAW_PASSWORD; // Use static final from AuthService
        final String adminEncodedPassword = passwordEncoder.encode(ADMIN_RAW_PASSWORD);

        System.out.println("Hardcoded Admin Encoded Password (from UserDetailsService bean): " + adminEncodedPassword);
        System.out.println("Admin Credentials: Email - " + ADMIN_EMAIL + ", Password - " + ADMIN_RAW_PASSWORD);
        System.out.println("WARNING: Hardcoding credentials is not recommended for production environments.");

        return username -> {
            if (ADMIN_EMAIL.equalsIgnoreCase(username)) {
                return org.springframework.security.core.userdetails.User.builder()
                        .username(ADMIN_EMAIL)
                        .password(adminEncodedPassword)
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + User.Role.ADMIN.name())))
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .disabled(false)
                        .build();
            } else {
                User user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
                return user;
            }
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Here, userDetailsService() and passwordEncoder() are *method calls* to get the beans,
        // not constructor injections. Spring's wiring handles this.
        authProvider.setUserDetailsService(userDetailsService(passwordEncoder())); // Call the method to get the bean
        authProvider.setPasswordEncoder(passwordEncoder()); // Call the method to get the bean
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}