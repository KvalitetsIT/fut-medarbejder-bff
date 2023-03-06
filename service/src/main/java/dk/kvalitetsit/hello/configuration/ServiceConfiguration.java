package dk.kvalitetsit.hello.configuration;

import dk.kvalitetsit.hello.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {

    @Value("${auth.server.url}")
    private String authServerUrl;

    @Bean
    public AuthService getAuthService() {
        return new AuthService(authServerUrl);
    }
}
