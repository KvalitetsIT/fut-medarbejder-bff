package dk.kvalitetsit.fut.clinicalimpression;

import org.openapitools.api.ClinicalImpressionApi;
import org.openapitools.model.ClinicalimpressionDto;
import org.openapitools.model.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClinicalImpressionController implements ClinicalImpressionApi {
    private final ClinicalImpressionServiceImpl clinicalImpressionService;
    private static final Logger logger = LoggerFactory.getLogger(ClinicalImpressionController.class);

    public ClinicalImpressionController(ClinicalImpressionServiceImpl clinicalImpressionService) {
        this.clinicalImpressionService = clinicalImpressionService;
    }

    @Override
    public ResponseEntity<ClinicalimpressionDto> v1GetClinicalImpressionForEpisodeOfCare(String episodeOfCareId, String clinicalImpressionId) {
        ClinicalimpressionDto clinicalimpressionDto = clinicalImpressionService.getClinicalImpression(episodeOfCareId, clinicalImpressionId);
        return ResponseEntity.ok(clinicalimpressionDto);
    }
}
