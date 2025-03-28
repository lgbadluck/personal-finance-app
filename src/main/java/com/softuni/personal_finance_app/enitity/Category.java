package com.softuni.personal_finance_app.enitity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JsonIgnore // Exclude from serialization
    private User categoryOwner;

    @ManyToMany(mappedBy = "categories")
    @OrderBy("createdOn DESC")
    @JsonIgnore // Exclude from serialization
    private List<Budget> budgets;

    @OneToMany(mappedBy = "category", fetch = FetchType.EAGER)
    @OrderBy("datetimeOfExpense DESC, createdOn DESC")
    @JsonIgnore // Exclude from serialization
    private List<Expense> expenses;

    @CreationTimestamp
    private LocalDateTime createdOn;

    @UpdateTimestamp
    private LocalDateTime updatedOn;
}
