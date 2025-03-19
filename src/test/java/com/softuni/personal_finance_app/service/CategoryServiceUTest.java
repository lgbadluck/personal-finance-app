package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.enitity.Category;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.exception.DomainException;
import com.softuni.personal_finance_app.repository.CategoryRepository;
import com.softuni.personal_finance_app.web.dto.CategoryRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceUTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void givenExistingCategoryInDatabase_whenSaveCategory_thenExceptionIsThrown() {

        // Given
        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName("Transportation");
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("Vik123")
                .build();
        Category category = Category.builder()
                .name(categoryRequest.getName()).build();

        when(categoryRepository.findByNameAndCategoryOwner(categoryRequest.getName(), user)).thenReturn(Optional.of(category));

        // When & Then
        assertThrows(DomainException.class, () -> categoryService.saveCategory(categoryRequest, user));
    }

    @Test
    void happyPath_whenSaveCategory() {

        // Given
        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName("Transportation");
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("Vik123")
                .build();
        Category category = Category.builder()
                .name(categoryRequest.getName()).build();

        // When
        when(categoryRepository.findByNameAndCategoryOwner(categoryRequest.getName(), user)).thenReturn(Optional.empty());

        categoryService.saveCategory(categoryRequest, user);

        // Then
        verify(categoryRepository, times(1)).save(any(Category.class));
    }
}
