package com.softuni.personal_finance_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients
public class PersonalFinanceAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonalFinanceAppApplication.class, args);
	}

}
