package dk.kvalitetsit.fut.careplan;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import dk.kvalitetsit.fut.auth.AuthService;
import org.hl7.fhir.r4.model.*;
import org.openapitools.model.CareplanDto;
import org.openapitools.model.ContextDto;
import org.openapitools.model.PatientDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CarePlanServiceImpl implements CarePlanService {
    private static final Logger logger = LoggerFactory.getLogger(CarePlanServiceImpl.class);

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
    public CareplanDto getCarePlan(String episodeofcareId, String carePlanId) {
        String episodeOfCareUrl = "https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/"+episodeofcareId;
        IGenericClient client = getFhirClientWithEpisodeOfCareContext(episodeOfCareUrl);

        CarePlan carePlan = client.read()
                .resource(CarePlan.class)
                .withId("CarePlan/" + carePlanId)
                .execute();

        return CarePlanMapper.mapCarePlan(carePlan);
    }

    @Override
    public List<CareplanDto> getCarePlansForCareTeam(String careTeamId) {
        String careTeamUrl = "https://organization.devenvcgi.ehealth.sundhed.dk/fhir/CareTeam/"+careTeamId;
        var careTeamCriteria = CarePlan.CARE_TEAM.hasId(careTeamUrl);

        List<CarePlan> result = lookupByCriteria(CarePlan.class, List.of(careTeamCriteria));

        return result.stream()
                .map(carePlan -> CarePlanMapper.mapCarePlan(carePlan))
                .collect(Collectors.toList());
    }

    @Override
    public String createCarePlan(String episodeOfCareId, String planDefinitionId) {
        String episodeOfCareUrl = "https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/"+episodeOfCareId;
        String planDefinitionUrl = "https://plan.devenvcgi.ehealth.sundhed.dk/fhir/PlanDefinition/"+planDefinitionId;

        Parameters parameters = new Parameters();
        parameters.addParameter()
                .setName("episodeOfCare")
                .setValue(new Reference(episodeOfCareUrl));
        parameters.addParameter()
                .setName("planDefinition")
                .setValue(new Reference(planDefinitionUrl));

        //System.out.println( fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(parameters) );

        IGenericClient client = getFhirClientWithEpisodeOfCareContext(episodeOfCareUrl);
        CarePlan result = client.operation()
                .onServer()
                .named("$apply")
                .withParameters(parameters)
                .returnResourceType(CarePlan.class)
                .execute();

        return result.getIdElement().toUnqualifiedVersionless().getIdPart();
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
        try {
            return this.getFhirClient(authService.getToken());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private IGenericClient getFhirClientWithPatientContext(String patientUrl) {
        AuthService.Token token = null;
        try {
            token = authService.getToken();

            ContextDto context = authService.getContext(token);
            String careTeamId = context.getCareTeams().get(0).getUuid();

            token = authService.refreshTokenWithCareTeamAndPatientContext(token, careTeamId, patientUrl);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return this.getFhirClient(token);
    }

    private IGenericClient getFhirClientWithEpisodeOfCareContext(String episodeOfCareUrl) {

        AuthService.Token token = null;
        try {
            token = authService.getToken();

            ContextDto context = authService.getContext(token);
            String careTeamId = context.getCareTeams().get(0).getUuid();

            token = authService.refreshTokenWithCareTeamAndEpisodeOfCareContext(token, careTeamId, episodeOfCareUrl);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return this.getFhirClient(token);
    }
    private IGenericClient getFhirClient(AuthService.Token token) {
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token.accessToken());

        IGenericClient client = fhirContext.newRestfulGenericClient(careplanServiceUrl);
        client.registerInterceptor(authInterceptor);

        return client;
    }
}
