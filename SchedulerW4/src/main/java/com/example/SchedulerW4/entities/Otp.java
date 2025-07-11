package com.example.SchedulerW4.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "otps")
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email; // The email associated with this OTP

    @Column(nullable = false)
    private String otpCode; // The actual 4-digit OTP

    @Column(nullable = false)
    private LocalDateTime creationTime;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    @Column(nullable = false)
    private boolean isUsed; // To mark OTP as used after successful verification (optional, can delete instead)
}