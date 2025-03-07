package com.softuni.personal_finance_app.web.dto;

import lombok.Data;

@Data
public class NotificationSettings {

    private String type;

    private boolean enabled;

    private String contactInfo;
}
