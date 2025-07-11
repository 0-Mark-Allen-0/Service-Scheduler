package com.example.SchedulerW4.repositories;

import com.example.SchedulerW4.entities.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    // Find the latest valid OTP for a given email
    Optional<Otp> findFirstByEmailAndIsUsedFalseAndExpiryTimeAfterOrderByCreationTimeDesc(
            String email, LocalDateTime currentTime);

    // To clean up all OTPs for a user after successful login or on new OTP generation
    void deleteAllByEmail(String email);
}