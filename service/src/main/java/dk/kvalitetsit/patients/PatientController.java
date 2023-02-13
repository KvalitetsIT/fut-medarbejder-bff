package dk.kvalitetsit.patients;

import dk.kvalitetsit.hello.controller.HelloController;
import org.openapitools.api.CreatePatientApi;
import org.openapitools.api.PatientApi;
import org.openapitools.api.PatientsApi;
import org.openapitools.model.CreatePatient;
import org.openapitools.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
public class PatientController implements PatientApi, PatientsApi, CreatePatientApi {

    private final PatientService service;

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);


    public PatientController() {
        service = new PatientServiceImpl();
    }

    @Override
    public ResponseEntity<Void> v1PatientsIdDelete(String id) {
        try{
            service.removePatient(id);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Removing the patient was not possible");
        }
    }

    @Override
    public ResponseEntity<Patient> v1PatientsPost(CreatePatient createPatient){
        try {
            System.out.println(createPatient.toString());
            Patient patient = service.createPatient(createPatient);
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Creating the patient was not possible");
        }
    }

    @Override
    public ResponseEntity<Patient> v1PatientsIdGet(String id) {
        try {
            Patient patient = service.getPatient(id);
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Removing the patient was not possible");
        }
    }

    @Override
    public ResponseEntity<Patient> v1PatientsIdPut(String id, CreatePatient createPatient) {
        try {
            Patient patient = service.updatePatient(id, createPatient);
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Updating the patient was not possible");
        }
    }

    @Override
    public ResponseEntity<List<Patient>> v1PatientsGet() {
        List<Patient> patients = service.getPatients();
        return ResponseEntity.ok(patients);
    }


}
