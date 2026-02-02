package com.hackathon.spmguidance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpmGuidanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpmGuidanceApplication.class, args);
        System.out.println("==============================================");
        System.out.println("SPM Guidance Assistant is running on port 8080");
        System.out.println("Open: http://localhost:8080");
        System.out.println("==============================================");
    }
}