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
import org.openapitools.model.ContextDto;
import org.openapitools.model.PatientDto;
import org.openapitools.model.UserInfoDto;
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
        /**
         * Proceduren for at fremsøge og se detaljerne i et CareTeam, for en bestemt medarbejder, er:
         * 1. Få normalt access_token.
         * 2. Se din context. Her kan du se dine CareTeams.
         * 3. Få et ny access_token med dit valgte CareTeam som context.
         * 4. Fremsøg CarePlans.
         * 5. Sæt en udvalgt CarePlan på som context.
         * 6. Fremsøg nu specifik CarePlan med indhold. Her finder du patienten (under Subject).
         * 7. Gør det samme for alle CarePlans (step 4 og frem).
         */

        // Lav klient
        AuthService.Token token = authService.getToken("Gr6_medarbejder12", "Test1266");
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token.accessToken());
        IGenericClient client = fhirContext.newRestfulGenericClient(organizationServiceUrl);
        client.registerInterceptor(authInterceptor);

        // Skaf bruger-context
        UserInfoDto userInfo = authService.getUserInfo(token.accessToken());
        logger.info("user_id " + userInfo.getUserId());
        ContextDto context = authService.getContext(token.accessToken());

        List<PatientDto> patients = new ArrayList<>();

        // TODO: Switch context (CareTeam)
        String careTeamId = context.getCareTeams().get(0).getUuid();
        token = authService.refreshTokenWithCareTeamContext(token, careTeamId);
        logger.info(token.accessToken());

        // TODO: Hent CarePlans

        // TODO: Hent detaljer for hver CarePlan (via context switch)

        // TODO: Map til patienter


        return patients;
    }

    private IGenericClient getFhirClient() throws JsonProcessingException {
        AuthService.Token token = authService.getToken("Gr6_medarbejder12", "Test1266");
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token.accessToken());
        IGenericClient client = fhirContext.newRestfulGenericClient(organizationServiceUrl);
        client.registerInterceptor(authInterceptor);
        return client;
    }
}
