package com.example.SchedulerW4.services;

import com.example.SchedulerW4.dtos.auth_dtos.ProviderRegistrationDto;
import com.example.SchedulerW4.entities.Provider;
import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.repositories.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService{

    //Inject Repository
    private final ProviderRepository providerRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Provider registerProvider(ProviderRegistrationDto dto) {

        Provider provider = Provider.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(User.Role.PROVIDER)
                .specialization(dto.getSpecialization())
                .build();

        return providerRepository.save(provider);
    }

    @Override
    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
    }

    @Override
    public Provider findById(Long id) {
        return providerRepository.findById(id).orElseThrow(() -> new RuntimeException("Provider Not Found With ID: " + id));
    }
}
