package com.softuni.personal_finance_app.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryRequest {

    @NotNull(message = "Category name is required")
    @Size(max = 40, message = "Category name cannot exceed 40 characters")
    private String name;

    @NotNull(message = "Description is required")
    @Size(max = 300, message = "Description cannot exceed 300 characters")
    private String description;


}
