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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private NotificationService notificationService;
    @Mock
    private List<String> getDefaultCategories;

    @InjectMocks
    private UserService userService;



    @Test
    void whenExistingExpensesForUser_thenReturnAllExpense() {

        // Given
        Category category1 = Category.builder()
                .name("Category 1")
                .build();
        Category category2 = Category.builder()
                .name("Category 2")
                .build();

        Expense expense1 = Expense.builder()
                .category(category1)
                .amount(BigDecimal.valueOf(10))
                .datetimeOfExpense(LocalDateTime.now())
                .build();
        Expense expense2 = Expense.builder()
                .category(category2)
                .amount(BigDecimal.valueOf(20))
                .datetimeOfExpense(LocalDateTime.now())
                .build();
        Expense expense3 = Expense.builder()
                .category(category2)
                .amount(BigDecimal.valueOf(30))
                .datetimeOfExpense(LocalDateTime.now())
                .build();

        category1.setExpenses(List.of(expense1));
        category2.setExpenses(List.of(expense2, expense3));

        User user = User.builder()
                .categories(List.of(category1, category2))
                .build();

        // When && Then
        assertThat(userService.getAllExpensesByUser(user)).hasSize(3);

    }

    @Test
    void whenExistingUsers_thenReturnAllUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User(), new User()));

        // When & Then
        assertThat(userService.getAllUsers()).hasSize(3);

    }
    @Test
    void givenExistingUsername_whenRegisterUser_thenExceptionIsThrown() {

        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(new User()));

        // When & Then
        assertThrows(UsernameAlreadyExistException.class, () -> userService.registerUser(registerRequest));
        verify(userRepository, never()).save(any());
        verify(notificationService, never()).saveNotificationPreference(any(UUID.class), anyBoolean(), anyString());
    }

    @Test
    void givenHappyPath_whenRegisterUser() {

        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        UUID userId = user.getId();

        userService.getDefaultCategories = List.of(
                "Groceries", "Utilities(bills)", "Transportation", "Dining out",
                "Entertainment", "Shopping", "Travel", "Education");

        List<Category> listCategories = userService.getDefaultCategories
                .stream()
                .map(string -> Category.builder()
                        .name(string)
                        .build())
                .toList();

        user.setCategories(listCategories);

        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);

        // When
        User registeredUser = userService.registerUser(registerRequest);

        // Then
        assertThat(user.getCategories()).hasSize(8);
        verify(notificationService, times(1)).saveNotificationPreference(any(), any(boolean.class), any());
    }


    @Test
    void givenUserWithStatusActive_whenSwitchStatus_thenUserStatusBecomeInactive() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(true)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        userService.switchStatus(user.getId());

        // Then
        assertFalse(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenUserWithStatusInactive_whenSwitchStatus_thenUserStatusBecomeActive() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(false)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        userService.switchStatus(user.getId());

        // Then
        assertTrue(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenMissingUserFromDatabase_whenLoadUserByUsername_thenExceptionIsThrown() {

        // Given
        String username = "Vik123";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DomainException.class, () -> userService.loadUserByUsername(username));
    }

    @Test
    void givenExistingUser_whenLoadUserByUsername_thenReturnCorrectAuthenticationMetadata() {

        // Given
        String username = "Vik123";
        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(true)
                .password("123123")
                .role(Role.ADMIN)
                .build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        UserDetails authenticatedUserDetails = userService.loadUserByUsername(username);

        // Then
        assertInstanceOf(AuthenticatedUserDetails.class, authenticatedUserDetails);
        AuthenticatedUserDetails result = (AuthenticatedUserDetails) authenticatedUserDetails;
        assertEquals(user.getId(), result.getUserId());
        assertEquals(username, result.getUsername());
        assertEquals(user.getPassword(), result.getPassword());
        assertEquals(user.isActive(), result.isActive());
        assertEquals(user.getRole(), result.getRole());
        assertThat(result.getAuthorities()).hasSize(1);
        assertEquals("ROLE_ADMIN", result.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void givenUser_initializeData() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .client(Client.builder()
                        .email(null)
                        .build())
                .build();
        UUID userId = user.getId();

        userService.getDefaultCategories = List.of(
                "Groceries", "Utilities(bills)", "Transportation", "Dining out",
                "Entertainment", "Shopping", "Travel", "Education");

        List<Category> listCategories = userService.getDefaultCategories
                .stream()
                .map(string -> Category.builder()
                        .name(string)
                        .build())
                .toList();

        user.setCategories(listCategories);

        // When
        userService.initializeUser(user);

        // Then
        assertEquals(userId, user.getId());
        assertThat(user.getCategories()).hasSize(8);
        verify(userRepository, times(1)).save(user);
        verify(notificationService, times(1)).saveNotificationPreference(userId, false, null);
    }

    @Test
    void givenUserWithRoleUser_whenSwitchRole_thenUserReceivesAdminRole() {

        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(Role.USER)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.switchRole(userId);

        // Then
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void givenUserWithRoleAdmin_whenSwitchRole_thenUserReceivesUserRole() {

        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(Role.ADMIN)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.switchRole(userId);

        // Then
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void givenExistingUser_whenEditTheirProfileWithActualEmail_thenChangeTheirDetailsSaveNotificationPreferenceAndSaveToDatabase() {

        // Given

        UUID userId = UUID.randomUUID();
        ClientEditRequest dto = ClientEditRequest.builder()
                .firstName("Viktor")
                .lastName("Aleksandrov")
                .email("vik123@abv.bg")
                .build();

        User user = User.builder()
                .id(userId)
                .client(Client.builder()
                        .firstName("Viktor")
                        .lastName("Aleksandrov")
                        .email("vik123@abv.bg")
                        .build())
                .build();
        //when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.editClientDetails(user, dto);

        // Then
        assertEquals("Viktor", user.getClient().getFirstName());
        assertEquals("Aleksandrov", user.getClient().getLastName());
        assertEquals("vik123@abv.bg", user.getClient().getEmail());
        verify(notificationService, times(1)).saveNotificationPreference(userId, true, dto.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenExistingUser_whenEditTheirProfileWithEmptyEmail_thenChangeTheirDetailsSaveNotificationPreferenceAndSaveToDatabase() {

        // Given
        UUID userId = UUID.randomUUID();
        ClientEditRequest dto = ClientEditRequest.builder()
                .firstName("Viktor")
                .lastName("Aleksandrov")
                .email("")
                .build();
        User user = User.builder()
                .id(userId)
                .client(Client.builder()
                        .firstName("Viktor")
                        .lastName("Aleksandrov")
                        .email("")
                        .build())
                .build();
        //when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.editClientDetails(user, dto);

        // Then
        assertEquals("Viktor", user.getClient().getFirstName());
        assertEquals("Aleksandrov", user.getClient().getLastName());
        assertEquals("", user.getClient().getEmail());
        verify(notificationService, times(1)).saveNotificationPreference(userId, false, "");
        verify(userRepository, times(1)).save(user);
    }

}
