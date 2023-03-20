package dk.kvalitetsit.fut.careplan;

import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.openapitools.model.CareplanDto;
import org.openapitools.model.CareplanStatusDto;

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

    private static CareplanStatusDto mapCarePlanStatus(CarePlan.CarePlanStatus status) {
        return switch (status) {
            case ACTIVE -> CareplanStatusDto.ACTIVE;
            case COMPLETED -> CareplanStatusDto.COMPLETED;
            case DRAFT -> CareplanStatusDto.DRAFT;
            case ENTEREDINERROR -> CareplanStatusDto.ENTERED_IN_ERROR;
            case ONHOLD -> CareplanStatusDto.ON_HOLD;
            case REVOKED -> CareplanStatusDto.REVOKED;
            case UNKNOWN -> CareplanStatusDto.UNKNOWN;
            case NULL -> null;
        };
    }

    public static CarePlan.CarePlanStatus mapCarePlanStatus(CareplanStatusDto status) {
        return switch (status) {
            case ACTIVE -> CarePlan.CarePlanStatus.ACTIVE;
            case COMPLETED -> CarePlan.CarePlanStatus.COMPLETED;
            case DRAFT -> CarePlan.CarePlanStatus.DRAFT;
            case ENTERED_IN_ERROR -> CarePlan.CarePlanStatus.ENTEREDINERROR;
            case ON_HOLD -> CarePlan.CarePlanStatus.ONHOLD;
            case REVOKED -> CarePlan.CarePlanStatus.REVOKED;
            case UNKNOWN -> CarePlan.CarePlanStatus.UNKNOWN;
            default -> CarePlan.CarePlanStatus.NULL;
        };
    }

    public static ServiceRequest.ServiceRequestStatus mapCarePlanStatusToServiceRequestStatus(CareplanStatusDto status) {
        return switch (status) {
            case ACTIVE -> ServiceRequest.ServiceRequestStatus.ACTIVE;
            case COMPLETED -> ServiceRequest.ServiceRequestStatus.COMPLETED;
            case DRAFT -> ServiceRequest.ServiceRequestStatus.DRAFT;
            case ENTERED_IN_ERROR -> ServiceRequest.ServiceRequestStatus.ENTEREDINERROR;
            case ON_HOLD -> ServiceRequest.ServiceRequestStatus.ONHOLD;
            case REVOKED -> ServiceRequest.ServiceRequestStatus.REVOKED;
            case UNKNOWN -> ServiceRequest.ServiceRequestStatus.UNKNOWN;
            default -> ServiceRequest.ServiceRequestStatus.NULL;
        };
    }
}
