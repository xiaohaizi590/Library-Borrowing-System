package net.togogo.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserFeignConfig {

    @Autowired
    private TokenStore tokenStore;

    @Bean
    public RequestInterceptor feignAuthInterceptor() {
        return template -> {
            String token = tokenStore.getToken();
            if (token != null && !token.isEmpty()) {
                template.header("Authorization", "Bearer " + token);
            }
        };
    }
}