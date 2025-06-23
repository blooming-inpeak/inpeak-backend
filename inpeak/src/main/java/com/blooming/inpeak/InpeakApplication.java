package com.blooming.inpeak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class InpeakApplication {

    public static void main(String[] args) {
        SpringApplication.run(InpeakApplication.class, args);
    }

}
