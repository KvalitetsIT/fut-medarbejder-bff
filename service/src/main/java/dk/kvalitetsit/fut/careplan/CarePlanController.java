package dk.kvalitetsit.fut.careplan;

import org.openapitools.api.EpisodeOfCaresApi;
import org.openapitools.model.CreateEpisodeOfCareDto;
import org.openapitools.model.EpisodeofcareDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
public class CarePlanController implements EpisodeOfCaresApi {

    private final CarePlanServiceImpl carePlanService;

    private static final Logger logger = LoggerFactory.getLogger(CarePlanController.class);


    public CarePlanController(CarePlanServiceImpl patientService) {
        this.carePlanService = patientService;
    }


    @Override
    public ResponseEntity<List<EpisodeofcareDto>> v1EpisodeofcaresGet(String careTeamId) {
        List<EpisodeofcareDto> patients = carePlanService.getEpisodeOfCares(careTeamId);
        return ResponseEntity.ok(patients);
    }

    @Override
    public ResponseEntity<Void> v1EpisodeofcaresPost(CreateEpisodeOfCareDto createEpisodeOfCareDto) {
        String careTeamId = createEpisodeOfCareDto.getCareTeamId();
        String patientId = createEpisodeOfCareDto.getPatientId();
        CreateEpisodeOfCareDto.ProvenanceEnum provenance = createEpisodeOfCareDto.getProvenance();
        List<String> conditionCodes = createEpisodeOfCareDto.getConditionCodes();

        String episodeOfCareId = carePlanService.createEpisodeOfCare(careTeamId, patientId, provenance, conditionCodes);
        URI location = URI.create(ServletUriComponentsBuilder.fromCurrentRequestUri().path("/" + episodeOfCareId).build().toString());
        return ResponseEntity.created(location).build();
    }
}
