// src/main/java/com/example/SchedulerW4/configs/AuthenticationService.java
package com.example.SchedulerW4.configs;

import com.example.SchedulerW4.dtos.auth_dtos.LoginRequestDto;
import com.example.SchedulerW4.dtos.auth_dtos.LoginResponseDto;
import com.example.SchedulerW4.dtos.auth_dtos.OtpRequestDto;
import com.example.SchedulerW4.dtos.notification_dtos.NotificationDto;
import com.example.SchedulerW4.entities.Otp;
import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.notifications.NotificationMessageProducer;
import com.example.SchedulerW4.repositories.OtpRepository;
import com.example.SchedulerW4.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationMessageProducer notificationMessageProducer;

    private static final int OTP_VALIDITY_MINUTES = 5;

    @Transactional
    public LoginResponseDto authenticate(LoginRequestDto dto) {
        // Step 1: Authenticate credentials using Spring Security's AuthenticationManager
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        // Step 2: After successful credential authentication, retrieve the full User object.
        User user;
        if (dto.getEmail().equalsIgnoreCase(AuthenticationService.ADMIN_EMAIL)) {
            user = new User();
            user.setId(0L);
            user.setName("Hardcoded Admin");
            user.setEmail(dto.getEmail());
            user.setRole(User.Role.ADMIN);
        } else {
            user = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found after successful authentication. This should not happen."));
        }

        // --- NEW LOGIC: Bypass OTP for ADMIN role ---
        if (user.getRole() == User.Role.ADMIN) {
            String jwtToken = jwtService.generateToken(user.getEmail(), user.getRole().name());

            return LoginResponseDto.builder()
                    .message("Logged In Successfully as Admin!")
                    .role(user.getRole())
                    .token(jwtToken)
                    .otpRequired(false) // No OTP required for admin
                    .email(user.getEmail())
                    .build();
        }
        // --- END NEW LOGIC ---


        // Step 3: Generate and store OTP in the separate Otp entity (for USER and PROVIDER roles)
        // First, clear any old OTPs for this email to prevent confusion
        otpRepository.deleteAllByEmail(user.getEmail());

        String otpCode = generateOtp();
        LocalDateTime now = LocalDateTime.now();
        Otp otp = Otp.builder()
                .email(user.getEmail())
                .otpCode(otpCode)
                .creationTime(now)
                .expiryTime(now.plusMinutes(OTP_VALIDITY_MINUTES))
                .isUsed(false)
                .build();
        otpRepository.save(otp);

        // Step 4: Send OTP via RabbitMQ
        NotificationDto notificationDto = new NotificationDto(
                user.getEmail(),
                "Your OTP for Scheduler Login",
                String.format("Your 4-digit OTP is: %s. This OTP is valid for %d minutes.", otpCode, OTP_VALIDITY_MINUTES)
        );
        notificationMessageProducer.sendNotification(notificationDto);

        // Return a response indicating OTP is required for non-admin roles
        return LoginResponseDto.builder()
                .message("OTP sent to your email. Please verify.")
                .otpRequired(true)
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public LoginResponseDto verifyOtp(OtpRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found for OTP verification."));

        // Although an admin should not reach this point, add a check for robustness
        if (user.getRole() == User.Role.ADMIN) {
            throw new BadCredentialsException("OTP verification is not applicable for admin users.");
        }

        Optional<Otp> latestOtpOptional = otpRepository.findFirstByEmailAndIsUsedFalseAndExpiryTimeAfterOrderByCreationTimeDesc(
                dto.getEmail(), LocalDateTime.now());

        if (latestOtpOptional.isEmpty()) {
            throw new BadCredentialsException("No valid OTP found or OTP has expired.");
        }

        Otp otp = latestOtpOptional.get();

        if (!otp.getOtpCode().equals(dto.getOtp())) {
            throw new BadCredentialsException("Invalid OTP.");
        }

        otp.setUsed(true);
        otpRepository.save(otp);

        String jwtToken = jwtService.generateToken(user.getEmail(), user.getRole().name());


        return LoginResponseDto.builder()
                .message("Logged In Successfully!")
                .role(user.getRole())
                .token(jwtToken)
                .otpRequired(false)
                .email(user.getEmail())
                .build();
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }

    public static final String ADMIN_EMAIL = "admin@gmail.com";
    public static final String ADMIN_RAW_PASSWORD = "admin123";
}