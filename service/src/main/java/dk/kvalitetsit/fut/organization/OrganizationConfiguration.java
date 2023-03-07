package dk.kvalitetsit.fut.organization;

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
public class OrganizationConfiguration implements WebMvcConfigurer {

    @Value("${organization.service.url}")
    private String organizationServiceUrl;

    @Bean
    public OrganizationServiceImpl organizationService(@Autowired AuthService authService,
                                                       @Autowired FhirContext fhirContext) {
        IGenericClient fhirClient = FhirContext.forR4().newRestfulGenericClient(organizationServiceUrl);
        return new OrganizationServiceImpl(fhirContext, organizationServiceUrl, authService);
    }

    @Value("${ALLOWED_ORIGINS:http://localhost:3000}")
    private List<String> allowedOrigins;

    @Bean // TODO: Vi skal nok have samlet de her filtre et sted
    public FilterRegistrationBean<CorsFilter> organizationCorsFilter() {
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
        @Bean // TODO: Vi skal nok have samlet de her filtre et sted
        public Filter organizationLoggerFilter() {
            return new Filter() {
                private static final Logger logger = LoggerFactory.getLogger(OrganizationConfiguration.class);
                @Override
                public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                        throws IOException, ServletException {
                    HttpServletRequest httpRequest = (HttpServletRequest) request;

                    logger.info(String.format("Received an %s request at: %s", httpRequest.getMethod(),
                            httpRequest.getRequestURI()));
                    chain.doFilter(request, response);
                }
            };
        }
    }

}
