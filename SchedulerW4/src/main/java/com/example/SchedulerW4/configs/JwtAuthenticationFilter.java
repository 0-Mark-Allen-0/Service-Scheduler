// src/main/java/com/example/SchedulerW4/configs/JwtAuthenticationFilter.java
package com.example.SchedulerW4.configs;

import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections; // Import Collections

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token); // Extract email from JWT

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Check if it's the hardcoded admin user
            if (AuthenticationService.ADMIN_EMAIL.equalsIgnoreCase(email)) {
                // Create a dummy User object for the hardcoded admin based on its role
                User adminUser = new User();
                adminUser.setId(0L); // Dummy ID
                adminUser.setName("Hardcoded Admin");
                adminUser.setEmail(AuthenticationService.ADMIN_EMAIL);
                adminUser.setRole(User.Role.ADMIN); // Set the ADMIN role

                // Validate the token for the hardcoded admin (just check expiration, signature)
                if (jwtService.isTokenValid(token)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            adminUser, // Principal is your User entity
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + User.Role.ADMIN.name()))
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } else {
                // For other users, try to load from the database
                userRepository.findByEmail(email).ifPresent(user -> {
                    // Validate the token against the user details from the database
                    if (jwtService.isTokenValid(token)) { // No longer need to pass userDetails to isTokenValid
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user, // Principal is your User entity
                                null,
                                user.getAuthorities() // Get authorities from your User entity
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                });
            }
        }
        filterChain.doFilter(request, response);
    }
}