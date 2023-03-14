package dk.kvalitetsit.fut.plandefinition;

import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.openapitools.model.PlandefinitionDto;

public class PlanDefinitionMapper {

    private static final String EMPLOYEE_TITLE = "http://ehealth.sundhed.dk/fhir/StructureDefinition/ehealth-employee-title";
    public static PlandefinitionDto mapPlanDefinition(PlanDefinition planDefinition) {
        PlandefinitionDto plandefinitionDto = new PlandefinitionDto();

        plandefinitionDto.setId(planDefinition.getIdElement().toUnqualifiedVersionless().getIdPart());
        plandefinitionDto.setStatus( mapPlanDefinitionStatus(planDefinition.getStatus()) );
        plandefinitionDto.setPatientTitle(planDefinition.getTitle());
        plandefinitionDto.setClinicianTitle(planDefinition.getExtensionByUrl(EMPLOYEE_TITLE).getValue().toString());

        return plandefinitionDto;
    }

    private static PlandefinitionDto.StatusEnum mapPlanDefinitionStatus(Enumerations.PublicationStatus status) {
        return switch (status) {
            case ACTIVE -> PlandefinitionDto.StatusEnum.ACTIVE;
            case DRAFT -> PlandefinitionDto.StatusEnum.DRAFT;
            case RETIRED -> PlandefinitionDto.StatusEnum.RETIRED;
            case UNKNOWN -> PlandefinitionDto.StatusEnum.UNKNOWN;
            case NULL -> null;
        };
    }
}
