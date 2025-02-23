package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.enitity.Category;
import com.softuni.personal_finance_app.enitity.Expense;
import com.softuni.personal_finance_app.enitity.Role;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.exception.DomainException;
import com.softuni.personal_finance_app.repository.CategoryRepository;
import com.softuni.personal_finance_app.repository.ExpenseRepository;
import com.softuni.personal_finance_app.repository.UserRepository;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    private final List<String> getDefaultCategories;

    @Autowired
    public UserService(UserRepository userRepository,
                       ExpenseRepository expenseRepository,
                       CategoryRepository categoryRepository,
                       PasswordEncoder passwordEncoder,
                       List<String> getDefaultCategories) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.getDefaultCategories = getDefaultCategories;
    }

    @Transactional
    public User registerUser(RegisterRequest registerRequest) {

        Optional<User> userOptional = userRepository.findByUsername(registerRequest.getUsername());

        if(userOptional.isPresent()) {
            throw new RuntimeException("Username already exists: [%s]".formatted(registerRequest.getUsername()));
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.USER)
                .isActive(true)
                .build();

        return initializeUser(user);
    }

    private void addDefaultCategoriesToUser(User user) {
        //Add default categories for this user
        for (String categoryName : getDefaultCategories) {
            Category category = Category.builder()
                    .name(categoryName)
                    .description("Expenses for " + categoryName)
                    .categoryOwner(user)
                    .build();
            categoryRepository.save(category);
        }
    }

    @Transactional
    public User initializeUser(User user) {

        userRepository.save(user);
        addDefaultCategoriesToUser(user);

        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new DomainException("User with this username does not exist."));

        return new AuthenticatedUserDetails(user.getId(), username, user.getPassword(), user.getRole(), user.isActive());
    }

    public User getById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new DomainException("No user found with id [%s]".formatted(userId)));
    }

    public List<Expense> getAllExpensesByUser(User user) {

        List<Expense> expenseList = new ArrayList<>();

        for (Category category : user.getCategories()) {
            expenseList.addAll(category.getExpenses());
            
        }
        return expenseList;
    }
}
