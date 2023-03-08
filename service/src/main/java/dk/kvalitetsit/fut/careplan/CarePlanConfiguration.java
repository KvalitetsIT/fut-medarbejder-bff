package dk.kvalitetsit.fut.careplan;

import ca.uhn.fhir.context.FhirContext;
import dk.kvalitetsit.fut.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CarePlanConfiguration implements WebMvcConfigurer {

    @Value("${careplan.service.url}")
    private String careplanServiceUrl;

    @Bean
    public CarePlanServiceImpl carePlanService(@Autowired AuthService authService, @Autowired FhirContext fhirContext) {
        return new CarePlanServiceImpl(fhirContext, careplanServiceUrl, authService);
    }

    @Value("${ALLOWED_ORIGINS:http://localhost:3000}")
    private List<String> allowedOrigins;
}
