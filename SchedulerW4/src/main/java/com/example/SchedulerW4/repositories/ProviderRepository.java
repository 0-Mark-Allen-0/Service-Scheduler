package com.example.SchedulerW4.repositories;

import com.example.SchedulerW4.entities.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderRepository extends JpaRepository<Provider, Long> {

    //For ADMIN
    Page<Provider> findByNameContainingIgnoreCaseOrSpecializationContainingIgnoreCase (
            String name,
            String specialization,
            Pageable pageable
    );
}
