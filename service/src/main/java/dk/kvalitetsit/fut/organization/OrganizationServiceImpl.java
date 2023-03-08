package dk.kvalitetsit.fut.organization;

import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.openapitools.model.CareTeamDto;
import org.openapitools.model.CareTeamReasonCodeInnerDto;

import com.fasterxml.jackson.core.JsonProcessingException;

import dk.kvalitetsit.fut.auth.AuthService;
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

        String accessToken = authService.getToken("Gr6_medarbejder12", "Test1266");
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(accessToken);
        IGenericClient client = fhirContext.newRestfulGenericClient(organizationServiceUrl);
        client.registerInterceptor(authInterceptor);

        // TODO: test - remove when done
        UserInfoDto userInfo = authService.getUserInfo(accessToken);
        logger.info("user_id " + userInfo.getUserId());
        //

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

                    List<CodeableConcept> reasons = careTeam.getReasonCode();
                    dto.setReasonCode(reasons.stream().map( (code -> {
                        CareTeamReasonCodeInnerDto reasonDto = new CareTeamReasonCodeInnerDto();
                        reasonDto.code(code.getCoding().get(0).getCode());
                        reasonDto.display(code.getCoding().get(0).getDisplay());
                        return reasonDto;
                    })).toList());

                    List<org.hl7.fhir.r4.model.Reference> orgs = careTeam.getManagingOrganization();
                    dto.setManagingOrganization(orgs.stream().map(o -> o.getReference()).toList());

                    return dto;
                })
                .collect(Collectors.toList());
    }
}
