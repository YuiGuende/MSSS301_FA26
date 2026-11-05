package com.mss301.watch.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// [TODO-6.17] BASIC logs the method, URL and status of each Saga call to MSBrand
@Configuration
public class AppConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
