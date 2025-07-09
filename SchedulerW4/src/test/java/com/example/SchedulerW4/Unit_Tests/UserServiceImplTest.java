package com.example.SchedulerW4.Unit_Tests;

import com.example.SchedulerW4.dtos.auth_dtos.UserRegistrationDto;
import com.example.SchedulerW4.dtos.slot_dtos.SlotResponseDto;
import com.example.SchedulerW4.entities.Provider;
import com.example.SchedulerW4.entities.Slot;
import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.repositories.SlotRepository;
import com.example.SchedulerW4.repositories.UserRepository;
import com.example.SchedulerW4.services.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private SlotRepository slotRepository;
    @Mock private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl userService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_shouldEncodePasswordAndSave() {
        UserRegistrationDto dto = new UserRegistrationDto("Alice", "alice@email.com", "password123");
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPass");

        User savedUser = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@email.com")
                .password("encodedPass")
                .role(User.Role.USER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPassword()).isEqualTo("encodedPass");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void findById_validId_shouldReturnUser() {
        User user = User.builder().id(1L).name("Test").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test");
    }

    @Test
    void findById_invalidId_shouldThrow() {
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(100L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User Not Found With ID");
    }

    @Test
    void findByEmail_validEmail_shouldReturnUser() {
        User user = User.builder().email("user@mail.com").build();
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmail("user@mail.com");

        assertThat(result.getEmail()).isEqualTo("user@mail.com");
    }

    @Test
    void findByEmail_invalidEmail_shouldThrow() {
        when(userRepository.findByEmail("missing@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("missing@mail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User Not Found With Email");
    }

    @Test
    void getAllUsers_shouldReturnUserList() {
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));

        List<User> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
    }

    @Test
    void getAllSlots_shouldReturnMappedDTOs() {
        Slot slot = Slot.builder()
                .id(10L)
                .isBooked(true)
                .startTime(LocalDateTime.of(2025, 1, 1, 10, 0))
                .endTime(LocalDateTime.of(2025, 1, 1, 11, 0))
                .provider(Provider.builder().id(5L).name("Dr. Who").specialization("Time Travel").build())
                .build();

        when(slotRepository.findAll()).thenReturn(List.of(slot));

        List<SlotResponseDto> result = userService.getAllSlots();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlotId()).isEqualTo(10L);
        assertThat(result.get(0).getProviderName()).isEqualTo("Dr. Who");
    }

    @Test
    void getAllAvailableSlots_shouldReturnMappedDTOs() {
        Slot slot = Slot.builder()
                .id(20L)
                .isBooked(false)
                .startTime(LocalDateTime.of(2025, 2, 2, 15, 0))
                .endTime(LocalDateTime.of(2025, 2, 2, 16, 0))
                .provider(Provider.builder().id(7L).name("Dr. Strange").specialization("Mystic Arts").build())
                .build();

        when(slotRepository.findByIsBooked(false)).thenReturn(List.of(slot));

        List<SlotResponseDto> result = userService.getAllAvailableSlots();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isBooked()).isFalse();
        assertThat(result.get(0).getProviderName()).isEqualTo("Dr. Strange");
    }
}
