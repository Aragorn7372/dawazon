package dev.luisvives.dawazon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DawazonApplication {

    public static void main(String[] args) {
        SpringApplication.run(DawazonApplication.class, args);
    }

}
