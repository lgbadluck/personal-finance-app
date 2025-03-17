package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.enitity.*;
import com.softuni.personal_finance_app.exception.DomainException;
import com.softuni.personal_finance_app.exception.UsernameAlreadyExistException;
import com.softuni.personal_finance_app.repository.CategoryRepository;
import com.softuni.personal_finance_app.repository.ClientRepository;
import com.softuni.personal_finance_app.repository.UserRepository;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.web.dto.ClientEditRequest;
import com.softuni.personal_finance_app.web.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    private final List<String> getDefaultCategories;

    @Autowired
    public UserService(UserRepository userRepository,
                       ClientRepository clientRepository,
                       CategoryRepository categoryRepository,
                       PasswordEncoder passwordEncoder,
                       NotificationService notificationService,
                       List<String> getDefaultCategories) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.getDefaultCategories = getDefaultCategories;
    }

    @Transactional
    public User registerUser(RegisterRequest registerRequest) {

        Optional<User> userOptional = userRepository.findByUsername(registerRequest.getUsername());

        if(userOptional.isPresent()) {
            throw new UsernameAlreadyExistException("Username already exists: [%s]".formatted(registerRequest.getUsername()));
        }

        Client client  = Client.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .build();

        clientRepository.save(client);

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.USER)
                .isActive(true)
                .client(client)
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

        // Persist new notification preference with isEnabled = false
        notificationService.saveNotificationPreference(user.getId(), false, user.getClient().getEmail());

        log.info("Successfully create new user account for username [%s] and id [%s]".formatted(user.getUsername(), user.getId()));

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

        // Sort by Expense Date in DESC order
        expenseList.sort(Comparator.comparing(Expense::getDatetimeOfExpense).reversed());

        return expenseList;
    }

    public void editClientDetails(User user, ClientEditRequest clientEditRequest) {

        if (clientEditRequest.getEmail().isBlank()) {
            notificationService.saveNotificationPreference(user.getId(), false, clientEditRequest.getEmail());
        }

        user.getClient().setFirstName(clientEditRequest.getFirstName());
        user.getClient().setLastName(clientEditRequest.getLastName());
        user.getClient().setEmail(clientEditRequest.getEmail());

        if (!clientEditRequest.getEmail().isBlank()) {
            notificationService.saveNotificationPreference(user.getId(), true, clientEditRequest.getEmail());
        }

        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void switchStatus(UUID userId) {

        User user = getById(userId);

        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public void switchRole(UUID userId) {

        User user = getById(userId);

        if (user.getRole() == Role.USER) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(Role.USER);
        }

        userRepository.save(user);
    }
}
