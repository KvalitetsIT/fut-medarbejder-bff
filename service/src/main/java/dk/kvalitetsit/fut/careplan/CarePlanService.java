package dk.kvalitetsit.fut.careplan;

import org.openapitools.model.CreateEpisodeOfCareDto;
import org.openapitools.model.EpisodeofcareDto;

import java.util.List;

public interface CarePlanService {
    List<EpisodeofcareDto> getEpisodeOfCares(String careTeamId);
    String createEpisodeOfCare(String careTeamId, String patientId, CreateEpisodeOfCareDto.ProvenanceEnum provenance, List<String> conditionCodes);
}
