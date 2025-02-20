package com.softuni.personal_finance_app.init;

import com.softuni.personal_finance_app.enitity.Client;
import com.softuni.personal_finance_app.enitity.Role;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.repository.ClientRepository;
import com.softuni.personal_finance_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    @Autowired
    public DataInitializer(PasswordEncoder passwordEncoder,
                           UserRepository userRepository,
                           ClientRepository clientRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public void run(String... args) throws Exception {
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

            userRepository.save(adminUser);
            System.out.println("Default admin user created.");
        }
    }
}

