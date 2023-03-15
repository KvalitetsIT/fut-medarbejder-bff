package dk.kvalitetsit.fut.episodeofcare;

import org.openapitools.model.CreateEpisodeOfCareDto;
import org.openapitools.model.EpisodeOfCareStatusDto;
import org.openapitools.model.EpisodeofcareDto;

import java.time.OffsetDateTime;
import java.util.List;

public interface EpisodeOfCareService {
    List<EpisodeofcareDto> getEpisodeOfCaresForCareTeam(String careTeamId);
    String createEpisodeOfCare(String careTeamId, String patientId, CreateEpisodeOfCareDto.ProvenanceEnum provenance, List<String> conditionCodes);

    EpisodeofcareDto getEpisodeOfCare(String episodeOfCareId);

    void updateEpisodeOfCare(String episodeOfCareId, OffsetDateTime start, OffsetDateTime end, EpisodeOfCareStatusDto status, String careTeamId);
}
