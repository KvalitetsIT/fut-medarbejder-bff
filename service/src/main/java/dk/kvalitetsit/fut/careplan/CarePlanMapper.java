package dk.kvalitetsit.fut.careplan;

import org.hl7.fhir.r4.model.EpisodeOfCare;
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
        episodeofcareDto.setEnd( toOffsetDateTime(episodeOfCare.getPeriod().getStart()) );
        episodeofcareDto.setPatientId(episodeOfCare.getPatient().getReferenceElement().toUnqualifiedVersionless().getIdPart());

        return episodeofcareDto;
    }

    private static EpisodeofcareDto.StatusEnum mapEpisodeOfCareStatus(EpisodeOfCare.EpisodeOfCareStatus status) {
        return switch (status) {
            case ACTIVE -> EpisodeofcareDto.StatusEnum.ACTIVE;
            case PLANNED -> EpisodeofcareDto.StatusEnum.PLANNED;
            case ONHOLD -> EpisodeofcareDto.StatusEnum.ONHOLD;
            case FINISHED -> EpisodeofcareDto.StatusEnum.FINISHED;
            case WAITLIST -> EpisodeofcareDto.StatusEnum.WAITLIST;
            case CANCELLED -> EpisodeofcareDto.StatusEnum.CANCELLED;
            case ENTEREDINERROR -> EpisodeofcareDto.StatusEnum.ENTERED_IN_ERROR;
            case NULL -> null;
        };

    }
    private static OffsetDateTime toOffsetDateTime(Date date) {
        return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.of("Europe/Copenhagen"));
    }
}
