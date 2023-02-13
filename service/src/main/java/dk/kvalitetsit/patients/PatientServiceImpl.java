package dk.kvalitetsit.patients;

import org.openapitools.model.CreatePatient;
import org.openapitools.model.Patient;

import java.util.*;

public class PatientServiceImpl implements PatientService{

    private final Map<String, Patient> patients = new HashMap<>();


    @Override
    public Patient getPatient(String uuid) throws Exception {
        boolean patientIsMissing = !this.patients.containsKey(uuid);
        if(patientIsMissing) throw new Exception("Patient not found");
        return patients.get(uuid);
    }

    @Override
    public List<Patient> getPatients() {
        return patients.values().stream().toList();
    }

    @Override
    public void removePatient(String uuid) throws Exception {
        getPatient(uuid);
        patients.remove(uuid);
    }

    @Override
    public Patient updatePatient(String uuid, CreatePatient createPatient) throws Exception {
        return patients.put(uuid, constructPatient(createPatient));
    }

    @Override
    public Patient createPatient(CreatePatient patient) {

        return constructPatient(patient);
    }


    private Patient constructPatient(CreatePatient createPatient) {
        Patient patient = new Patient();
        patient.setUuid(UUID.randomUUID().toString());
        patient.setFirstName(createPatient.getFirstName());
        patient.setLastName(createPatient.getLastName());
        patients.put(patient.getUuid(), patient);

        return patient;
    }


}
