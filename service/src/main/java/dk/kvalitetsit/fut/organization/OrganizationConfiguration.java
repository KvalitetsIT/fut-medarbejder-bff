package dk.kvalitetsit.fut.organization;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import dk.kvalitetsit.fut.auth.AuthService;
import dk.kvalitetsit.fut.patient.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class OrganizationConfiguration implements WebMvcConfigurer {

    @Value("${organization.service.url}")
    private String organizationServiceUrl;

    @Value("${careplan.service.url}")
    private String carePlanServiceUrl;

    @Bean
    public OrganizationServiceImpl organizationService(@Autowired PatientService patientService,
                                                       @Autowired AuthService authService,
                                                       @Autowired FhirContext fhirContext) {
        IGenericClient fhirClient = FhirContext.forR4().newRestfulGenericClient(organizationServiceUrl);
        return new OrganizationServiceImpl(fhirContext,
                organizationServiceUrl,
                carePlanServiceUrl,
                patientService,
                authService);
    }

    @Value("${ALLOWED_ORIGINS:http://localhost:3000}")
    private List<String> allowedOrigins;
}
