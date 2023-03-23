package dk.kvalitetsit.fut.task;

import org.openapitools.api.TaskApi;
import org.openapitools.model.TaskDto;
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
    public ResponseEntity<List<TaskDto>> v1GetTasksForCareTeam(String careTeamId) {
        List<TaskDto> tasks = taskService.getTasks(careTeamId);
        return ResponseEntity.ok(tasks);
        //return ResponseEntity.ok(patients);
    }
}
