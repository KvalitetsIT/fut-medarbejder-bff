package dk.kvalitetsit.fut.careplan;

import org.openapitools.model.CareplanDto;

import java.util.List;

public interface CarePlanService {

    CareplanDto getCarePlan(String episodeofcareId, String carePlanId);

    List<CareplanDto> getCarePlansForCareTeam(String careTeamId);

    String createCarePlan(String episodeofcareId, String plandefinitionId);
}
