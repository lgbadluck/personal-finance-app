package com.softuni.personal_finance_app.init;

import com.softuni.personal_finance_app.enitity.Client;
import com.softuni.personal_finance_app.enitity.Role;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.repository.ClientRepository;
import com.softuni.personal_finance_app.repository.UserRepository;
import com.softuni.personal_finance_app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;


    @Autowired
    public DataInitializer(PasswordEncoder passwordEncoder,
                           UserService userService,
                           UserRepository userRepository,
                           ClientRepository clientRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {

            Client adminClient = Client.builder()
                    .firstName("firstADMIN")
                    .lastName("lastADMIN")
                    .email("admin@admin.com")
                    .build();

            clientRepository.save(adminClient);

            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .isActive(true)
                    .role(Role.ADMIN)
                    .client(adminClient)
                    .build();

            userService.initializeUser(adminUser);
            System.out.println("Default admin user created.");
        }
    }
}
