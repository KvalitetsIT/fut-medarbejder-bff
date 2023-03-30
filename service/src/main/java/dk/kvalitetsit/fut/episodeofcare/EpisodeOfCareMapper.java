package dk.kvalitetsit.fut.episodeofcare;

import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.openapitools.model.EpisodeOfCareStatusDto;
import org.openapitools.model.EpisodeofcareDto;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

public class EpisodeOfCareMapper {

    public static EpisodeofcareDto mapEpisodeOfCare(EpisodeOfCare episodeOfCare, String conditionCode) {
        EpisodeofcareDto episodeofcareDto = new EpisodeofcareDto();

        episodeofcareDto.setUuid(episodeOfCare.getIdElement().toUnqualifiedVersionless().getIdPart());
        episodeofcareDto.setStatus( mapEpisodeOfCareStatus(episodeOfCare.getStatus()) );
        episodeofcareDto.setStart( toOffsetDateTime(episodeOfCare.getPeriod().getStart()) );
        episodeofcareDto.setEnd( toOffsetDateTime(episodeOfCare.getPeriod().getEnd()) );
        episodeofcareDto.setPatientId(episodeOfCare.getPatient().getReferenceElement().toUnqualifiedVersionless().getIdPart());
        episodeofcareDto.setConditionCode(conditionCode);
        episodeofcareDto.setCareteamId( episodeOfCare.getTeamFirstRep().getReferenceElement().toUnqualifiedVersionless().getIdPart() );

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

    private static OffsetDateTime toOffsetDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.of("Europe/Copenhagen"));
    }
}
