package pl.edu.mimuw.cloudatlas.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Client {
    public static void main(String[] args) {
        SpringApplication.run(Client.class, args);
    }
}
