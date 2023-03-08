package dk.kvalitetsit.fut.careplan;

import org.openapitools.model.EpisodeofcareDto;

import java.util.List;

public interface CarePlanService {
    List<EpisodeofcareDto> getEpisodeOfCares(String careTeamId);
}
