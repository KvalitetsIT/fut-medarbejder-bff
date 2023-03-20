package dk.kvalitetsit.fut.consent;

import org.hl7.fhir.r4.model.Consent;
import org.openapitools.model.ConsentDto;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

public class ConsentMapper {

    private static ConsentDto.StatusEnum mapConsentStatus(Consent.ConsentState status) {
        return switch (status) {
            case ACTIVE -> ConsentDto.StatusEnum.ACTIVE;
            case DRAFT -> ConsentDto.StatusEnum.DRAFT;
            case ENTEREDINERROR -> ConsentDto.StatusEnum.ENTERED_IN_ERROR;
            case INACTIVE ->  ConsentDto.StatusEnum.INACTIVE;
            case PROPOSED -> ConsentDto.StatusEnum.PROPOSED;
            case REJECTED -> ConsentDto.StatusEnum.REJECTED;
            case NULL -> null;
        };

    }
    private static OffsetDateTime toOffsetDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.of("Europe/Copenhagen"));
    }

    public static ConsentDto mapConsent(Consent consent) {
        ConsentDto consentDto = new ConsentDto();

        consentDto.setId(consent.getIdElement().toUnqualifiedVersionless().getIdPart());
        consentDto.setStatus( mapConsentStatus(consent.getStatus()) );
        consentDto.setStart( toOffsetDateTime(consent.getProvision().getPeriod().getStart()) );
        consentDto.setEnd( toOffsetDateTime(consent.getProvision().getPeriod().getEnd()) );
        consentDto.setPatientId(consent.getPatient().getReferenceElement().toUnqualifiedVersionless().getIdPart());
        consentDto.setEpisodeOfCareId(consent.getProvision().getDataFirstRep().getReference().getReferenceElement().toUnqualifiedVersionless().getIdPart());

        return consentDto;
    }
}
