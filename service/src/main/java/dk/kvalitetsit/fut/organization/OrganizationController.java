package dk.kvalitetsit.fut.organization;

import org.openapitools.api.OrganizationApi;
import org.openapitools.model.CareTeamDto;
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

    public OrganizationController(OrganizationServiceImpl organizationService) {
        this.organizationService = organizationService;
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

    @Override
    public ResponseEntity<CareTeamDto> v1CareteamsIdGet(String id) {
        CareTeamDto careTeam = null;
        try {
            careTeam = organizationService.getCareTeam(id);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Getting the CareTeam was not possible");
        }
        return ResponseEntity.ok(careTeam);
    }

    @Override
    public ResponseEntity<List<PatientDto>> v1CareteamsIdPatientsGet(String id) {
        List<PatientDto> patients = null;
        try {
            patients = organizationService.getPatientsForCareTeam();
        } catch (Exception e) {
            logger.error(e.toString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Getting the Patients for the CareTeam was not possible");
        }
        return ResponseEntity.ok(patients);
    }

    @Override
    public ResponseEntity<List<CareTeamDto>> v1ParticipantIdCareteamsGet(Integer participantId) {
        List<CareTeamDto> careTeams = null;
        try {
            careTeams = organizationService.getCareTeams(participantId);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Getting the CareTeam was not possible");
        }
        return ResponseEntity.ok(careTeams);
    }

}
