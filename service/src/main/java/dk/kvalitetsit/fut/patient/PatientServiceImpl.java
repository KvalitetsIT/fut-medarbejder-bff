package dk.kvalitetsit.fut.patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import dk.kvalitetsit.fut.auth.AuthService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.openapitools.model.CreatePatientDto;
import org.openapitools.model.PatientDto;

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
    public List<PatientDto> getPatients(String given, String family) {
        List<ICriterion> criteria = new ArrayList<>();
        if (given != null) {
            criteria.add(Patient.GIVEN.matches().value(given));
        }
        if (family != null) {
            criteria.add(Patient.FAMILY.matches().value(family));
        }

        List<Patient> result = lookupByCriteria(Patient.class, criteria);

        return result.stream()
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

    private <T extends Resource> List<T> lookupByCriteria(Class<T> resourceClass, List<ICriterion> criteria) {
        IGenericClient client = getFhirClient();

        IQuery<Bundle> query = client
                .search()
                .forResource(resourceClass)
                .returnBundle(Bundle.class);

        if (!criteria.isEmpty()) {
            query = query.where(criteria.get(0));
            for(int i = 1; i < criteria.size(); i++) {
                query = query.and(criteria.get(i));
            }
        }

        Bundle result = query.execute();

        return result.getEntry().stream()
                .map(bundleEntryComponent -> (T)bundleEntryComponent.getResource())
                .collect(Collectors.toList());
    }

    private IGenericClient getFhirClient() {
        BearerTokenAuthInterceptor authInterceptor = null;
        try {
            authInterceptor = new BearerTokenAuthInterceptor( authService.getToken().accessToken() );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        IGenericClient client = fhirContext.newRestfulGenericClient(patientServiceUrl);
        client.registerInterceptor(authInterceptor);
        return client;
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
