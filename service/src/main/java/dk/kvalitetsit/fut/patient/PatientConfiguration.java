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
    public PatientServiceImpl patientService(@Autowired AuthService authService) {
        IGenericClient fhirClient = FhirContext.forR4().newRestfulGenericClient(patientServiceUrl);
        return new PatientServiceImpl(fhirClient, authService);
    }

    @Value("${ALLOWED_ORIGINS:http://localhost:3000}")
    private List<String> allowedOrigins;

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        allowedOrigins.forEach(config::addAllowedOrigin);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(0);

        return bean;
    }

    @Configuration
    public static class FilterConfiguration {
        @Bean
        public Filter loggerFilter() {
            return new Filter() {
                private static final Logger logger = LoggerFactory.getLogger(PatientConfiguration.class);
                @Override
                public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                    HttpServletRequest httpRequest = (HttpServletRequest) request;

                    logger.info(String.format("Received an %s request at: %s", httpRequest.getMethod(), httpRequest.getRequestURI()));
                    chain.doFilter(request, response);
                }
            };
        }

    }


}
