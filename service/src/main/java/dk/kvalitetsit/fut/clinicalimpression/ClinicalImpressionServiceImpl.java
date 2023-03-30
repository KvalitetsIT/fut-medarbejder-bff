package dk.kvalitetsit.fut.clinicalimpression;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import dk.kvalitetsit.fut.auth.AuthService;
import dk.kvalitetsit.fut.configuration.FhirLoggingInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.openapitools.model.ClinicalimpressionDto;
import org.openapitools.model.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class ClinicalImpressionServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(ClinicalImpressionServiceImpl.class);
    private FhirContext fhirContext;
    private String fhirServiceEndpoint;
    private AuthService authService;

    public ClinicalImpressionServiceImpl(FhirContext fhirContext, String fhirServiceEndpoint, AuthService authService) {
        this.fhirContext = fhirContext;
        this.fhirServiceEndpoint = fhirServiceEndpoint;
        this.authService = authService;
    }

    private IGenericClient getFhirClient(AuthService.Token token) {
        IGenericClient client = fhirContext.newRestfulGenericClient(fhirServiceEndpoint);
        FhirLoggingInterceptor logInt = new FhirLoggingInterceptor(logger);
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token.accessToken());
        client.registerInterceptor(logInt);
        client.registerInterceptor(authInterceptor);
        return client;
    }

    public ClinicalimpressionDto getClinicalImpression(String episodeOfCareId, String clinicalImpressionId) {
        String episodeOfCareUrl = "https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/"+episodeOfCareId;
        AuthService.Token token = authService.getToken();
        try {
            token = authService.refreshTokenWithEpisodeOfCareContext(token, episodeOfCareUrl);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        IGenericClient client = getFhirClient(token);
        ClinicalImpression clinicalImpression = client.read()
                .resource(ClinicalImpression.class)
                .withId("ClinicalImpression/" + clinicalImpressionId)
                .execute();

        return ClinicalImpressionMapper.mapClinicalImpression(clinicalImpression);
    }

    //public void createClinicalImpression()
}
