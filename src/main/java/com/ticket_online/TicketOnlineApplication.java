package com.ticket_online;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TicketOnlineApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketOnlineApplication.class, args);
    }
}
