package dk.kvalitetsit.fut.consent;

import dk.kvalitetsit.fut.episodeofcare.EpisodeOfCareService;
import org.openapitools.api.ConsentApi;
import org.openapitools.model.ConsentDto;
import org.openapitools.model.CreateConsentDto;
import org.openapitools.model.EpisodeofcareDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
public class ConsentController implements ConsentApi {

    private final ConsentService consentService;
    private final EpisodeOfCareService episodeOfCareService;

    private static final Logger logger = LoggerFactory.getLogger(ConsentController.class);


    public ConsentController(ConsentService consentService, EpisodeOfCareService episodeOfCareService) {
        this.consentService = consentService;
        this.episodeOfCareService = episodeOfCareService;
    }


    @Override
    public ResponseEntity<ConsentDto> v1GetConsentForEpisodeOfCare(String episodeOfCareId, String consentId) {
        ConsentDto consents = consentService.getConsent(episodeOfCareId, consentId);
        return ResponseEntity.ok(consents);
    }

    @Override
    public ResponseEntity<List<ConsentDto>> v1GetConsentsForEpisodeOfCare(String episodeOfCareId) {
        List<ConsentDto> consents = consentService.getConsents(episodeOfCareId);
        return ResponseEntity.ok(consents);
    }

    @Override
    public ResponseEntity<Void> v1PostConsentForEpisodeOfCare(String episodeOfCareId, CreateConsentDto createConsentDto) {
        CreateConsentDto.CategoryEnum category = createConsentDto.getCategory();
        CreateConsentDto.StatusEnum status = createConsentDto.getStatus();

        EpisodeofcareDto episodeOfCare = episodeOfCareService.getEpisodeOfCare(episodeOfCareId);
        String patientId = episodeOfCare.getPatientId();

        String consentId = consentService.createConsent(episodeOfCareId, patientId, category, status);
        URI location = URI.create(ServletUriComponentsBuilder.fromCurrentRequestUri().path("/" + consentId).build().toString());
        return ResponseEntity.created(location).build();
    }
}
