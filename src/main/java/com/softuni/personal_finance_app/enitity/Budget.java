package com.softuni.personal_finance_app.enitity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
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
    private List<Category> categories;

    @ManyToMany(mappedBy = "budgets")
    private List<User> users;
}
