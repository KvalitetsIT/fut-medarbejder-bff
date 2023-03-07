package dk.kvalitetsit.fut.organization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CareTeam;
import org.openapitools.model.CareTeamDto;
import org.openapitools.model.PatientDto;

import com.fasterxml.jackson.core.JsonProcessingException;

import dk.kvalitetsit.fut.auth.AuthService;


public class OrganizationServiceImpl implements OrganizationService {

    private final Map<String, PatientDto> patients = new HashMap<>();
    private FhirContext fhirContext;
    private String patientServiceUrl;
    private AuthService authService;

    public OrganizationServiceImpl(FhirContext fhirContext, String patientServiceUrl, AuthService authService) {
        this.fhirContext = fhirContext;
        this.patientServiceUrl = patientServiceUrl;
        this.authService = authService;
    }

    @Override
    public List<CareTeamDto> getCareTeams() throws Exception {

        BearerTokenAuthInterceptor authInterceptor = null;
        try {
            authInterceptor = new BearerTokenAuthInterceptor( authService.getToken() );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        IGenericClient client = fhirContext.newRestfulGenericClient(patientServiceUrl);
        client.registerInterceptor(authInterceptor);

        Bundle result = client
                .search()
                .forResource(CareTeam.class)
                .returnBundle(Bundle.class)
                .execute();

        return result.getEntry().stream()
                .map(bundleEntryComponent -> (CareTeam) bundleEntryComponent.getResource())
                .map(careTeam -> {
                    CareTeamDto dto = new CareTeamDto();
                    dto.setUuid(careTeam.getIdElement().toUnqualifiedVersionless().getIdPart());
                    dto.setName(careTeam.getName());
                    dto.setStatus(careTeam.getStatus().name());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
