package com.example.SchedulerW4.services;

import com.example.SchedulerW4.dtos.auth_dtos.ProviderRegistrationDto;
import com.example.SchedulerW4.entities.Provider;

import java.util.List;

public interface ProviderService {

    Provider registerProvider (ProviderRegistrationDto dto);

    List<Provider> getAllProviders();

    Provider findById (Long id);

}
