package dk.kvalitetsit.fut.organization;

import org.openapitools.model.CareTeamDto;
import org.openapitools.model.PatientDto;

import java.util.List;

public interface OrganizationService {
    List<CareTeamDto> getCareTeams() throws Exception;
    CareTeamDto getCareTeam(String id) throws Exception;
    List<PatientDto> getPatientsForCareTeam() throws Exception;
}
