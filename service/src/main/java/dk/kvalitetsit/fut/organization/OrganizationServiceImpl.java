package dk.kvalitetsit.fut.organization;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CareTeam;
import org.openapitools.model.CareTeamDto;

import com.fasterxml.jackson.core.JsonProcessingException;

import dk.kvalitetsit.fut.auth.AuthService;
import org.openapitools.model.PatientDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OrganizationServiceImpl implements OrganizationService {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationServiceImpl.class);
    private final FhirContext fhirContext;
    private final String organizationServiceUrl;
    private final AuthService authService;

    public OrganizationServiceImpl(FhirContext fhirContext, String organizationServiceUrl, AuthService authService) {
        this.fhirContext = fhirContext;
        this.organizationServiceUrl = organizationServiceUrl;
        this.authService = authService;
    }

    @Override
    public List<CareTeamDto> getCareTeams() throws JsonProcessingException {

        IGenericClient client = getFhirClient();

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
        IGenericClient client = getFhirClient();
        CareTeam careTeam = client.read().resource(CareTeam.class).withId(careTeamId).execute();
        return OrganizationMapper.mapCareTeam(careTeam);
    }

    @Override
    public List<PatientDto> getPatientsForCareTeam() throws Exception {

        /*       // EKSPERIMENT
        // TODO: Ryd op når du er færdig :)
        // Får jeg CareTeams for andre end mig selv igennem FUT API??

        // Medarbejder er hardkodet indtil videre
        String accessToken = authService.getToken("Gr6_medarbejder12", "Test1266");
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(accessToken);
        IGenericClient client = fhirContext.newRestfulGenericClient(organizationServiceUrl);
        client.registerInterceptor(authInterceptor);

        // Hvem er jeg?
        UserInfoDto userInfo = authService.getUserInfo(accessToken);
        logger.info("user_id " + userInfo.getUserId());

        // Vi låner lige kaldet til CareTeams
        List<CareTeamDto> careTeams = getCareTeams();
        */
        // Map til liste af patienter
        List<PatientDto> patients = new ArrayList<PatientDto>();

        return patients;
    }

    private IGenericClient getFhirClient() throws JsonProcessingException {
        String accessToken = authService.getToken("Gr6_medarbejder12", "Test1266");
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(accessToken);
        IGenericClient client = fhirContext.newRestfulGenericClient(organizationServiceUrl);
        client.registerInterceptor(authInterceptor);
        return client;
    }
}
