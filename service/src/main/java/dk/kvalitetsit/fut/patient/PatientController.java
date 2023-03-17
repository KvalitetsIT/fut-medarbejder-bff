package dk.kvalitetsit.fut.patient;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.openapitools.api.PatientApi;
import org.openapitools.model.CreatePatientDto;
import org.openapitools.model.PatientDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
public class PatientController implements PatientApi {
    private final PatientService patientService;
    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @Override
    public ResponseEntity<List<PatientDto>> v1GetPatients(String given, String family, String cpr) {
        List<PatientDto> patients;
        if (cpr != null) {
            patients = patientService.searchPatient(cpr);
        } else {
            patients = patientService.searchPatients(given, family);
        }
        return ResponseEntity.ok(patients);
    }

    @Override
    public ResponseEntity<PatientDto> v1GetPatient(String patientId, String careTeamId) {
        PatientDto patientDto = null;
        try {
            patientDto = patientService.getPatient(patientId, careTeamId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(patientDto);
    }

    @Override
    public ResponseEntity<PatientDto> v1PostPatient(CreatePatientDto createPatientDto) {
        String cpr = createPatientDto.getCpr();
        String patientId = patientService.createPatient(cpr);
        URI location = URI.create(ServletUriComponentsBuilder.fromCurrentRequestUri().path("/" + patientId).build().toString());
        return ResponseEntity.created(location).build();
    }
}
