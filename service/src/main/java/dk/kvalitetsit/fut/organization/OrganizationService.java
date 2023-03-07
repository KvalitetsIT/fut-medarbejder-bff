package dk.kvalitetsit.fut.organization;

import org.openapitools.model.CareTeamDto;

import java.util.List;

public interface OrganizationService {
    List<CareTeamDto> getCareTeams() throws Exception;
}
