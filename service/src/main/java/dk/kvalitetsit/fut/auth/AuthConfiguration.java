package dk.kvalitetsit.fut.auth;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfiguration {

    @Value("${auth.token.url}")
    private String authTokenUrl;

    @Value("${auth.userinfo.url}")
    private String authUserinfoUrl;

    @Bean
    public AuthService getAuthService() {
        return new AuthService(authTokenUrl, authUserinfoUrl);
    }

    @Bean
    public FhirContext getFhirContext() {
        return FhirContext.forR4();
    }
}
