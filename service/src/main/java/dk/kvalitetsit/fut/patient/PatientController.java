package dk.kvalitetsit.fut.patient;

import org.openapitools.api.PatientsApi;
import org.openapitools.model.CreatePatient;
import org.openapitools.model.Patient;
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
    public ResponseEntity<Patient> v1PatientsPost(CreatePatient createPatient){
        try {
            System.out.println(createPatient.toString());
            Patient patient = patientService.createPatient(createPatient);
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Creating the patient was not possible");
        }
    }

    @Override
    public ResponseEntity<Patient> v1PatientsIdGet(String id) {
        try {
            Patient patient = patientService.getPatient(id);
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Removing the patient was not possible");
        }
    }

    @Override
    public ResponseEntity<List<Patient>> v1PatientsGet() {
        List<Patient> patients = patientService.getPatients();
        return ResponseEntity.ok(patients);
    }
}
