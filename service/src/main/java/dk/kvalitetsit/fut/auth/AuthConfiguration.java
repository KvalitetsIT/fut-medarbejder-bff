package dk.kvalitetsit.fut.auth;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfiguration {

    @Value("${auth.server.url}")
    private String authServerUrl;

    @Bean
    public AuthService getAuthService() {
        return new AuthService(authServerUrl);
    }

    @Bean
    public FhirContext getFhirContext() {
        return FhirContext.forR4();
    }
}
