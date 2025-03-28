package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.enitity.*;
import com.softuni.personal_finance_app.exception.DomainException;
import com.softuni.personal_finance_app.repository.BudgetRepository;
import com.softuni.personal_finance_app.repository.CategoryRepository;
import com.softuni.personal_finance_app.repository.InvitationRepository;
import com.softuni.personal_finance_app.repository.UserRepository;
import com.softuni.personal_finance_app.web.dto.BudgetRequest;
import com.softuni.personal_finance_app.web.dto.ShareBudgetRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceUTest {

    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BudgetService budgetService;

    @Test
    void happyPath_whenCheckCompletedBudgetStatus_WithRenew() {

        // Given
        Budget budget1 = Budget.builder()
                .id(UUID.randomUUID())
                .status(BudgetStatus.ACTIVE)
                .type(BudgetType.WEEK)
                .isRenewed(false)
                .createdOn(LocalDateTime.now().minusDays(1))
                .build();
        Budget budget2 = Budget.builder()
                .id(UUID.randomUUID())
                .status(BudgetStatus.ACTIVE)
                .type(BudgetType.MONTH)
                .isRenewed(false)
                .createdOn(LocalDateTime.now().minusMonths(1).minusDays(1))
                .build();
        // Renewed Budget has to have Users and Categories for renewBudget() call
        Budget budget3 = Budget.builder()
                .id(UUID.randomUUID())
                .status(BudgetStatus.ACTIVE)
                .type(BudgetType.YEAR)
                .isRenewed(true)
                .categories(List.of(
                        Category.builder()
                                .name("Category 1")
                                .build()
                ))
                .createdOn(LocalDateTime.now().minusYears(1).minusDays(1))
                .build();
        List<Budget> userBudgets = new ArrayList<>();
        userBudgets.add(budget3);
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .budgets(userBudgets)
                .build();
        budget3.setUsers(List.of(user));
        List<Budget> activeBudgets = List.of(budget3, budget2, budget1);

        when(budgetRepository.findByStatus(BudgetStatus.ACTIVE)).thenReturn(activeBudgets);
        //doNothing().when(budgetService).renewBudget(any());

        // When
        budgetService.checkCompletedBudgetStatus();

        // Then
        assertEquals(BudgetStatus.ACTIVE, budget1.getStatus());
        assertEquals(BudgetStatus.COMPLETED, budget2.getStatus());
        assertEquals(BudgetStatus.COMPLETED, budget3.getStatus());
        verify(budgetRepository, times(3)).save(any(Budget.class));
    }

    @Test
    void happyPath_whenUpdateBudgetSpendingForUser() {

        // Given
        Category category1 = Category.builder()
                .name("Category 1")
                .build();
        Expense expense1 = Expense.builder()
                .category(category1)
                .amount(BigDecimal.valueOf(100))
                .datetimeOfExpense(LocalDateTime.now().plusDays(1))
                .build();
        List<Expense> expenseList = new ArrayList<>();
        expenseList.add(expense1);
        category1.setExpenses(expenseList);
        Budget budget1 = Budget.builder()
                .id(UUID.randomUUID())
                .status(BudgetStatus.ACTIVE)
                .type(BudgetType.WEEK)
                .isRenewed(false)
                .categories(List.of(category1))
                .createdOn(LocalDateTime.now().minusDays(1))
                .build();
        Budget budget2 = Budget.builder()
                .id(UUID.randomUUID())
                .status(BudgetStatus.COMPLETED)
                .type(BudgetType.MONTH)
                .isRenewed(false)
                .createdOn(LocalDateTime.now().minusMonths(1).minusDays(1))
                .build();
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .budgets(List.of(budget1, budget2))
                .build();

        // When
        budgetService.updateBudgetSpendingForUser(user);

        // Then
        assertEquals(BudgetStatus.ACTIVE, budget1.getStatus());
        assertEquals(BudgetStatus.COMPLETED, budget2.getStatus());
        assertEquals(BigDecimal.valueOf(100), budget1.getSpent());
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void happyPath_whenSaveBudget() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .budgets(new ArrayList<>())
                .build();

        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user));

        // When
        assertEquals(0, user.getBudgets().size());
        budgetService.saveBudget(BudgetRequest.builder().name("Budget 1").build(), user);

        // Then
        assertEquals(1, user.getBudgets().size());
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void happyPath_whenFindBudgetById() {

        // Given
        UUID budgetId = UUID.randomUUID();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .build();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget1));

        // When
        budgetService.findBudgetById(budgetId);

        // Then
        verify(budgetRepository, times(1)).findById(budgetId);
    }

    @Test
    void givenMissingBudgetInDatabase_whenFindBudgetById_thenExceptionIsThrown() {

        // Given
        UUID budgetId = UUID.randomUUID();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .build();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

        // When && Then
        assertThrows(DomainException.class, () -> budgetService.findBudgetById(budgetId));
        verify(budgetRepository, times(1)).findById(budgetId);

    }

    @Test
    void happyPath_whenUpdateBudget() {

        // Given
        UUID budgetId = UUID.randomUUID();
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        BudgetRequest budgetRequest = BudgetRequest.builder()
                .name("Budget Name")
                .description("Budget Description")
                .maxToSpend(BigDecimal.valueOf(1000))
                .type(BudgetType.WEEK)
                .selectedCategories(List.of(
                        Category.builder()
                                .name("Category 1")
                                .build(),
                        Category.builder()
                                .name("Category 2")
                                .build()
                ))
                .isRenewed(false)
                .build();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .users(List.of(user))
                .build();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget1));

        // When
        budgetService.updateBudget(budgetId, budgetRequest, user);

        // Then
        assertEquals(budget1.getName(), budgetRequest.getName());
        assertEquals(budget1.getDescription(), budgetRequest.getDescription());
        assertEquals(budget1.getMaxToSpend(), budgetRequest.getMaxToSpend());
        assertEquals(budget1.getType(), budgetRequest.getType());
        assertEquals(budget1.getCategories(), budgetRequest.getSelectedCategories());
        assertEquals(budget1.isRenewed(), budgetRequest.isRenewed());
        verify(budgetRepository, times(1)).save(budget1);
    }

    @Test
    void givenMissingBudgetInDatabase_whenUpdateBudget_thenExceptionIsThrown() {

        // Given
        UUID budgetId = UUID.randomUUID();
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        BudgetRequest budgetRequest = BudgetRequest.builder()
                .name("Budget Name")
                .description("Budget Description")
                .maxToSpend(BigDecimal.valueOf(1000))
                .type(BudgetType.WEEK)
                .selectedCategories(List.of(
                        Category.builder()
                                .name("Category 1")
                                .build(),
                        Category.builder()
                                .name("Category 2")
                                .build()
                ))
                .isRenewed(false)
                .build();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .users(List.of(user))
                .build();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

        // When && Then
        assertThrows(DomainException.class, () -> budgetService.updateBudget(budgetId, budgetRequest, user));
        verify(budgetRepository, times(0)).save(budget1);

    }

    @Test
    void givenBudgetOwnerMismatchInDatabase_whenUpdateBudget_thenExceptionIsThrown() {

        // Given
        UUID budgetId = UUID.randomUUID();
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        User notOwner = User.builder()
                .id(UUID.randomUUID())
                .build();
        BudgetRequest budgetRequest = BudgetRequest.builder()
                .name("Budget Name")
                .description("Budget Description")
                .maxToSpend(BigDecimal.valueOf(1000))
                .type(BudgetType.WEEK)
                .selectedCategories(List.of(
                        Category.builder()
                                .name("Category 1")
                                .build(),
                        Category.builder()
                                .name("Category 2")
                                .build()
                ))
                .isRenewed(false)
                .build();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .users(List.of(user))
                .build();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget1));

        // When && Then
        assertThrows(DomainException.class, () -> budgetService.updateBudget(budgetId, budgetRequest, notOwner));
        verify(budgetRepository, times(0)).save(budget1);

    }

    @Test
    void happyPath_whenTerminateBudgetByIdAndOwner() {

        // Given
        UUID budgetId = UUID.randomUUID();
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .status(BudgetStatus.ACTIVE)
                .users(List.of(user))
                .build();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget1));

        // When
        budgetService.terminateBudgetByIdAndOwner(budgetId, user);

        // Then
        assertEquals(budget1.getStatus(), BudgetStatus.TERMINATED);
        verify(budgetRepository, times(1)).save(budget1);
    }

    @Test
    void givenMissingBudgetInDatabase_whenTerminateBudgetByIdAndOwner_thenExceptionIsThrown() {

        // Given
        UUID budgetId = UUID.randomUUID();
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .status(BudgetStatus.ACTIVE)
                .users(List.of(user))
                .build();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

        // When && Then
        assertEquals(budget1.getStatus(), BudgetStatus.ACTIVE);
        assertThrows(DomainException.class, () -> budgetService.terminateBudgetByIdAndOwner(budgetId, user));
        verify(budgetRepository, times(0)).save(budget1);

    }

    @Test
    void givenBudgetOwnerMismatchInDatabase_whenTerminateBudgetByIdAndOwner_thenExceptionIsThrown() {

        // Given
        UUID budgetId = UUID.randomUUID();
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        User notOwner = User.builder()
                .id(UUID.randomUUID())
                .build();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .status(BudgetStatus.ACTIVE)
                .users(List.of(user))
                .build();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget1));

        // When && Then
        assertEquals(budget1.getStatus(), BudgetStatus.ACTIVE);
        assertThrows(DomainException.class, () -> budgetService.terminateBudgetByIdAndOwner(budgetId, notOwner));
        verify(budgetRepository, times(0)).save(budget1);

    }

    @Test
    void happyPath_whenShareBudget() {

        // Given
        UUID budgetId = UUID.randomUUID();
        UUID senderUserId = UUID.randomUUID();
        String username = "Receiver";
        User user = User.builder()
                .id(senderUserId)
                .username("Sender")
                .build();
        User receiver = User.builder()
                .id(senderUserId)
                .username(username)
                .build();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .build();
        List<User> userList = new ArrayList<>();
        userList.add(user);
        budget1.setUsers(userList);

        ShareBudgetRequest shareBudgetRequest = new ShareBudgetRequest();
        shareBudgetRequest.setBudgetId(budgetId);
        shareBudgetRequest.setSenderUserId(senderUserId);
        shareBudgetRequest.setUsername(username);
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget1));
        when(userRepository.findByUsername(shareBudgetRequest.getUsername())).thenReturn(Optional.of(receiver));

        // When
        assertEquals(1, budget1.getUsers().size());
        budgetService.shareBudget(shareBudgetRequest, user, budgetId);

        // Then
        assertEquals(1, budget1.getUsers().size()); // Not Shared - still only 1 user is owner
        verify(notificationService, times(1)).sendNotification(any(), any(), any());
        verify(invitationRepository, times(1)).save(any());
    }

    @Test
    void givenBudgetSenderMatchUsernameInDatabase_whenShareBudget_thenExceptionIsThrown() {

        // Given
        UUID budgetId = UUID.randomUUID();
        UUID senderUserId = UUID.randomUUID();
        String username = "Receiver";
        User user = User.builder()
                .id(senderUserId)
                .username("Sender")
                .build();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .build();
        List<User> userList = new ArrayList<>();
        userList.add(user);
        budget1.setUsers(userList);

        ShareBudgetRequest shareBudgetRequest = new ShareBudgetRequest();
        shareBudgetRequest.setBudgetId(budgetId);
        shareBudgetRequest.setSenderUserId(senderUserId);
        //shareBudgetRequest.setUsername(username);
        shareBudgetRequest.setUsername("Sender"); // Receiver is with same name

        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget1));

        // When
        assertEquals(1, budget1.getUsers().size());

        // Then
        assertEquals(1, budget1.getUsers().size()); // Not Shared - still only 1 user is owner
        assertThrows(DomainException.class, () -> budgetService.shareBudget(shareBudgetRequest, user, budgetId));
        verify(notificationService, times(0)).sendNotification(any(), any(), any());
        verify(invitationRepository, times(0)).save(any());
    }

    @Test
    void givenMissingBudgetInDatabase_whenShareBudget_thenExceptionIsThrown() {

        // Given
        UUID budgetId = UUID.randomUUID();
        UUID senderUserId = UUID.randomUUID();
        String username = "Receiver";
        User user = User.builder()
                .id(senderUserId)
                .username("Sender")
                .build();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .build();
        List<User> userList = new ArrayList<>();
        userList.add(user);
        budget1.setUsers(userList);

        ShareBudgetRequest shareBudgetRequest = new ShareBudgetRequest();
        shareBudgetRequest.setBudgetId(budgetId);
        shareBudgetRequest.setSenderUserId(senderUserId);
        shareBudgetRequest.setUsername(username);
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

        // When
        assertEquals(1, budget1.getUsers().size());

        // Then
        assertEquals(1, budget1.getUsers().size()); // Not Shared - still only 1 user is owner
        assertThrows(DomainException.class, () -> budgetService.shareBudget(shareBudgetRequest, user, budgetId));
        verify(notificationService, times(0)).sendNotification(any(), any(), any());
        verify(invitationRepository, times(0)).save(any());
    }

    @Test
    void givenMissingReceiverUserInDatabase_whenShareBudget_thenExceptionIsThrown() {

        // Given
        UUID budgetId = UUID.randomUUID();
        UUID senderUserId = UUID.randomUUID();
        String username = "Receiver";
        User user = User.builder()
                .id(senderUserId)
                .username("Sender")
                .build();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .build();
        List<User> userList = new ArrayList<>();
        userList.add(user);
        budget1.setUsers(userList);

        ShareBudgetRequest shareBudgetRequest = new ShareBudgetRequest();
        shareBudgetRequest.setBudgetId(budgetId);
        shareBudgetRequest.setSenderUserId(senderUserId);
        shareBudgetRequest.setUsername(username);
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget1));
        when(userRepository.findByUsername(shareBudgetRequest.getUsername())).thenReturn(Optional.empty());

        // When
        assertEquals(1, budget1.getUsers().size());

        // Then
        assertEquals(1, budget1.getUsers().size()); // Not Shared - still only 1 user is owner
        assertThrows(DomainException.class, () -> budgetService.shareBudget(shareBudgetRequest, user, budgetId));
        verify(notificationService, times(0)).sendNotification(any(), any(), any());
        verify(invitationRepository, times(0)).save(any());
    }

    @Test
    void happyPath_whenCreateSharedBudget() {

        // Given
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID inviteId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        User sender = User.builder()
                .id(senderId)
                .username("Sender")
                .budgets(new ArrayList<>())
                .categories(new ArrayList<>())
                .build();
        User receiver = User.builder()
                .id(receiverId)
                .username("Receiver")
                .budgets(new ArrayList<>())
                .categories(new ArrayList<>())
                .build();
        Invitation acceptedInvitation = Invitation.builder()
                .id(inviteId)
                .receiverId(receiverId)
                .senderId(senderId)
                .budgetId(budgetId)
                .name("Invitation 1")
                .senderUserName(sender.getUsername())
                .receiverUserName(receiver.getUsername())
                .build();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .build();
        List<Category> userPresentCategories = new ArrayList<>();
        Category category1 = Category.builder()
                .name("Category 1")
                .build();
        Category category2 = Category.builder()
                .name("Category 2")
                .build();
        userPresentCategories.add(category1);
        userPresentCategories.add(category2);
        sender.getCategories().add(category1);
        sender.getCategories().add(category2);
        receiver.getCategories().add(category1);
        List<User> userList = new ArrayList<>();
        userList.add(sender);
        budget1.setUsers(userList);
        budget1.setCategories(userPresentCategories);

        when(budgetRepository.findById(acceptedInvitation.getBudgetId())).thenReturn(Optional.of(budget1));
        when(userRepository.findById(acceptedInvitation.getReceiverId())).thenReturn(Optional.of(receiver));

        // When
        assertEquals(1, budget1.getUsers().size());
        budgetService.createSharedBudget(acceptedInvitation);

        // Then
        assertEquals(2, budget1.getUsers().size());
        verify(budgetRepository, times(1)).save(any(Budget.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void whenMissingBudgetInDatabase_whenCreateSharedBudget_thenExceptionIsThrown() {

        // Given
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID inviteId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        User sender = User.builder()
                .id(senderId)
                .username("Sender")
                .budgets(new ArrayList<>())
                .categories(new ArrayList<>())
                .build();
        User receiver = User.builder()
                .id(receiverId)
                .username("Receiver")
                .budgets(new ArrayList<>())
                .categories(new ArrayList<>())
                .build();
        Invitation acceptedInvitation = Invitation.builder()
                .id(inviteId)
                .receiverId(receiverId)
                .senderId(senderId)
                .budgetId(budgetId)
                .name("Invitation 1")
                .senderUserName(sender.getUsername())
                .receiverUserName(receiver.getUsername())
                .build();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .build();
        List<Category> userPresentCategories = new ArrayList<>();
        Category category1 = Category.builder()
                .name("Category 1")
                .build();
        Category category2 = Category.builder()
                .name("Category 2")
                .build();
        userPresentCategories.add(category1);
        userPresentCategories.add(category2);
        sender.getCategories().add(category1);
        sender.getCategories().add(category2);
        receiver.getCategories().add(category1);
        List<User> userList = new ArrayList<>();
        userList.add(sender);
        budget1.setUsers(userList);
        budget1.setCategories(userPresentCategories);

        when(budgetRepository.findById(acceptedInvitation.getBudgetId())).thenReturn(Optional.empty());
        //when(userRepository.findById(acceptedInvitation.getReceiverId())).thenReturn(Optional.of(receiver));

        // When
        assertEquals(1, budget1.getUsers().size());

        // Then
        assertThrows(DomainException.class, () -> budgetService.createSharedBudget(acceptedInvitation));
        assertEquals(1, budget1.getUsers().size());
        verify(budgetRepository, times(0)).save(any(Budget.class));
        verify(userRepository, times(0)).save(any(User.class));
        verify(categoryRepository, times(0)).save(any(Category.class));
    }

    @Test
    void whenMissingReceiverUserInDatabase_whenCreateSharedBudget_thenExceptionIsThrown() {

        // Given
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID inviteId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        User sender = User.builder()
                .id(senderId)
                .username("Sender")
                .budgets(new ArrayList<>())
                .categories(new ArrayList<>())
                .build();
        User receiver = User.builder()
                .id(receiverId)
                .username("Receiver")
                .budgets(new ArrayList<>())
                .categories(new ArrayList<>())
                .build();
        Invitation acceptedInvitation = Invitation.builder()
                .id(inviteId)
                .receiverId(receiverId)
                .senderId(senderId)
                .budgetId(budgetId)
                .name("Invitation 1")
                .senderUserName(sender.getUsername())
                .receiverUserName(receiver.getUsername())
                .build();
        Budget budget1 = Budget.builder()
                .id(budgetId)
                .build();
        List<Category> userPresentCategories = new ArrayList<>();
        Category category1 = Category.builder()
                .name("Category 1")
                .build();
        Category category2 = Category.builder()
                .name("Category 2")
                .build();
        userPresentCategories.add(category1);
        userPresentCategories.add(category2);
        sender.getCategories().add(category1);
        sender.getCategories().add(category2);
        receiver.getCategories().add(category1);
        List<User> userList = new ArrayList<>();
        userList.add(sender);
        budget1.setUsers(userList);
        budget1.setCategories(userPresentCategories);

        when(budgetRepository.findById(acceptedInvitation.getBudgetId())).thenReturn(Optional.of(budget1));
        when(userRepository.findById(acceptedInvitation.getReceiverId())).thenReturn(Optional.empty());

        // When
        assertEquals(1, budget1.getUsers().size());

        // Then
        assertThrows(DomainException.class, () -> budgetService.createSharedBudget(acceptedInvitation));
        assertEquals(1, budget1.getUsers().size());
        verify(budgetRepository, times(0)).save(any(Budget.class));
        verify(userRepository, times(0)).save(any(User.class));
        verify(categoryRepository, times(0)).save(any(Category.class));
    }

    @Test
    void happyPath_whenGetTotalAmountSpentByBudgetUser() {

        // Given
        User user1 = User.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .categories(new ArrayList<>())
                .build();
        User user2 = User.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .categories(new ArrayList<>())
                .build();
        List<User> userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);

        Category categoryUser1 = Category.builder()
                .id(UUID.randomUUID())
                .name("Category1")
                .expenses(new ArrayList<>())
                .build();
        Category categoryUser2 = Category.builder()
                .id(UUID.randomUUID())
                .name("Category1")
                .expenses(new ArrayList<>())
                .build();
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(categoryUser1);
        categoryList.add(categoryUser2);
        user1.getCategories().add(categoryUser1);
        user2.getCategories().add(categoryUser2);

        Budget budget1 = Budget.builder()
                .id(UUID.randomUUID())
                .spent(BigDecimal.ZERO)
                .categories(new ArrayList<>())
                .createdOn(LocalDateTime.now())
                .type(BudgetType.MONTH)
                .isRenewed(false)
                .users(userList)
                .build();
        budget1.setCategories(categoryList);

        Expense expense1 = Expense.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(12))
                .datetimeOfExpense(LocalDateTime.now().plusDays(1))
                .category(categoryUser1)
                .build();
        Expense expense2 = Expense.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(23))
                .datetimeOfExpense(LocalDateTime.now().minusDays(1))
                .category(categoryUser1)
                .build();
        Expense expense3 = Expense.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(34))
                .datetimeOfExpense(LocalDateTime.now().plusDays(1))
                .category(categoryUser2)
                .build();
        Expense expense4 = Expense.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(45))
                .datetimeOfExpense(LocalDateTime.now().minusDays(1))
                .category(categoryUser2)
                .build();
        categoryUser1.getExpenses().add(expense1);
        categoryUser1.getExpenses().add(expense2);
        categoryUser2.getExpenses().add(expense3);
        categoryUser2.getExpenses().add(expense4);

        // When
        assertEquals(BigDecimal.ZERO, budget1.getSpent());
        HashMap<User, Double> resultTotal = budgetService.getTotalAmountSpentByBudgetUser(budget1);

        // Then
        assertEquals(46.00, resultTotal.get(user1) + resultTotal.get(user2));
        assertEquals(12.00, resultTotal.get(user1));
        assertEquals(34.00, resultTotal.get(user2));
    }
}
