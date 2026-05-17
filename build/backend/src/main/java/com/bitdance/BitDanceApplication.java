package com.bitdance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;



@SpringBootApplication
@EnableScheduling
public class BitDanceApplication {
    public static void main(String[] args) {
        System.setProperty("socksNonProxyHosts", "98.142.241.155|localhost|127.0.0.1");
        SpringApplication.run(BitDanceApplication.class, args);
    }
}
