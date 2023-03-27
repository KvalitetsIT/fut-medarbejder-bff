package dk.kvalitetsit.fut.clinicalimpression;

import org.hl7.fhir.r4.model.*;
import org.openapitools.model.ClinicalimpressionDto;
import org.openapitools.model.TaskDto;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClinicalImpressionMapper {

    private static OffsetDateTime toOffsetDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.of("Europe/Copenhagen"));
    }

    public static ClinicalimpressionDto mapClinicalImpression(ClinicalImpression clinicalImpression) {
        ClinicalimpressionDto clinicalimpressionDto = new ClinicalimpressionDto();

        clinicalimpressionDto.setId(clinicalImpression.getIdElement().toUnqualifiedVersionless().getIdPart());
        clinicalimpressionDto.setType(clinicalImpression.getCode().getCodingFirstRep().getDisplay());
        clinicalimpressionDto.setDate( toOffsetDateTime(clinicalImpression.getDate()) );

        // CarePlan
        if (clinicalImpression.hasExtension("http://ehealth.sundhed.dk/fhir/StructureDefinition/ehealth-clinicalimpression-careplan")) {
            Extension ext = clinicalImpression.getExtensionByUrl("http://ehealth.sundhed.dk/fhir/StructureDefinition/ehealth-clinicalimpression-careplan");
            if (ext.getValue() instanceof Reference) {
                String carePlanId = ((Reference) ext.getValue()).getReferenceElement().toUnqualifiedVersionless().getIdPart();
                clinicalimpressionDto.setCareplanId(carePlanId);
            }
        }

        // QuestionnaireResponse
        boolean found = false;
        for (ClinicalImpression.ClinicalImpressionInvestigationComponent clinicalImpressionInvestigationComponent : clinicalImpression.getInvestigation()) {
            for (Reference reference : clinicalImpressionInvestigationComponent.getItem()) {
                if (reference.getReferenceElement().getResourceType().matches("QuestionnaireResponse")) {
                    String questionnaireResponseId = reference.getReferenceElement().toUnqualifiedVersionless().getIdPart();
                    clinicalimpressionDto.setQuestionnaireResponseId(questionnaireResponseId);
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }

        if (clinicalImpression.hasFinding()) {
            String finding = clinicalImpression.getFindingFirstRep().getItemCodeableConcept().getCodingFirstRep().getDisplay();
            clinicalimpressionDto.setFinding(finding);
        }

        return clinicalimpressionDto;
    }
}
