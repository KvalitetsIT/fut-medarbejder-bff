package dk.kvalitetsit.fut.patient;

import org.openapitools.model.CreatePatient;
import org.openapitools.model.Patient;

import java.util.List;

public interface PatientService {
    Patient getPatient(String uuid) throws Exception;

    List<Patient> getPatients();

    void removePatient(String uuid) throws Exception;

    Patient createPatient(CreatePatient patient);
}
