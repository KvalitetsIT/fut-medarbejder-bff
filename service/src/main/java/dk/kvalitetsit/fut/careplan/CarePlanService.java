package dk.kvalitetsit.fut.careplan;

import org.openapitools.model.CareplanDto;
import org.openapitools.model.CareplanStatusDto;

import java.time.OffsetDateTime;
import java.util.List;

public interface CarePlanService {

    CareplanDto getCarePlan(String episodeofcareId, String carePlanId);

    List<CareplanDto> getCarePlansForCareTeam(String careTeamId);

    List<CareplanDto> getCarePlansForCareTeam(String careTeamId, String episodeOfCareId);

    String createCarePlan(String episodeofcareId, String plandefinitionId);

    void updateCarePlan(String episodeOfCareId, String careplanId, OffsetDateTime start, OffsetDateTime end, CareplanStatusDto status);

    void deleteCarePlan(String episodeOfCareId, String careplanId);
}
