package dk.kvalitetsit.fut.careplan;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import dk.kvalitetsit.fut.auth.AuthService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.hl7.fhir.r4.model.Resource;
import org.openapitools.model.EpisodeofcareDto;
import org.openapitools.model.PatientDto;

import java.util.*;
import java.util.stream.Collectors;

public class CarePlanServiceImpl implements CarePlanService {

    private final Map<String, PatientDto> patients = new HashMap<>();
    private FhirContext fhirContext;
    private String careplanServiceUrl;
    private AuthService authService;

    public CarePlanServiceImpl(FhirContext fhirContext, String careplanServiceUrl, AuthService authService) {
        this.fhirContext = fhirContext;
        this.careplanServiceUrl = careplanServiceUrl;
        this.authService = authService;
    }



    @Override
    public List<EpisodeofcareDto> getEpisodeOfCares(String careTeamId) {
        var teamCriteria = new ReferenceClientParam("team").hasId("https://organization.devenvcgi.ehealth.sundhed.dk/fhir/CareTeam/" + careTeamId);

        List<EpisodeOfCare> result = lookupByCriteria(EpisodeOfCare.class, List.of(teamCriteria));

        return result.stream()
                .map(episodeOfCare -> CarePlanMapper.mapEpisodeOfCare(episodeOfCare))
                .collect(Collectors.toList());
    }



    private <T extends Resource> List<T> lookupByCriteria(Class<T> resourceClass, List<ICriterion> criteria) {
        IGenericClient client = getFhirClient();

        IQuery<Bundle> query = client
                .search()
                .forResource(resourceClass)
                .returnBundle(Bundle.class);

        if (!criteria.isEmpty()) {
            query = query.where(criteria.get(0));
            for(int i = 1; i < criteria.size(); i++) {
                query = query.and(criteria.get(i));
            }
        }

        Bundle result = query.execute();

        return result.getEntry().stream()
                .map(bundleEntryComponent -> (T)bundleEntryComponent.getResource())
                .collect(Collectors.toList());
    }

    private IGenericClient getFhirClient() {
        BearerTokenAuthInterceptor authInterceptor = null;
        try {
            authInterceptor = new BearerTokenAuthInterceptor( authService.getToken().accessToken() );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        IGenericClient client = fhirContext.newRestfulGenericClient(careplanServiceUrl);
        client.registerInterceptor(authInterceptor);
        return client;
    }
}
