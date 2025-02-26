package com.softuni.personal_finance_app.enitity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
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
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal maxToSpend;

    private BigDecimal spent;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BudgetType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BudgetStatus status;

    @Column(nullable = false)
    private boolean isRenewed;

    @ManyToMany
    @JoinTable(
            name = "budget_category",
            joinColumns = @JoinColumn(name = "budget_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    @OrderBy("createdOn ASC")
    private List<Category> categories;

    @ManyToMany(mappedBy = "budgets", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdOn;

    @UpdateTimestamp
    private LocalDateTime updatedOn;

    // Other fields and methods
    public void addUser(User user) {

        users.add(user);
        user.getBudgets().add(this);
    }
}
