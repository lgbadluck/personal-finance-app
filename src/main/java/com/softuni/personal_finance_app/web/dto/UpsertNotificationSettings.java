package com.softuni.personal_finance_app.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpsertNotificationSettings {

    private UUID userId;

    private boolean notificationEnabled;

    private String type;

    private String contactInfo;
}
