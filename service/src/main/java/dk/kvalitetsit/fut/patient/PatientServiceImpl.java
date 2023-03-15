package dk.kvalitetsit.fut.patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.openapitools.model.CreatePatientDto;
import org.openapitools.model.PatientDto;

import java.util.*;
import java.util.stream.Collectors;

import dk.kvalitetsit.fut.auth.AuthService;
import dk.kvalitetsit.fut.configuration.FhirLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientServiceImpl implements PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientServiceImpl.class);
    private final Map<String, PatientDto> patients = new HashMap<>();
    private FhirContext fhirContext;
    private String patientServiceUrl;
    private String organizationServiceUrl;
    private AuthService authService;

    public PatientServiceImpl(FhirContext fhirContext,
                              String patientServiceUrl,
                              String organizationServiceUrl,
                              AuthService authService) {
        this.fhirContext = fhirContext;
        this.patientServiceUrl = patientServiceUrl;
        this.organizationServiceUrl = organizationServiceUrl;
        this.authService = authService;
    }

    @Override
    public PatientDto getPatient(String careTeamId, String patientId) throws JsonProcessingException {
        String careTeamResource = organizationServiceUrl + "CareTeam/" + careTeamId;

        AuthService.Token token = authService.getToken();
        token = authService.refreshTokenWithCareTeamAndPatientContext(
                token, careTeamResource, patientId);

        IGenericClient client = getFhirClient(token);
        try {
            Patient patient = client.read().resource(Patient.class).withId(patientId).execute();
            return PatientMapper.mapPatient(patient);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return null;
    }

    @Override
    public PatientDto getPatient(String patientId) throws JsonProcessingException {
        throw new NotImplementedException();
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

        List<Patient> result = null;
        try {
            result = lookupByCriteria(Patient.class, criteria);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return result.stream()
                .map(patient -> PatientMapper.mapPatient(patient))
                .collect(Collectors.toList());
    }

    @Override
    public void removePatient(String uuid) {
        throw new NotImplementedException();
    }

    @Override
    public PatientDto createPatient(CreatePatientDto patient) {
        return constructPatient(patient);
    }

    private <T extends Resource> List<T> lookupByCriteria(Class<T> resourceClass, List<ICriterion> criteria)
            throws JsonProcessingException {
        AuthService.Token token = authService.getToken();
        IGenericClient client = getFhirClient(token);

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

    private IGenericClient getFhirClient(AuthService.Token token) {
        IGenericClient client = fhirContext.newRestfulGenericClient(patientServiceUrl);
        FhirLoggingInterceptor logInt = new FhirLoggingInterceptor(logger);
        client.registerInterceptor(logInt);

        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token.accessToken());
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
