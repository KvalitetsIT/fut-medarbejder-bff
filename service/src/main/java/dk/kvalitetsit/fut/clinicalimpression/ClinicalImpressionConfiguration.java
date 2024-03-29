package dk.kvalitetsit.fut.clinicalimpression;

import ca.uhn.fhir.context.FhirContext;
import dk.kvalitetsit.fut.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ClinicalImpressionConfiguration implements WebMvcConfigurer {
    @Value("${clinicalimpression.fhir.service.endpoint}")
    private String fhirServiceEnpoint;
    @Bean
    public ClinicalImpressionServiceImpl clinicalImpressionService(@Autowired AuthService authService, @Autowired FhirContext fhirContext) {
        return new ClinicalImpressionServiceImpl(fhirContext, fhirServiceEnpoint, authService);
    }
}
