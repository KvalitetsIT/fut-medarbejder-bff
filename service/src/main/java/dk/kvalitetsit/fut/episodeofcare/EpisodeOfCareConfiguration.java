package dk.kvalitetsit.fut.episodeofcare;

import ca.uhn.fhir.context.FhirContext;
import dk.kvalitetsit.fut.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class EpisodeOfCareConfiguration implements WebMvcConfigurer {

    @Value("${episodeofcare.fhir.service.endpoint}")
    private String fhirServiceEndpoint;

    @Bean
    public EpisodeOfCareService episodeOfCareService(@Autowired AuthService authService, @Autowired FhirContext fhirContext) {
        return new EpisodeOfCareServiceImpl(fhirContext, fhirServiceEndpoint, authService);
    }
}
