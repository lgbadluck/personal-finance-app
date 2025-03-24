package com.softuni.personal_finance_app.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationSettings {

    private String type;

    private boolean enabled;

    private String contactInfo;
}
