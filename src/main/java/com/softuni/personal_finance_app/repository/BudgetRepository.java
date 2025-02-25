package com.softuni.personal_finance_app.repository;

import com.softuni.personal_finance_app.enitity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

}
