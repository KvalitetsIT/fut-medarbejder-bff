package dk.kvalitetsit.fut.plandefinition;

import org.openapitools.api.PlanDefinitionApi;
import org.openapitools.model.PlandefinitionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PlanDefinitionController implements PlanDefinitionApi {

    private final PlanDefinitionService planDefinitionService;

    private static final Logger logger = LoggerFactory.getLogger(PlanDefinitionController.class);


    public PlanDefinitionController(PlanDefinitionService planDefinitionService) {
        this.planDefinitionService = planDefinitionService;
    }

    @Override
    public ResponseEntity<List<PlandefinitionDto>> v1GetPlanDefinitions(String title) {
        List<PlandefinitionDto> plandefinitions = planDefinitionService.getPlanDefinitions(title);
        return ResponseEntity.ok(plandefinitions);
    }
}
