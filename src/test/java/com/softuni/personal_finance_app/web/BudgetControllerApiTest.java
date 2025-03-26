package com.softuni.personal_finance_app.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softuni.personal_finance_app.enitity.*;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.BudgetService;
import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.BudgetRequest;
import com.softuni.personal_finance_app.web.dto.ShareBudgetRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.softuni.personal_finance_app.TestBuilder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(BudgetController.class)
public class BudgetControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private BudgetService budgetService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getDeleteEndpoint_shouldReturnBudgetsView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = get("/budgets/delete")
                .param("budgetId", String.valueOf(budgetId))
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/budgets"));
        verify(userService, times(1)).getById(any());
        verify(budgetService, times(1)).terminateBudgetByIdAndOwner(any(), any());
    }


    @Test
    void putProcessBudgetEditUpdate_happyPath() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);
        User randomUser = aRandomUser();
        Budget randomBudget = aRandomBudget();
        List<Category> categoryList = List.of(aRandomCategory());
        BudgetRequest budgetRequest = BudgetRequest.builder()
                .name("Budget Name 1")
                .description("Budget Description 1")
                .maxToSpend(BigDecimal.valueOf(1000.01))
                .type(BudgetType.MONTH)
                .isRenewed(true)
                .selectedCategories(categoryList)
                .build();

        // Serialize BudgetRequest to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBodyJson = objectMapper.writeValueAsString(budgetRequest);


        MockHttpServletRequestBuilder request = put("/budgets/submitEdit")
                .param("budgetId", String.valueOf(budgetId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyJson)
                .with(user(principal))
                .with(csrf());


        when(userService.getById(any())).thenReturn(randomUser);
        when(budgetService.findBudgetById(any())).thenReturn(randomBudget);

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/budgets"));
        verify(userService, times(1)).getById(any());
        verify(budgetService, times(1)).updateBudget(any(), any(), any());
    }

    @Test
    void putProcessBudgetEditUpdateWhenBindingError_shouldReturnUpdateExpenseView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        User randomUser = aRandomUser();
        Budget randomBudget = aRandomBudget();
        List<Category> categoryList = List.of(aRandomCategory());
        BudgetRequest budgetRequest = BudgetRequest.builder()
                .name("Budget Name 1")
                .description("Budget Description 1")
                .maxToSpend(BigDecimal.valueOf(1000.01))
                .type(null) //.type(BudgetType.MONTH)
                .isRenewed(true)
                .selectedCategories(categoryList)
                .build();

        // Serialize BudgetRequest to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBodyJson = objectMapper.writeValueAsString(budgetRequest); // Missing param => Binding Error

        MockHttpServletRequestBuilder request = put("/budgets/submitEdit")
                .param("budgetId", String.valueOf(budgetId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyJson)
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(randomUser);
        when(budgetService.findBudgetById(any())).thenReturn(randomBudget);

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-budget"))
                .andExpect(model().attributeExists("user", "budgetRequest", "budgetId"));
        verify(userService, times(1)).getById(any());
        verify(budgetService, never()).updateBudget(any(), any(), any());
    }

    @Test
    void getBudgetEditPage_shouldReturnEditBudgetView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = get("/budgets/showEdit")
                .param("budgetId", String.valueOf(budgetId))
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(budgetService.findBudgetById(any())).thenReturn(aRandomBudget());
        when(budgetService.getBudgetEndDate(any(), any())).thenReturn(LocalDateTime.now().plusMonths(1));

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-budget"))
                .andExpect(model().attributeExists("user", "budgetRequest", "budgetId", "budget", "budgetEndDate", "budgetStartDate", "totalAmount"));
        verify(userService, times(1)).getById(any());
        verify(budgetService, times(1)).findBudgetById(any());
        verify(budgetService, times(1)).getBudgetEndDate(any(), any());
        verify(budgetService, times(1)).getTotalAmountSpentByBudgetUser(any());
    }

    @Test
    void getBudgetsPage_shouldReturnBudgetsView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = get("/budgets")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("budgets-page"))
                .andExpect(model().attributeExists("user", "activePage"));
        verify(userService, times(1)).getById(any());
        verify(budgetService, times(1)).updateBudgetSpendingForUser(any());
    }

    @Test
    void postPostProcessBudgetRequest_happyPath() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        Budget randomBudget = aRandomBudget();

        BudgetRequest budgetRequest = BudgetRequest.builder()
                .name(randomBudget.getName())
                .description(randomBudget.getDescription())
                .maxToSpend(randomBudget.getMaxToSpend())
                .type(randomBudget.getType())
                .selectedCategories(randomBudget.getCategories())
                .isRenewed(randomBudget.isRenewed())
                .build();

        MockHttpServletRequestBuilder request = post("/budgets")
                .formField("name", budgetRequest.getName())
                .formField("description", budgetRequest.getDescription())
                .formField("maxToSpend",  String.valueOf(budgetRequest.getMaxToSpend()))
                .formField("type",  String.valueOf(budgetRequest.getType()))
                .formField("selectedCategories",  String.valueOf(budgetRequest.getSelectedCategories()))
                .formField("isRenewed",  String.valueOf(budgetRequest.isRenewed()))
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/budgets"))
                .andExpect(model().attributeExists("user"));;
        verify(userService, times(1)).getById(any());
        verify(budgetService, never()).saveBudget(any(), any());
    }

    @Test
    void postProcessBudgetRequestWhenBindingError_shouldReturnAddBudgetView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = post("/budgets")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-budget"));
        verify(userService, times(1)).getById(any());
        verify(budgetService, never()).saveBudget(any(), any());
    }


    @Test
    void getBudgetRequestPage_shouldReturnAddBudgetView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = get("/budgets/add")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-budget"))
                .andExpect(model().attributeExists("budgetRequest", "user", "activePage"));
        verify(userService, times(1)).getById(any());
    }

    @Test
    void postProcessShareBudgetRequest_happyPath() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        User randomUser = aRandomUser();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        ShareBudgetRequest shareBudgetRequest = new ShareBudgetRequest();
        shareBudgetRequest.setBudgetId(budgetId);
        shareBudgetRequest.setSenderUserId(randomUser.getId());
        shareBudgetRequest.setUsername(randomUser.getUsername());

        MockHttpServletRequestBuilder request = post("/budgets/share")
                .param("budgetId", String.valueOf(budgetId))
                .formField("budgetId", String.valueOf(shareBudgetRequest.getBudgetId()))
                .formField("senderUserId", String.valueOf(shareBudgetRequest.getSenderUserId()))
                .formField("username", shareBudgetRequest.getUsername())
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(randomUser);

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/budgets"))
                .andExpect(model().attributeExists("user"));;
        verify(userService, times(1)).getById(any());
        verify(budgetService, never()).findBudgetById(any());
        verify(budgetService, times(1)).shareBudget(any(), any(), any());
    }

    @Test
    void postProcessShareBudgetRequestWhenBindingError_shouldReturnShareBudgetView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = post("/budgets/share")
                .param("budgetId", String.valueOf(budgetId))
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(budgetService.findBudgetById(any())).thenReturn(aRandomBudget());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("share-budget"));
        verify(userService, times(1)).getById(any());
        verify(budgetService, times(1)).findBudgetById(any());
        verify(budgetService, never()).shareBudget(any(), any(), any());
    }

    @Test
    void getShareBudgetPage_shouldReturnShareBudgetView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = get("/budgets/share")
                .param("budgetId", String.valueOf(budgetId))
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(budgetService.findBudgetById(any())).thenReturn(aRandomBudget());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("share-budget"))
                .andExpect(model().attributeExists("user", "shareBudgetRequest", "budgetId", "budget"));
        verify(userService, times(1)).getById(any());
        verify(budgetService, times(1)).findBudgetById(any());
    }

//    @Test
//    void testSerialization() throws Exception {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        Category category1 = new Category();
//        category1.setId(UUID.randomUUID());
//        category1.setName("Category 1");
//
//        List<Category> categories = List.of(category1);
//
//        BudgetRequest budgetRequest = BudgetRequest.builder()
//                .name("Test Budget")
//                .description("Test Description")
//                .maxToSpend(new BigDecimal("1000"))
//                .isRenewed(true)
//                .selectedCategories(categories)
//                .build();
//
//        String json = objectMapper.writeValueAsString(budgetRequest);
//        System.out.println(json);
//    }
}
