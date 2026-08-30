package com.example.rcp.config;
import org.springframework.context.annotation.*;
import java.time.Clock;
@Configuration public class AppConfig { @Bean public Clock clock(){return Clock.systemDefaultZone();} }
