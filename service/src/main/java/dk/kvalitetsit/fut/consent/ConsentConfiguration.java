package dk.kvalitetsit.fut.consent;

import ca.uhn.fhir.context.FhirContext;
import dk.kvalitetsit.fut.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class ConsentConfiguration implements WebMvcConfigurer {

    @Value("${consent.fhir.service.endpoint}")
    private String fhirServiceEndpoint;

    @Bean
    public ConsentServiceImpl consentService(@Autowired AuthService authService, @Autowired FhirContext fhirContext) {
        return new ConsentServiceImpl(fhirContext, fhirServiceEndpoint, authService);
    }

    @Value("${ALLOWED_ORIGINS:http://localhost:3000}")
    private List<String> allowedOrigins;
}
