package com.assesment.ragchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "com.assesment.ragchat")
public class RagChatStorageApp {
    public static void main(String[] args) {
        SpringApplication.run(RagChatStorageApp.class, args);
    }

}