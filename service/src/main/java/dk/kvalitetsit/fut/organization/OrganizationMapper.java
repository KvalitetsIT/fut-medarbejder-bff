package dk.kvalitetsit.fut.organization;

import org.hl7.fhir.r4.model.*;
import org.openapitools.model.CareTeamDto;
import org.openapitools.model.CareTeamReasonCodeInnerDto;

import java.util.List;

public class OrganizationMapper {

    public static CareTeamDto mapCareTeam(CareTeam careTeam) {
        CareTeamDto dto = new CareTeamDto();
        dto.setId(careTeam.getIdElement().toUnqualifiedVersionless().getIdPart());
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
        dto.setManagingOrganization(orgs.stream().map(Reference::getReference).toList());

        return dto;
    }

}
