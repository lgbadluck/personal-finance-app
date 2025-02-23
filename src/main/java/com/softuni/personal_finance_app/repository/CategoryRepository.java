package com.softuni.personal_finance_app.repository;

import com.softuni.personal_finance_app.enitity.Category;
import com.softuni.personal_finance_app.enitity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByNameAndCategoryOwner(String categoryName, User categoryOwner);

}
