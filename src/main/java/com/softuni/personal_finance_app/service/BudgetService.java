package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.enitity.*;
import com.softuni.personal_finance_app.exception.DomainException;
import com.softuni.personal_finance_app.repository.BudgetRepository;
import com.softuni.personal_finance_app.repository.CategoryRepository;
import com.softuni.personal_finance_app.repository.InvitationRepository;
import com.softuni.personal_finance_app.repository.UserRepository;
import com.softuni.personal_finance_app.web.dto.BudgetRequest;
import com.softuni.personal_finance_app.web.dto.ShareBudgetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final NotificationService notificationService;

    @Autowired
    public BudgetService(BudgetRepository budgetRepository,
                         CategoryRepository categoryRepository,
                         UserRepository userRepository,
                         InvitationRepository invitationRepository,
                         NotificationService notificationService) {
        this.budgetRepository = budgetRepository;

        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
        this.notificationService = notificationService;
    }


    //@Scheduled(fixedRate = 3600000) // 60 minutes in milliseconds
    @Scheduled(fixedRate = 60000) // 1 minutes in milliseconds
    @Transactional
    public void checkCompletedBudgetStatus() {
        List<Budget> activeBudgets = budgetRepository.findByStatus(BudgetStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();

        for (Budget budget : activeBudgets) {
            LocalDateTime startDate = budget.getCreatedOn();
            LocalDateTime endDate = getBudgetEndDate(budget, startDate);
            if (endDate.isBefore(now)) {
                budget.setStatus(BudgetStatus.COMPLETED);
                budgetRepository.save(budget);
                System.out.printf("%s -==- SCHEDULED-JOB: Found 1 Completed Budget!%n", LocalDateTime.now());

                if(budget.isRenewed()) {
                    renewBudget(budget);
                    System.out.printf("%s -==- SCHEDULED-JOB: Completed Budget was RENEWED!%n", LocalDateTime.now());
                }
            }
        }
        System.out.printf("%s -==- SCHEDULED-JOB: Checked for Completed Budgets!%n", LocalDateTime.now());
    }

    //@Transactional
    public void renewBudget(Budget budget) {

        Budget renewedBudget = Budget.builder()
                .name(budget.getName() + "| RENEW@%s".formatted(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyy"))))
                .description(budget.getDescription())
                .maxToSpend(budget.getMaxToSpend())
                .status(BudgetStatus.ACTIVE)
                .type(budget.getType())
                .spent(BigDecimal.ZERO)
                .isRenewed(budget.isRenewed())
                .categories(new ArrayList<>(budget.getCategories()))
                //.users(new ArrayList<>(budget.getUsers()))
                .users(new ArrayList<>())
                .createdOn(getBudgetEndDate(budget, budget.getCreatedOn()).plusDays(1))
                .build();


        for (User user : budget.getUsers()) {
            renewedBudget.addUser(user);
        }

        budgetRepository.save(renewedBudget);
    }

    public LocalDateTime getBudgetEndDate(Budget budget, LocalDateTime startDate) {
        return switch (budget.getType()) {
                    case WEEK -> startDate.plusWeeks(1);
                    case MONTH -> startDate.plusMonths(1);
                    case YEAR -> startDate.plusYears(1);
                };
    }

    @Transactional
    public void updateBudgetSpendingForUser(User user) {

        for (Budget budget : user.getBudgets()) {
            if(budget.getStatus() != BudgetStatus.ACTIVE){
                continue;
            }

            LocalDateTime startDate = budget.getCreatedOn();
            LocalDateTime endDate = getBudgetEndDate(budget, startDate);

            BigDecimal spentOnBudget = budget.getCategories().stream()
                    .flatMap(category -> category.getExpenses().stream())
                    .filter(expense ->
                            expense.getDatetimeOfExpense().isAfter(startDate) &&
                            expense.getDatetimeOfExpense().isBefore(endDate))
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            budget.setSpent(spentOnBudget);

            /* No Longer Needed has Scheduled Job
            if(endDate.isBefore(LocalDateTime.now())) {
                budget.setStatus(BudgetStatus.COMPLETED);
            }
            */

            budgetRepository.save(budget);
        }
    }

    public void saveBudget(BudgetRequest budgetRequest, User user) {

            Budget budget = Budget.builder()
                    .name(budgetRequest.getName())
                    .description(budgetRequest.getDescription())
                    .maxToSpend(budgetRequest.getMaxToSpend())
                    .spent(BigDecimal.valueOf(0))
                    .type(budgetRequest.getType())
                    .categories(budgetRequest.getSelectedCategories())
                    .isRenewed(true)
                    .status(BudgetStatus.ACTIVE)
                    .users(new ArrayList<>())
                    .build();

            budget.addUser(user);

            budgetRepository.save(budget);
    }

    public Budget findBudgetById(UUID budgetId) {

        return budgetRepository.findById(budgetId).orElseThrow(() -> new DomainException("Can't find Budget id [%s]".formatted(budgetId.toString())));
    }

    public void updateBudget(UUID budgetId, BudgetRequest budgetRequest, User user) {

        Budget budget = budgetRepository.findById(budgetId).orElseThrow(() -> new DomainException("Can't find Expense id [%s]".formatted(budgetId.toString())));

        if(!budget.getUsers().contains(user)) {
            throw new DomainException("Budget id [%s] is not owned by user id [%s]".formatted(budget.getId().toString(), user.getId().toString()));
        }

        budget.setName(budgetRequest.getName());
        budget.setDescription(budgetRequest.getDescription());
        budget.setMaxToSpend(budgetRequest.getMaxToSpend());
        budget.setType(budgetRequest.getType());
        budget.setCategories(budgetRequest.getSelectedCategories()); // TO DO: Create a method to Change Categories for shared Users also!
        budget.setRenewed(budgetRequest.isRenewed());

        budgetRepository.save(budget);
    }

    public void terminateBudgetByIdAndOwner(UUID budgetId, User user) {

        Optional<Budget> optionalBudget = budgetRepository.findById(budgetId);

        if(optionalBudget.isEmpty()) {
            throw new DomainException("Not found - Budget id [%s]".formatted(budgetId.toString()));
        }

        Budget budget = optionalBudget.get();

        if(!budget.getUsers().contains(user)) {
            throw new DomainException("User id [%s] is not owner of Budget id [%s]"
                    .formatted(
                            user.getId().toString(),
                            budget.getId().toString())
            );
        }

        budget.setStatus(BudgetStatus.TERMINATED);
        budgetRepository.save(budget);
    }

    public void shareBudget(ShareBudgetRequest shareBudgetRequest, User senderUser, UUID budgetId) {
        
        Optional<Budget> optionalBudget = budgetRepository.findById(budgetId);

        if(shareBudgetRequest.getUsername().equals(senderUser.getUsername())){
            throw new DomainException("Can not send invitation to oneself: Username [%s]".formatted(shareBudgetRequest.getUsername()));
        }

        if(optionalBudget.isEmpty()) {
            throw new DomainException("Not found - Budget id [%s]".formatted(budgetId.toString()));
        }

        Budget budget = optionalBudget.get();

        Optional<User> userOptional = userRepository.findByUsername(shareBudgetRequest.getUsername());

        if(userOptional.isEmpty()) {
            throw new DomainException("Username does not exists: [%s]".formatted(shareBudgetRequest.getUsername()));
        }

        User receiverUser = userOptional.get();

        Invitation invitation = Invitation.builder()
                .name("Sharing a Budget [%s]".formatted(budget.getName()))
                .budgetId(budgetId)
                .senderId(senderUser.getId())
                .senderUserName(senderUser.getUsername())
                .receiverId(receiverUser.getId())
                .receiverUserName(receiverUser.getUsername())
                .status(InvitationStatus.SENT)
                .build();

        //Send Notification to the receiver
        String subject = "New Invitation - Budget share";
        String body = "User [%s] sent you an invitation to share Budget [%s] with you!".formatted(senderUser.getUsername(), budget.getName());
        notificationService.sendNotification(receiverUser.getId(), subject, body);

        invitationRepository.save(invitation);
    }

    @Transactional
    public void createSharedBudget(Invitation acceptedInvitation) {

        Budget budget = budgetRepository.findById(acceptedInvitation.getBudgetId())
                .orElseThrow(() -> new DomainException("Not found - Budget id [%s]".formatted(acceptedInvitation.getBudgetId())));

        User user = userRepository.findById(acceptedInvitation.getReceiverId())
                .orElseThrow(() -> new DomainException("User not found - User id [%s]".formatted(acceptedInvitation.getReceiverId())));

        List<Category> userMissingCategories = new ArrayList<>();
        List<Category> userPresentCategories = new ArrayList<>();

        for (Category budgetCategory : budget.getCategories()) {
            boolean foundMatch = false;
            for (Category userCategory : user.getCategories()) {
                if(budgetCategory.getName().equals(userCategory.getName())) {
                    userPresentCategories.add(userCategory);
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                userMissingCategories.add(budgetCategory);
            }
        }

        if(!userMissingCategories.isEmpty()) {
            // Create Missing categories for User and add them
            for (Category budgetCategory : userMissingCategories) {
                Category category = Category.builder()
                        .name(budgetCategory.getName())
                        .description(budgetCategory.getDescription())
                        .categoryOwner(user)
                        .build();

                categoryRepository.save(category);
                userPresentCategories.add(category);
            }
        }

        // Adding the new User's matching categories to the Shared Budget:
        for (Category userPresentCategory : userPresentCategories) {
            budget.addCategory(userPresentCategory);
        }

        budget.addUser(user);

        budgetRepository.save(budget);
        userRepository.save(user);
    }

    public HashMap<User, Double> getTotalAmountSpentByBudgetUser(Budget budget) {

        HashMap<User, Double> totalAmount = new HashMap<>();

        for (User user : budget.getUsers()) {
            double total = 0.0;

            for (Category category : user.getCategories()) {
                if( budget.getCategories().contains(category) ) {
                    for (Expense expense : category.getExpenses()) {
                        if (expense.getDatetimeOfExpense().isAfter(budget.getCreatedOn()) && expense.getDatetimeOfExpense().isBefore(getBudgetEndDate(budget, budget.getCreatedOn()))) {
                            total += expense.getAmount().doubleValue();
                        }
                    }
                }
            }

            totalAmount.put(user, total);
        }

        return  totalAmount;
    }
}
