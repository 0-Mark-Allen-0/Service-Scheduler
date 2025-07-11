package com.example.SchedulerW4;

import com.example.SchedulerW4.dtos.*;
import com.example.SchedulerW4.dtos.appointment_dtos.*;
import com.example.SchedulerW4.dtos.auth_dtos.*;
import com.example.SchedulerW4.dtos.slot_dtos.*;
import com.example.SchedulerW4.entities.*;
import com.example.SchedulerW4.repositories.*;
import com.example.SchedulerW4.schedulers.SlotCleanup;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SchedulerIntegrationTest {

    // 🐘 PostgreSQL
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    // 🐇 RabbitMQ
    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.13-management");

    // 🔁 Redis (uses generic container)
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.2")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", () -> rabbitMQ.getMappedPort(5672));

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @LocalServerPort
    private int port;

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private SlotRepository slotRepository;
    @Autowired private SlotCleanup slotCleanup;
    @Autowired private OtpRepository otpRepository; // Autowire OtpRepository to fetch OTP

    private final RestTemplate restTemplate = new RestTemplate();

    private static String jwtUser;
    private static String jwtProvider;
    private static Long slotId;
    private static Long appointmentId;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test @Order(1)
    void registerUserAndProvider() {
        var userReq = new UserRegistrationDto("Test User", "user@example.com", "userpass");
        var providerReq = new ProviderRegistrationDto("Dr. John", "provider@example.com", "providerpass", "Cardiology");

        ResponseEntity<?> userResp = restTemplate.postForEntity(baseUrl() + "/auth/register/user", userReq, Object.class);
        ResponseEntity<?> providerResp = restTemplate.postForEntity(baseUrl() + "/auth/register/provider", providerReq, Object.class);

        assertThat(userResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(providerResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(2)
    void loginAndExtractJwtTokens() {
        var userLogin = new LoginRequestDto("user@example.com", "userpass");
        var providerLogin = new LoginRequestDto("provider@example.com", "providerpass");

        // --- User Login and OTP Verification ---
        ResponseEntity<LoginResponseDto> userLoginResp = null;
        try {
            userLoginResp = restTemplate.postForEntity(baseUrl() + "/auth/login", userLogin, LoginResponseDto.class);
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error during user login: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            fail("User login failed with HTTP error: " + e.getResponseBodyAsString());
        }
        assertThat(userLoginResp).isNotNull();
        assertThat(userLoginResp.getBody()).isNotNull();
        assertThat(userLoginResp.getBody().isOtpRequired()).isTrue(); // Expect OTP to be required

        // Fetch OTP for user from the database
        Optional<Otp> userOtpOptional = otpRepository.findFirstByEmailAndIsUsedFalseAndExpiryTimeAfterOrderByCreationTimeDesc(
                userLogin.getEmail(), LocalDateTime.now());
        assertThat(userOtpOptional).isPresent();
        String userOtpCode = userOtpOptional.get().getOtpCode();

        // Verify OTP for user
        var userOtpReq = new OtpRequestDto(userLogin.getEmail(), userOtpCode);
        ResponseEntity<LoginResponseDto> userOtpVerifyResp = null;
        try {
            userOtpVerifyResp = restTemplate.postForEntity(baseUrl() + "/auth/verify-otp", userOtpReq, LoginResponseDto.class);
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error during user OTP verification: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            fail("User OTP verification failed with HTTP error: " + e.getResponseBodyAsString());
        }
        assertThat(userOtpVerifyResp).isNotNull();
        assertThat(userOtpVerifyResp.getBody()).isNotNull();
        assertThat(userOtpVerifyResp.getBody().isOtpRequired()).isFalse(); // OTP no longer required
        jwtUser = userOtpVerifyResp.getBody().getToken();
        assertThat(jwtUser).isNotBlank();


        // --- Provider Login and OTP Verification ---
        ResponseEntity<LoginResponseDto> providerLoginResp = null;
        try {
            providerLoginResp = restTemplate.postForEntity(baseUrl() + "/auth/login", providerLogin, LoginResponseDto.class);
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error during provider login: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            fail("Provider login failed with HTTP error: " + e.getResponseBodyAsString());
        }
        assertThat(providerLoginResp).isNotNull();
        assertThat(providerLoginResp.getBody()).isNotNull();
        assertThat(providerLoginResp.getBody().isOtpRequired()).isTrue(); // Expect OTP to be required

        // Fetch OTP for provider from the database
        Optional<Otp> providerOtpOptional = otpRepository.findFirstByEmailAndIsUsedFalseAndExpiryTimeAfterOrderByCreationTimeDesc(
                providerLogin.getEmail(), LocalDateTime.now());
        assertThat(providerOtpOptional).isPresent();
        String providerOtpCode = providerOtpOptional.get().getOtpCode();

        // Verify OTP for provider
        var providerOtpReq = new OtpRequestDto(providerLogin.getEmail(), providerOtpCode);
        ResponseEntity<LoginResponseDto> providerOtpVerifyResp = null;
        try {
            providerOtpVerifyResp = restTemplate.postForEntity(baseUrl() + "/auth/verify-otp", providerOtpReq, LoginResponseDto.class);
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error during provider OTP verification: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            fail("Provider OTP verification failed with HTTP error: " + e.getResponseBodyAsString());
        }
        assertThat(providerOtpVerifyResp).isNotNull();
        assertThat(providerOtpVerifyResp.getBody()).isNotNull();
        assertThat(providerOtpVerifyResp.getBody().isOtpRequired()).isFalse(); // OTP no longer required
        jwtProvider = providerOtpVerifyResp.getBody().getToken();
        assertThat(jwtProvider).isNotBlank();
    }

    @Test @Order(3)
    void providerCreatesSlot() {
        // Ensure the slot is created in the future to avoid "Cannot book a slot in the past" error
        var requestDto = SlotRequestDto.builder().startTime(LocalDateTime.now().plusHours(1)).build();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtProvider);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SlotRequestDto> request = new HttpEntity<>(requestDto, headers);
        ResponseEntity<?> response = restTemplate.postForEntity(baseUrl() + "/providers/slots/add", request, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Fetch the created slot ID from the database
        // It's safer to query for the slot created by the specific provider or within a time range.
        // For simplicity and given the ordered tests, we'll assume it's the latest one created.
        slotId = slotRepository.findAll().stream()
                .max(Comparator.comparing(Slot::getStartTime)) // Find the latest created slot
                .orElseThrow(() -> new AssertionError("No slot found after creation"))
                .getId();
    }

    @Test @Order(4)
    void userBooksSlot() {
        Long providerId = slotRepository.findById(slotId).get().getProvider().getId();
        var dto = AppointmentRequestDto.builder().slotId(slotId).providerId(providerId).build();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtUser);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AppointmentRequestDto> request = new HttpEntity<>(dto, headers);
        // Change response type to BookingResponseDto to capture the appointmentId
        ResponseEntity<BookingResponseDto> response = restTemplate.postForEntity(baseUrl() + "/users/appointment/book", request, BookingResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED); // Expect 200 OK
        assertThat(response.getBody()).isNotNull();
        // Assert that the booking was successful (either BOOKED or QUEUED, both create an appointment record)
        assertThat(response.getBody().getStatus()).isIn(BookingResponseDto.BookingStatus.BOOKED, BookingResponseDto.BookingStatus.QUEUED);
        assertThat(response.getBody().getAppointment()).isNotNull(); // Ensure the nested appointment DTO is present

        // Extract the appointmentId directly from the response body
        appointmentId = response.getBody().getAppointment().getAppointmentId();
        assertThat(appointmentId).isNotNull(); // Ensure the extracted ID is not null
    }

    @Test @Order(5)
    void userCancelsAppointment() {
        var cancelDto = new AppointmentCancelDto(appointmentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtUser);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AppointmentCancelDto> request = new HttpEntity<>(cancelDto, headers);
        ResponseEntity<MessageDto> response = restTemplate.exchange(baseUrl() + "/users/appointment/cancel", HttpMethod.DELETE, request, MessageDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(6)
    void scheduledCleanupShouldDeleteSlotAndAppointment() throws InterruptedException {
        Thread.sleep(12_000); // Give time for async tasks (if any)

        // 🔁 Manually expire the slot by setting its endTime in the past
        slotRepository.findById(slotId).ifPresent(slot -> {
            slot.setEndTime(LocalDateTime.now().minusMinutes(1));
            slotRepository.save(slot);
        });

        // 🚮 Trigger the cleanup manually
        slotCleanup.cleanUp();

        // ✅ Assert both records are deleted
        assertThat(appointmentRepository.findById(appointmentId)).isEmpty();
        assertThat(slotRepository.findById(slotId)).isEmpty();
    }

}