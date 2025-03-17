package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.NotificationService;
import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.Notification;
import com.softuni.personal_finance_app.web.dto.NotificationSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(UserService userService,
                                  NotificationService notificationService) {

        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ModelAndView getNotificationPage(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        User user = userService.getById(authenticatedUserDetails.getUserId());

        NotificationSettings notificationSettings = notificationService.getNotificationSettings(user.getId());
        List<Notification> notificationHistory = notificationService.getNotificationHistory(user.getId());
        long succeededNotificationsNumber = notificationHistory.stream().filter(notification -> notification.getStatus().equals("SUCCEEDED")).count();
        long failedNotificationsNumber = notificationHistory.stream().filter(notification -> notification.getStatus().equals("FAILED")).count();
        notificationHistory = notificationHistory.stream().limit(5).toList();

        ModelAndView modelAndView = new ModelAndView("notifications-page");
        modelAndView.addObject("user", user);
        modelAndView.addObject("notificationPreference", notificationSettings);
        modelAndView.addObject("succeededNotificationsNumber", succeededNotificationsNumber);
        modelAndView.addObject("failedNotificationsNumber", failedNotificationsNumber);
        modelAndView.addObject("notificationHistory", notificationHistory);

        return modelAndView;
    }

    @PutMapping("/user-settings")
    public String updateNotificationSettings(@RequestParam(name = "enabled") boolean enabled,
                                                @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        notificationService.updateNotificationSettings(authenticatedUserDetails.getUserId(), enabled);

        return "redirect:/notifications";
    }

    @DeleteMapping
    public String deleteNotificationHistory(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        UUID userId = authenticatedUserDetails.getUserId();

        notificationService.clearHistory(userId);

        return "redirect:/notifications";
    }

    @PutMapping
    public String retryFailedNotifications(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        UUID userId = authenticatedUserDetails.getUserId();

        notificationService.retryFailed(userId);

        return "redirect:/notifications";
    }
}
