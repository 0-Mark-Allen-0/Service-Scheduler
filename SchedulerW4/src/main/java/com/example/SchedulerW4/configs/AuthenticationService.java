// src/main/java/com/example/SchedulerW4/configs/AuthenticationService.java
package com.example.SchedulerW4.configs;

import com.example.SchedulerW4.dtos.auth_dtos.LoginRequestDto;
import com.example.SchedulerW4.dtos.auth_dtos.LoginResponseDto;
import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; // Re-add this import
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder; // Keep this, even if not used directly for auth here

    public LoginResponseDto authenticate(LoginRequestDto dto) {
        // Step 1: Authenticate credentials using Spring Security's AuthenticationManager
        // This will now use the custom UserDetailsService (defined in SecurityConfig or a separate class)
        // which includes the hardcoded admin user.
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        // Step 2: After successful authentication, retrieve the full User object.
        // This needs to handle both database users and the hardcoded admin.
        // If it's the hardcoded admin email, create a dummy User object for it.
        User user;
        if (dto.getEmail().equalsIgnoreCase(AuthenticationService.ADMIN_EMAIL)) {
            // For the hardcoded admin, create a User object
            user = new User();
            user.setId(0L); // A dummy ID for the hardcoded admin
            user.setName("Hardcoded Admin");
            user.setEmail(dto.getEmail());
            // No need to set password here, as authentication manager already verified it.
            user.setRole(User.Role.ADMIN);
        } else {
            // For other users, fetch from the database
            user = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found after successful authentication. This should not happen."));
        }

        // Step 3: Generate JWT token using the authenticated user's email
        String jwtToken = jwtService.generateToken(user.getEmail());

        // Step 4: Build and return the LoginResponseDto with all necessary details
        return LoginResponseDto.builder()
                .message("Logged In Successfully!")
                .role(user.getRole())
                .token(jwtToken)
                .build();
    }

    // Static constant for the hardcoded admin email
    public static final String ADMIN_EMAIL = "admin@gmail.com";
    // Static constant for the hardcoded admin raw password
    public static final String ADMIN_RAW_PASSWORD = "admin123";
}