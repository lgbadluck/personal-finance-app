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
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean isActive;

    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm")
    @CreationTimestamp
    private LocalDateTime createdOn;

    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm")
    @UpdateTimestamp
    private LocalDateTime updatedOn;

    @OneToOne
    private Client client;

    @OneToMany(mappedBy = "categoryOwner")
    private List<Category> categories;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_budget",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "budget_id"))
    private List<Budget> budgets;
}
