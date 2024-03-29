package dk.kvalitetsit.fut.careplan;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import dk.kvalitetsit.fut.auth.AuthService;
import org.hl7.fhir.r4.model.*;
import org.openapitools.model.CareplanDto;
import org.openapitools.model.CareplanStatusDto;
import org.openapitools.model.ContextDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CarePlanServiceImpl implements CarePlanService {
    private static final Logger logger = LoggerFactory.getLogger(CarePlanServiceImpl.class);

    private final FhirContext fhirContext;
    private final String careplanServiceUrl;
    private final AuthService authService;

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
                .map(CarePlanMapper::mapCarePlan)
                .collect(Collectors.toList());
    }

    @Override
    public List<CareplanDto> getCarePlansForCareTeam(String careTeamId, String episodeOfCareId, List<String> status) {
        String careTeamUrl = "https://organization.devenvcgi.ehealth.sundhed.dk/fhir/CareTeam/"+careTeamId;
        var careTeamCriteria = CarePlan.CARE_TEAM.hasId(careTeamUrl);

        String episodeOfCareUrl = "https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/"+episodeOfCareId;
        var episodeOfCareCriteria = new ReferenceClientParam("episodeOfCare").hasId(episodeOfCareUrl);

        List<CarePlan> result;
        if (status != null) {
            var statusCriteria = CarePlan.STATUS.exactly().codes(status);
            result = lookupByCriteria(CarePlan.class, List.of(careTeamCriteria, episodeOfCareCriteria, statusCriteria));
        }
        else {
            result = lookupByCriteria(CarePlan.class, List.of(careTeamCriteria, episodeOfCareCriteria));
        }

        return result.stream()
                .map(CarePlanMapper::mapCarePlan)
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

    @Override
    public void updateCarePlan(String episodeOfCareId, String careplanId, OffsetDateTime start, OffsetDateTime end, CareplanStatusDto status) {
        String episodeOfCareUrl = "https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/"+episodeOfCareId;

        IGenericClient client = getFhirClientWithEpisodeOfCareContext(episodeOfCareUrl);
        CarePlan carePlan = client.read()
                .resource(CarePlan.class)
                .withId("CarePlan/" + careplanId)
                .execute();

        if (start != null) {
            carePlan.getPeriod().setStart(Date.from(start.toInstant()));
        }
        if (end != null) {
            carePlan.getPeriod().setEnd(Date.from(end.toInstant()));
        }
        if (status != null) {
            CarePlan.CarePlanStatus newStatus = CarePlanMapper.mapCarePlanStatus(status);
            carePlan.setStatus(newStatus);

            carePlan.getActivity().forEach(carePlanActivityComponent -> {
                ServiceRequest serviceRequest = client.read()
                        .resource(ServiceRequest.class)
                        .withId(carePlanActivityComponent.getReference().getReferenceElement().toUnqualifiedVersionless())
                        .execute();

                serviceRequest.setStatus(CarePlanMapper.mapCarePlanStatusToServiceRequestStatus(status));
                client.update()
                        .resource(serviceRequest)
                        .execute();
            });
        }

        client.update()
                .resource(carePlan)
                .execute();

        logger.info(String.format("Updated CarePlan with id: %s", careplanId));
    }

    @Override
    public void deleteCarePlan(String episodeOfCareId, String careplanId) {
        CareplanDto carePlan = this.getCarePlan(episodeOfCareId, careplanId);
        switch (carePlan.getStatus()) {
            case DRAFT -> updateCarePlan(episodeOfCareId, careplanId, null, OffsetDateTime.now(), CareplanStatusDto.ENTERED_IN_ERROR);
            case ACTIVE -> updateCarePlan(episodeOfCareId, careplanId, null, OffsetDateTime.now(), CareplanStatusDto.REVOKED);
            default -> logger.warn(String.format("Not Implemented! Trying to delete CarePlan with status: %s.", carePlan.getStatus().toString()));
        }
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
        return this.getFhirClient(authService.getToken());
    }
    private IGenericClient getFhirClientWithPatientContext(String patientUrl) {
        AuthService.Token token = null;
        try {
            token = authService.getToken();

            ContextDto context = authService.getContext(token);
            String careTeamId = context.getCareTeams().get(0).getId();

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
            String careTeamId = context.getCareTeams().get(0).getId();

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
