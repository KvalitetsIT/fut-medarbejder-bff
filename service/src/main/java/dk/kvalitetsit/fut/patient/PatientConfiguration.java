package dk.kvalitetsit.fut.patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import dk.kvalitetsit.fut.auth.AuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.List;

@Configuration
public class PatientConfiguration implements WebMvcConfigurer {

    @Value("${patient.service.url}")
    private String patientServiceUrl;

    @Bean
    public PatientServiceImpl patientService(@Autowired AuthService authService, @Autowired FhirContext fhirContext) {
        IGenericClient fhirClient = FhirContext.forR4().newRestfulGenericClient(patientServiceUrl);
        return new PatientServiceImpl(fhirContext, patientServiceUrl, authService);
    }

}
