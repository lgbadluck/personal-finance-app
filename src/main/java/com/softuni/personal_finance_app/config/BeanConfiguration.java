package com.softuni.personal_finance_app.config;

import com.softuni.personal_finance_app.enitity.Category;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class BeanConfiguration {

    private static final List<String> DEFAULT_CATEGORIES_NAMES = List.of(
            "Groceries", "Utilities(bills)", "Transportation", "Dining out",
            "Entertainment", "Shopping", "Travel", "Education");

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public List<String> getDefaultCategories() {

        return DEFAULT_CATEGORIES_NAMES;
    }
}