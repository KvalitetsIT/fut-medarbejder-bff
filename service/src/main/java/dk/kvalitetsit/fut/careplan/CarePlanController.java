package dk.kvalitetsit.fut.careplan;

import org.openapitools.api.CarePlanApi;
import org.openapitools.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
public class CarePlanController implements CarePlanApi {

    private final CarePlanService carePlanService;

    private static final Logger logger = LoggerFactory.getLogger(CarePlanController.class);


    public CarePlanController(CarePlanService carePlanService) {
        this.carePlanService = carePlanService;
    }


    @Override
    public ResponseEntity<CareplanDto> v1GetCarePlanForEpisodeOfCare(String episodeOfCareId, String careplanId) {
        CareplanDto careplan = carePlanService.getCarePlan(episodeOfCareId, careplanId);
        return ResponseEntity.ok(careplan);
    }

    @Override
    public ResponseEntity<List<CareplanDto>> v1GetCarePlansForCareTeam(String careTeamId) {
        List<CareplanDto> careplans = carePlanService.getCarePlansForCareTeam(careTeamId);
        return ResponseEntity.ok(careplans);

    }

    @Override
    public ResponseEntity<Void> v1PostCarePlanForEpisodeOfCare(String episodeOfCareId, CreateCarePlanDto createCarePlanDto) {
        String planDefinitionId = createCarePlanDto.getPlandefinitionId();

        String carePlanId = carePlanService.createCarePlan(episodeOfCareId, planDefinitionId);
        URI location = URI.create(ServletUriComponentsBuilder.fromCurrentRequestUri().path("/" + carePlanId).build().toString());
        return ResponseEntity.created(location).build();
    }

    @Override
    public ResponseEntity<Void> v1PatchCarePlan(String episodeOfCareId, String careplanId, CareplanUpdateDto careplanUpdateDto) {
        OffsetDateTime start = careplanUpdateDto.getStart();
        OffsetDateTime end = careplanUpdateDto.getEnd();
        CareplanStatusDto status = careplanUpdateDto.getStatus();
        String careTeamId = careplanUpdateDto.getCareTeamId();

        carePlanService.updateCarePlan(episodeOfCareId, careplanId, start, end, status, careTeamId);
        return ResponseEntity.ok().build();
    }
}
