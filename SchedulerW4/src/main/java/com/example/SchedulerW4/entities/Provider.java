package com.example.SchedulerW4.entities;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder

@Entity
@Table(name = "providers")
public class Provider extends User {

    private String specialization;

    @OneToMany(mappedBy = "provider")
    private List<Slot> slots;

    @OneToMany(mappedBy = "provider")
    private List<Appointment> appointments;

}
