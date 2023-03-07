package dk.kvalitetsit.fut.patient;

import org.openapitools.api.PatientsApi;
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
public class PatientController implements PatientsApi {

    private final PatientServiceImpl patientService;

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);


    public PatientController(PatientServiceImpl patientService) {
        this.patientService = patientService;
    }


    @Override
    public ResponseEntity<PatientDto> v1PatientsPost(CreatePatientDto createPatient){
        try {
            System.out.println(createPatient.toString());
            PatientDto patient = patientService.createPatient(createPatient);
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Creating the patient was not possible");
        }
    }

    @Override
    public ResponseEntity<PatientDto> v1PatientsIdGet(String id) {
        try {
            PatientDto patient = patientService.getPatient(id);
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Removing the patient was not possible");
        }
    }

    @Override
    public ResponseEntity<List<PatientDto>> v1PatientsGet() {
        List<PatientDto> patients = patientService.getPatients();
        return ResponseEntity.ok(patients);
    }
}
