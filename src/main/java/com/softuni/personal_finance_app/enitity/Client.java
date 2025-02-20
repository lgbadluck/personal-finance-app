package com.softuni.personal_finance_app.enitity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column(unique = true)
    private String email;


    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm")
    @CreationTimestamp
    private LocalDateTime createdOn;

    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm")
    @UpdateTimestamp
    private LocalDateTime updatedOn;

    @OneToOne(mappedBy = "client")
    private User user;
}
