package com.softuni.personal_finance_app.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Notification {

    private String subject;

    private LocalDateTime createdOn;

    private String status;

    private String type;

}
