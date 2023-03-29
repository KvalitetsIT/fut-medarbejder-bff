package dk.kvalitetsit.fut.task;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.openapitools.model.TaskDto;
import org.openapitools.model.TaskStatusDto;

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

        if (task.hasExtension("http://ehealth.sundhed.dk/fhir/StructureDefinition/ehealth-task-episodeOfCare")) {
            Extension ext = task.getExtensionByUrl("http://ehealth.sundhed.dk/fhir/StructureDefinition/ehealth-task-episodeOfCare");
            if (ext.getValue() instanceof Reference) {
                String episodeOfCareId = ((Reference) ext.getValue()).getReferenceElement().toUnqualifiedVersionless().getIdPart();
                taskDto.setEpisodeOfCareId(episodeOfCareId);
            }
        }

        return taskDto;
    }

    private static TaskStatusDto mapTaskStatus(Task.TaskStatus status) {
        return switch (status) {
            case DRAFT ->  TaskStatusDto.DRAFT;
            case REQUESTED -> TaskStatusDto.REQUESTED;
            case RECEIVED -> TaskStatusDto.RECEIVED;
            case ACCEPTED -> TaskStatusDto.ACCEPTED;
            case REJECTED ->  TaskStatusDto.REJECTED;
            case READY -> TaskStatusDto.READY;
            case CANCELLED -> TaskStatusDto.CANCELLED;
            case INPROGRESS -> TaskStatusDto.IN_PROGRESS;
            case ONHOLD -> TaskStatusDto.ON_HOLD;
            case FAILED -> TaskStatusDto.FAILED;
            case COMPLETED -> TaskStatusDto.COMPLETED;
            case ENTEREDINERROR -> TaskStatusDto.ENTERED_IN_ERROR;
            case NULL -> null;
        };
    }

    public static Task.TaskStatus mapTaskStatus(TaskStatusDto status) {
        if (status == null) {
            return Task.TaskStatus.NULL;
        }

        return switch (status) {
            case DRAFT ->  Task.TaskStatus.DRAFT;
            case REQUESTED -> Task.TaskStatus.REQUESTED;
            case RECEIVED -> Task.TaskStatus.RECEIVED;
            case ACCEPTED -> Task.TaskStatus.ACCEPTED;
            case REJECTED ->  Task.TaskStatus.REJECTED;
            case READY -> Task.TaskStatus.READY;
            case CANCELLED -> Task.TaskStatus.CANCELLED;
            case IN_PROGRESS -> Task.TaskStatus.INPROGRESS;
            case ON_HOLD -> Task.TaskStatus.ONHOLD;
            case FAILED -> Task.TaskStatus.FAILED;
            case COMPLETED -> Task.TaskStatus.COMPLETED;
            case ENTERED_IN_ERROR -> Task.TaskStatus.ENTEREDINERROR;
        };
    }

    private static OffsetDateTime toOffsetDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.of("Europe/Copenhagen"));
    }
}
