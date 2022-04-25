package com.tyche.ramsees;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RamseesApplication {

    public static void main(String[] args) {
        SpringApplication.run(RamseesApplication.class, args);
    }

}
