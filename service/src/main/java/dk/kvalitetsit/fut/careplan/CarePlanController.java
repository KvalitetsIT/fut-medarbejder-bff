package dk.kvalitetsit.fut.careplan;

import org.openapitools.api.ConsentApi;
import org.openapitools.api.EpisodeOfCaresApi;
import org.openapitools.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
public class CarePlanController implements EpisodeOfCaresApi, ConsentApi {

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

    @Override
    public ResponseEntity<EpisodeofcareDto> v1EpisodeofcaresIdGet(String id) {
        EpisodeofcareDto episodeOfCare = carePlanService.getEpisodeOfCare(id);

        return ResponseEntity.ok(episodeOfCare);
    }

    @Override
    public ResponseEntity<List<ConsentDto>> v1EpisodeofcaresIdConsentGet(String episodeOfCareId) {
        List<ConsentDto> consents = carePlanService.getConsents(episodeOfCareId);
        return ResponseEntity.ok(consents);
    }

    @Override
    public ResponseEntity<Void> v1EpisodeofcaresIdConsentPost(String episodeOfCareId, CreateConsentDto createConsentDto) {
        CreateConsentDto.CategoryEnum category = createConsentDto.getCategory();
        CreateConsentDto.StatusEnum status = createConsentDto.getStatus();

        String consentId = carePlanService.createConsent(episodeOfCareId, category, status);
        URI location = URI.create(ServletUriComponentsBuilder.fromCurrentRequestUri().path("/" + consentId).build().toString());
        return ResponseEntity.created(location).build();
    }

    @Override
    public ResponseEntity<ConsentDto> v1EpisodeofcaresEpisodeOfCareIdConsentConsentIdGet(String episodeOfCareId, String consentId) {
        ConsentDto consents = carePlanService.getConsents(episodeOfCareId, consentId);
        return ResponseEntity.ok(consents);
    }

    @Override
    public ResponseEntity<Void> v1EpisodeofcaresIdPatch(String id, UpdateEpisodeOfCareDto updateEpisodeOfCareDto) {
        OffsetDateTime start = updateEpisodeOfCareDto.getStart();
        OffsetDateTime end = updateEpisodeOfCareDto.getEnd();
        EpisodeOfCareStatusDto status = updateEpisodeOfCareDto.getStatus();
        String careTeamId = updateEpisodeOfCareDto.getCareTeamId();

        carePlanService.updateEpisodeOfCare(id, start, end, status, careTeamId);
        return ResponseEntity.ok().build();
    }
}
