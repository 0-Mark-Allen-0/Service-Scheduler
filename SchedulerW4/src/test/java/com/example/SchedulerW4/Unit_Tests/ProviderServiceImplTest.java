package com.example.SchedulerW4.Unit_Tests;

import com.example.SchedulerW4.dtos.auth_dtos.ProviderRegistrationDto;
import com.example.SchedulerW4.entities.Provider;
import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.repositories.ProviderRepository;
import com.example.SchedulerW4.services.ProviderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProviderServiceImplTest {

    @Mock private ProviderRepository providerRepository;
    @Mock private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks private ProviderServiceImpl providerService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerProvider_shouldEncodePasswordAndSave() {
        ProviderRegistrationDto dto = new ProviderRegistrationDto("Dr. Smith", "dr@example.com", "rawpass", "Cardiology");

        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedpass");

        Provider saved = Provider.builder()
                .id(1L)
                .name("Dr. Smith")
                .email("dr@example.com")
                .password("encodedpass")
                .specialization("Cardiology")
                .role(User.Role.PROVIDER)
                .build();

        when(providerRepository.save(any(Provider.class))).thenReturn(saved);

        Provider result = providerService.registerProvider(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPassword()).isEqualTo("encodedpass");
        verify(passwordEncoder).encode("rawpass");
        verify(providerRepository).save(any(Provider.class));
    }

    @Test
    void getAllProviders_shouldReturnList() {
        Provider p1 = Provider.builder().id(1L).name("Dr. A").build();
        Provider p2 = Provider.builder().id(2L).name("Dr. B").build();

        when(providerRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Provider> result = providerService.getAllProviders();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Dr. A");
    }

    @Test
    void findById_validId_shouldReturnProvider() {
        Provider provider = Provider.builder().id(1L).name("Dr. A").build();
        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));

        Provider result = providerService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Dr. A");
    }

    @Test
    void findById_invalidId_shouldThrowException() {
        when(providerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> providerService.findById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Provider Not Found");
    }
}
