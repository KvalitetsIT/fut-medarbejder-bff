package dk.kvalitetsit.fut.task;

import org.openapitools.api.TaskApi;
import org.openapitools.model.TaskDto;
import org.openapitools.model.TaskStatusDto;
import org.openapitools.model.UpdateTaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TaskController implements TaskApi {
    private final TaskServiceImpl taskService;
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    public TaskController(TaskServiceImpl taskService) {
        this.taskService = taskService;
    }

    @Override
    public ResponseEntity<List<TaskDto>> v1GetTasksForCareTeam(String careTeamId, String status) {
        List<TaskDto> tasks = taskService.getTasks(careTeamId, status);
        return ResponseEntity.ok(tasks);
        //return ResponseEntity.ok(patients);
    }

    @Override
    public ResponseEntity<TaskDto> v1GetTaskForCareTeam(String careTeamId, String taskId) {
        TaskDto task = taskService.getTaskById(careTeamId, taskId);
        return ResponseEntity.ok(task);
    }

    @Override
    public ResponseEntity<Void> v1PatchTask(String episodeOfCareId, String taskId, UpdateTaskDto updateTaskDto) {
        TaskStatusDto status = updateTaskDto.getStatus();
        taskService.updateTask(episodeOfCareId, taskId, status);

        return ResponseEntity.ok().build();
    }
}
