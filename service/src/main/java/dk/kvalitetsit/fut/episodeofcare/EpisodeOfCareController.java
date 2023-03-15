package dk.kvalitetsit.fut.episodeofcare;

import org.openapitools.api.EpisodeOfCareApi;
import org.openapitools.model.CreateEpisodeOfCareDto;
import org.openapitools.model.EpisodeOfCareStatusDto;
import org.openapitools.model.EpisodeofcareDto;
import org.openapitools.model.UpdateEpisodeOfCareDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
public class EpisodeOfCareController implements EpisodeOfCareApi {

    private final EpisodeOfCareService episodeOfCareService;

    private static final Logger logger = LoggerFactory.getLogger(EpisodeOfCareController.class);


    public EpisodeOfCareController(EpisodeOfCareService episodeOfCareService) {
        this.episodeOfCareService = episodeOfCareService;
    }

    @Override
    public ResponseEntity<List<EpisodeofcareDto>> v1GetEpisodeOfCaresForCareTeam(String careTeamId) {
        List<EpisodeofcareDto> patients = episodeOfCareService.getEpisodeOfCaresForCareTeam(careTeamId);
        return ResponseEntity.ok(patients);
    }

    @Override
    public ResponseEntity<EpisodeofcareDto> v1GetEpisodeOfCare(String episodeOfCareId) {
        EpisodeofcareDto episodeOfCare = episodeOfCareService.getEpisodeOfCare(episodeOfCareId);

        return ResponseEntity.ok(episodeOfCare);
    }

    @Override
    public ResponseEntity<List<EpisodeofcareDto>> v1GetEpisodeOfCares(String careTeamId) {
        List<EpisodeofcareDto> patients = episodeOfCareService.getEpisodeOfCaresForCareTeam(careTeamId);
        return ResponseEntity.ok(patients);
    }

    @Override
    public ResponseEntity<Void> v1PatchEpisodeOfCare(String episodeOfCareId, UpdateEpisodeOfCareDto updateEpisodeOfCareDto) {
        OffsetDateTime start = updateEpisodeOfCareDto.getStart();
        OffsetDateTime end = updateEpisodeOfCareDto.getEnd();
        EpisodeOfCareStatusDto status = updateEpisodeOfCareDto.getStatus();
        String careTeamId = updateEpisodeOfCareDto.getCareTeamId();

        episodeOfCareService.updateEpisodeOfCare(episodeOfCareId, start, end, status, careTeamId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> v1PostEpisodeOfCare(CreateEpisodeOfCareDto createEpisodeOfCareDto) {
        String careTeamId = createEpisodeOfCareDto.getCareTeamId();
        String patientId = createEpisodeOfCareDto.getPatientId();
        CreateEpisodeOfCareDto.ProvenanceEnum provenance = createEpisodeOfCareDto.getProvenance();
        List<String> conditionCodes = createEpisodeOfCareDto.getConditionCodes();

        String episodeOfCareId = episodeOfCareService.createEpisodeOfCare(careTeamId, patientId, provenance, conditionCodes);
        URI location = URI.create(ServletUriComponentsBuilder.fromCurrentRequestUri().path("/" + episodeOfCareId).build().toString());
        return ResponseEntity.created(location).build();
    }
}
