package com.subtrackr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubTrackrApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubTrackrApplication.class, args);
    }
}
