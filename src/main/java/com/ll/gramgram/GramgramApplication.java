package com.ll.gramgram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GramgramApplication {

    public static void main(String[] args) {
        SpringApplication.run(GramgramApplication.class, args);
    }

}
