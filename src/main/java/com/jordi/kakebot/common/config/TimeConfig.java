package com.jordi.kakebot.common.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    private static final ZoneId APP_ZONE = ZoneId.of("Europe/Madrid");

    @Bean
    public Clock appClock() {
        return Clock.system(APP_ZONE);
    }
}
