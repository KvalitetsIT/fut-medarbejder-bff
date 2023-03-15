package dk.kvalitetsit.fut.consent;

import org.openapitools.model.ConsentDto;
import org.openapitools.model.CreateConsentDto;

import java.util.List;

public interface ConsentService {

    ConsentDto getConsent(String episodeOfCareId, String consentId);
    List<ConsentDto> getConsents(String episodeOfCareId);
    String createConsent(String episodeOfCareId, String patientId, CreateConsentDto.CategoryEnum category, CreateConsentDto.StatusEnum status);
}
