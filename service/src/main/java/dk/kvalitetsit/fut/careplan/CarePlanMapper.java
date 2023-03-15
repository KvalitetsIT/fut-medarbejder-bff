package dk.kvalitetsit.fut.careplan;

import org.hl7.fhir.r4.model.CarePlan;
import org.openapitools.model.CareplanDto;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

public class CarePlanMapper {

    private static OffsetDateTime toOffsetDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.of("Europe/Copenhagen"));
    }

    public static CareplanDto mapCarePlan(CarePlan carePlan) {
        CareplanDto careplanDto = new CareplanDto();

        careplanDto.setId(carePlan.getIdElement().toUnqualifiedVersionless().getIdPart());
        careplanDto.setStatus( mapCarePlanStatus(carePlan.getStatus()) );
        careplanDto.setPatientId(carePlan.getSubject().getReferenceElement().toUnqualifiedVersionless().getIdPart());
        careplanDto.setCareTeamId(carePlan.getCareTeamFirstRep().getReferenceElement().toUnqualifiedVersionless().getIdPart());
        careplanDto.setStart( toOffsetDateTime(carePlan.getPeriod().getStart()) );
        careplanDto.setEnd( toOffsetDateTime(carePlan.getPeriod().getEnd()) );

        return careplanDto;
    }

    private static CareplanDto.StatusEnum mapCarePlanStatus(CarePlan.CarePlanStatus status) {
        return switch (status) {
            case ACTIVE -> CareplanDto.StatusEnum.ACTIVE;
            case COMPLETED -> CareplanDto.StatusEnum.COMPLETED;
            case DRAFT -> CareplanDto.StatusEnum.DRAFT;
            case ENTEREDINERROR -> CareplanDto.StatusEnum.ENTERED_IN_ERROR;
            case ONHOLD -> CareplanDto.StatusEnum.ON_HOLD;
            case REVOKED -> CareplanDto.StatusEnum.REVOKED;
            case UNKNOWN -> CareplanDto.StatusEnum.UNKNOWN;
            case NULL -> null;
        };
    }
}
