package dk.kvalitetsit.fut.careplan;

import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.openapitools.model.ConsentDto;
import org.openapitools.model.EpisodeOfCareStatusDto;
import org.openapitools.model.EpisodeofcareDto;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

public class CarePlanMapper {

    public static EpisodeofcareDto mapEpisodeOfCare(EpisodeOfCare episodeOfCare) {
        EpisodeofcareDto episodeofcareDto = new EpisodeofcareDto();

        episodeofcareDto.setUuid(episodeOfCare.getIdElement().toUnqualifiedVersionless().getIdPart());
        episodeofcareDto.setStatus( mapEpisodeOfCareStatus(episodeOfCare.getStatus()) );
        episodeofcareDto.setStart( toOffsetDateTime(episodeOfCare.getPeriod().getStart()) );
        episodeofcareDto.setEnd( toOffsetDateTime(episodeOfCare.getPeriod().getEnd()) );
        episodeofcareDto.setPatientId(episodeOfCare.getPatient().getReferenceElement().toUnqualifiedVersionless().getIdPart());

        return episodeofcareDto;
    }

    private static EpisodeOfCareStatusDto mapEpisodeOfCareStatus(EpisodeOfCare.EpisodeOfCareStatus status) {
        return switch (status) {
            case ACTIVE -> EpisodeOfCareStatusDto.ACTIVE;
            case PLANNED -> EpisodeOfCareStatusDto.PLANNED;
            case ONHOLD -> EpisodeOfCareStatusDto.ONHOLD;
            case FINISHED -> EpisodeOfCareStatusDto.FINISHED;
            case WAITLIST -> EpisodeOfCareStatusDto.WAITLIST;
            case CANCELLED -> EpisodeOfCareStatusDto.CANCELLED;
            case ENTEREDINERROR -> EpisodeOfCareStatusDto.ENTERED_IN_ERROR;
            case NULL -> null;
        };
    }

    public static EpisodeOfCare.EpisodeOfCareStatus mapEpisodeOfCareStatus(EpisodeOfCareStatusDto status) {
        return switch (status) {
            case ACTIVE -> EpisodeOfCare.EpisodeOfCareStatus.ACTIVE;
            case PLANNED -> EpisodeOfCare.EpisodeOfCareStatus.PLANNED;
            case ONHOLD -> EpisodeOfCare.EpisodeOfCareStatus.ONHOLD;
            case FINISHED -> EpisodeOfCare.EpisodeOfCareStatus.FINISHED;
            case WAITLIST -> EpisodeOfCare.EpisodeOfCareStatus.WAITLIST;
            case CANCELLED -> EpisodeOfCare.EpisodeOfCareStatus.CANCELLED;
            case ENTERED_IN_ERROR -> EpisodeOfCare.EpisodeOfCareStatus.ENTEREDINERROR;
            default -> EpisodeOfCare.EpisodeOfCareStatus.NULL;
        };

    }

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

        // reference to EpisodeOfCare will look like 'https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/118621'
        String ref = consent.getProvision().getDataFirstRep().getReference().getReference();
        int start = ref.lastIndexOf("/") + 1;
        String episodeOfCareId = ref.substring(start);

        consentDto.setEpisodeOfCareId(episodeOfCareId);


        return consentDto;
    }
}
