package dk.kvalitetsit.fut.patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import dk.kvalitetsit.fut.auth.AuthService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.openapitools.model.PatientDto;
import org.openapitools.model.CreatePatientDto;

import java.util.*;
import java.util.stream.Collectors;

public class PatientServiceImpl implements PatientService {

    private final Map<String, PatientDto> patients = new HashMap<>();
    private FhirContext fhirContext;
    private String patientServiceUrl;
    private AuthService authService;

    public PatientServiceImpl(FhirContext fhirContext, String patientServiceUrl, AuthService authService) {
        this.fhirContext = fhirContext;
        this.patientServiceUrl = patientServiceUrl;
        this.authService = authService;
    }

    @Override
    public PatientDto getPatient(String uuid) throws Exception {
        boolean patientIsMissing = !this.patients.containsKey(uuid);
        if(patientIsMissing) throw new Exception("Patient not found");
        return patients.get(uuid);
    }

    @Override
    public List<PatientDto> getPatients() {
        BearerTokenAuthInterceptor authInterceptor = null;
        try {
            authInterceptor = new BearerTokenAuthInterceptor( authService.getToken() );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        IGenericClient client = fhirContext.newRestfulGenericClient(patientServiceUrl);
        client.registerInterceptor(authInterceptor);

        Bundle result = client
                .search()
                .forResource(Patient.class)
                .returnBundle(Bundle.class)
                .execute();

        return result.getEntry().stream()
                .map(bundleEntryComponent -> (Patient)bundleEntryComponent.getResource())
                .map(patient -> PatientMapper.mapPatient(patient))
                .collect(Collectors.toList());
    }

    @Override
    public void removePatient(String uuid) throws Exception {
        getPatient(uuid);
        patients.remove(uuid);
    }

    @Override
    public PatientDto createPatient(CreatePatientDto patient) {

        return constructPatient(patient);
    }


    private PatientDto constructPatient(CreatePatientDto createPatient) {
        PatientDto patient = new PatientDto();
        patient.setUuid(UUID.randomUUID().toString());
        patient.setFirstName(createPatient.getFirstName());
        patient.setLastName(createPatient.getLastName());
        patients.put(patient.getUuid(), patient);

        return patient;
    }


}
