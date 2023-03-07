package dk.kvalitetsit.fut.organization;

import org.openapitools.api.OrganizationApi;
import org.openapitools.model.CareTeamDto;
import org.openapitools.model.CreatePatientDto;
import org.openapitools.model.PatientDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class OrganizationController implements OrganizationApi {

    private final OrganizationServiceImpl organizationService;

    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);


    public OrganizationController(OrganizationServiceImpl patientService) {
        this.organizationService = patientService;
    }

    @Override
    public ResponseEntity<List<CareTeamDto>> v1CareteamsGet() {
        List<CareTeamDto> careTeams = null;
        try {
            careTeams = organizationService.getCareTeams();
        } catch (Exception e) {
            logger.error(e.toString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Getting the CareTeam was not possible");
        }
        return ResponseEntity.ok(careTeams);
    }

}
