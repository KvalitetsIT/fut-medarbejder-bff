package dk.kvalitetsit.fut.plandefinition;

import org.openapitools.model.PlandefinitionDto;

import java.util.List;

public interface PlanDefinitionService {
    List<PlandefinitionDto> getPlanDefinitions(String title);
}
