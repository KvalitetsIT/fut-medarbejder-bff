package dk.kvalitetsit.fut.task;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Task;
import org.openapitools.model.TaskDto;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TaskMapper {

    private static final String EMPLOYEE_TITLE = "http://ehealth.sundhed.dk/fhir/StructureDefinition/ehealth-employee-title";
    public static TaskDto mapTask(Task task) {
        TaskDto taskDto = new TaskDto();

        taskDto.setId(task.getIdElement().toUnqualifiedVersionless().getIdPart());
        taskDto.setStatus( mapTaskStatus(task.getStatus()) );
        taskDto.setPriority(task.getPriorityElement().asStringValue());
        taskDto.setDescription(task.getDescription());
        taskDto.setClinicalImpressionId(task.getFocus().getReferenceElement().toUnqualifiedVersionless().getIdPart());
        taskDto.setAuthoredDate( toOffsetDateTime(task.getAuthoredOn()) );

        if (task.hasExtension("http://ehealth.sundhed.dk/fhir/StructureDefinition/ehealth-task-category")) {
            Extension ext = task.getExtensionByUrl("http://ehealth.sundhed.dk/fhir/StructureDefinition/ehealth-task-category");
            if (ext.getValue() instanceof CodeableConcept) {
                String category = ((CodeableConcept) ext.getValue()).getCodingFirstRep().getDisplay();
                taskDto.setCategory(category);
            }
        }
        return taskDto;
    }

    private static TaskDto.StatusEnum mapTaskStatus(Task.TaskStatus status) {
        return switch (status) {
            case DRAFT ->  TaskDto.StatusEnum.DRAFT;
            case REQUESTED -> TaskDto.StatusEnum.REQUESTED;
            case RECEIVED -> TaskDto.StatusEnum.RECEIVED;
            case ACCEPTED -> TaskDto.StatusEnum.ACCEPTED;
            case REJECTED ->  TaskDto.StatusEnum.REJECTED;
            case READY -> TaskDto.StatusEnum.READY;
            case CANCELLED -> TaskDto.StatusEnum.CANCELLED;
            case INPROGRESS -> TaskDto.StatusEnum.IN_PROGRESS;
            case ONHOLD -> TaskDto.StatusEnum.ON_HOLD;
            case FAILED -> TaskDto.StatusEnum.FAILED;
            case COMPLETED -> TaskDto.StatusEnum.COMPLETED;
            case ENTEREDINERROR -> TaskDto.StatusEnum.ENTERED_IN_ERROR;
            case NULL -> null;
        };
    }

    private static OffsetDateTime toOffsetDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.of("Europe/Copenhagen"));
    }
}
