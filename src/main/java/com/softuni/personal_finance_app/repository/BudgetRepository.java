package com.softuni.personal_finance_app.repository;

import com.softuni.personal_finance_app.enitity.Budget;
import com.softuni.personal_finance_app.enitity.BudgetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByStatus(BudgetStatus budgetStatus);

}
