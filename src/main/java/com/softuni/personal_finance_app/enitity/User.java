package com.softuni.personal_finance_app.enitity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean isActive;

    @CreationTimestamp
    private LocalDateTime createdOn;

    @UpdateTimestamp
    private LocalDateTime updatedOn;

    @OneToOne
    private Client client;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "categoryOwner") //, cascade = CascadeType.ALL)
    @OrderBy("createdOn ASC")
    private List<Category> categories;

    @ManyToMany(fetch = FetchType.EAGER) //, cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_budget",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "budget_id"))
    @OrderBy("status ASC, createdOn DESC")
    private List<Budget> budgets = new ArrayList<>();

    // Other fields and methods
    public void addBudget(Budget budget) {

        budgets.add(budget);
        budget.getUsers().add(this);
    }
}