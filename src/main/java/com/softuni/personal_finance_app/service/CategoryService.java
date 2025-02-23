package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.enitity.Category;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.exception.DomainException;
import com.softuni.personal_finance_app.repository.CategoryRepository;
import com.softuni.personal_finance_app.web.dto.CategoryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void saveCategory(CategoryRequest categoryRequest, User user) {

        String categoryName = categoryRequest.getName();
        String categoryDescription = categoryRequest.getDescription();

        Optional<Category> optionalCategory = categoryRepository.findByNameAndCategoryOwner(categoryName, user);

        if (optionalCategory.isPresent()) {
            throw new DomainException("User with id: [%s] already has category with name: [%s].".formatted(user.getId().toString(), categoryName));
        }

        Category category = Category.builder()
                .name(categoryName)
                .description(categoryDescription)
                .categoryOwner(user)
                .build();

        categoryRepository.save(category);
    }
}