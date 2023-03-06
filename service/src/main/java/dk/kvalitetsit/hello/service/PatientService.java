package dk.kvalitetsit.hello.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hl7.fhir.r4.model.Bundle;
import org.openapitools.model.CreatePatient;
import org.openapitools.model.Patient;

import java.util.*;

public class PatientService {

    private final Map<String, Patient> patients = new HashMap<>();
    private IGenericClient fhirClient;
    private AuthService authService;


    public PatientService(IGenericClient fhirClient, AuthService authService) {
        this.fhirClient = fhirClient;
        this.authService = authService;
    }


    public Patient getPatient(String uuid) throws Exception {
        boolean patientIsMissing = !this.patients.containsKey(uuid);
        if(patientIsMissing) throw new Exception("Patient not found");
        return patients.get(uuid);
    }

    public List<Patient> getPatients() {
        List<Patient> patients = new ArrayList<>();

        BearerTokenAuthInterceptor authInterceptor = null;
        try {
            authInterceptor = new BearerTokenAuthInterceptor( authService.getToken() );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        fhirClient.registerInterceptor(authInterceptor);

        Bundle result = fhirClient
                .search()
                .forResource(org.hl7.fhir.r4.model.Patient.class)
                .returnBundle(Bundle.class)
                .execute();
        result.getEntry().forEach(b -> System.out.println(b.getFullUrl()));

        for (Bundle.BundleEntryComponent bec : result.getEntry()) {
            org.hl7.fhir.r4.model.Patient fhirPatient = (org.hl7.fhir.r4.model.Patient)bec.getResource();

            Patient patient = new Patient();
            patient.setFirstName(fhirPatient.getNameFirstRep().getGivenAsSingleString());
            patient.setLastName(fhirPatient.getNameFirstRep().getNameAsSingleString());

            patients.add(patient);
        }

        return patients;
        //return patients.values().stream().toList();
    }

    public void removePatient(String uuid) throws Exception {
        getPatient(uuid);
        patients.remove(uuid);
    }

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
