package dk.kvalitetsit.fut.organization;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import dk.kvalitetsit.fut.patient.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.CarePlan;

import org.openapitools.model.ContextDto;
import org.openapitools.model.PatientDto;
import org.openapitools.model.UserInfoDto;
import org.openapitools.model.CareTeamDto;

import dk.kvalitetsit.fut.auth.AuthService;


public class OrganizationServiceImpl implements OrganizationService {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationServiceImpl.class);
    private final FhirContext fhirContext;
    private final String organizationServiceUrl;
    private final String carePlanServiceUrl;
    private final AuthService authService;
    private final PatientService patientService;

    public OrganizationServiceImpl(FhirContext fhirContext,
                                   String organizationServiceUrl,
                                   String carePlanServiceUrl,
                                   PatientService patientService,
                                   AuthService authService) {
        this.fhirContext = fhirContext;
        this.organizationServiceUrl = organizationServiceUrl;
        this.carePlanServiceUrl = carePlanServiceUrl;
        this.patientService = patientService;
        this.authService = authService;
    }

    @Override
    public List<CareTeamDto> getCareTeams() throws JsonProcessingException {
        AuthService.Token token = authService.getToken();
        IGenericClient client = getFhirClient(organizationServiceUrl, token);

        Bundle result = client
                .search()
                .forResource(CareTeam.class)
                .returnBundle(Bundle.class)
                .execute();

        return result.getEntry().stream()
                .map(bundleEntryComponent -> (CareTeam) bundleEntryComponent.getResource())
                .map(OrganizationMapper::mapCareTeam)
                .collect(Collectors.toList());
    }

    @Override
    public CareTeamDto getCareTeam(String careTeamId) throws Exception {
        AuthService.Token token = authService.getToken();
        IGenericClient client = getFhirClient(organizationServiceUrl, token);
        CareTeam careTeam = client.read().resource(CareTeam.class).withId(careTeamId).execute();
        return OrganizationMapper.mapCareTeam(careTeam);
    }

    @Override
    public List<PatientDto> getPatientsForCareTeam() throws Exception {
        List<PatientDto> patients = new ArrayList<>();

        // Lav klient
        AuthService.Token token = authService.getToken();

        // Find et CareTeam til context
        UserInfoDto userInfo = authService.getUserInfo(token.accessToken());
        logger.info("user_id " + userInfo.getUserId());
        ContextDto context = authService.getContext(token.accessToken());

        // Switch context (CareTeam)
        String careTeamId = context.getCareTeams().get(0).getUuid();
        token = authService.refreshTokenWithCareTeamContext(token, careTeamId);
        IGenericClient client = getFhirClient(carePlanServiceUrl, token);

        // Henter CarePlans
        Bundle result  = client
                .search()
                .forResource(CarePlan.class)
                .returnBundle(Bundle.class)
                .where(CarePlan.CARE_TEAM.hasId(careTeamId))
                .execute();
        List<CarePlan> list = result.getEntry().stream()
                .map(bundleEntryComponent -> (CarePlan)bundleEntryComponent.getResource())
                .toList();

        // Lav PatientDto'er
        list.forEach(carePlan -> {
            PatientDto patientDto = new PatientDto();
            patientDto.setUuid(carePlan.getSubject().getReference());
            patients.add(patientDto);
        });

        return patients;
    }

    private IGenericClient getFhirClient(String url, AuthService.Token token) {
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token.accessToken());
        IGenericClient client = fhirContext.newRestfulGenericClient(url);
        client.registerInterceptor(authInterceptor);
        return client;
    }
}
