package com.dangbun.global.openfeign;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.dangbun.domain")
public class OpenFeignConfig {
}
