package dk.kvalitetsit.fut.patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.SearchStyleEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import com.fasterxml.jackson.core.JsonProcessingException;

import dk.kvalitetsit.fut.auth.AuthService;
import org.hl7.fhir.r4.model.*;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

import org.openapitools.model.PatientDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dk.kvalitetsit.fut.configuration.FhirLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientServiceImpl implements PatientService {
    private static final Logger logger = LoggerFactory.getLogger(PatientServiceImpl.class);
    private FhirContext fhirContext;
    private String patientServiceUrl;
    private String organizationServiceUrl;
    private AuthService authService;

    public PatientServiceImpl(FhirContext fhirContext, String patientServiceUrl, String organizationServiceUrl, AuthService authService) {
        this.fhirContext = fhirContext;
        this.patientServiceUrl = patientServiceUrl;
        this.organizationServiceUrl = organizationServiceUrl;
        this.authService = authService;
    }

    @Override
    public PatientDto getPatient(String patientId, String careTeamId) throws JsonProcessingException {
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
    public String createPatient(String cpr) {
        Parameters parameters = new Parameters();
        parameters.addParameter()
                .setName("crn")
                .setValue(new Identifier().setSystem("urn:oid:1.2.208.176.1.2").setValue(cpr));

        IGenericClient client = getFhirClient(authService.getToken());
        Patient patient = client.operation()
                .onType(Patient.class)
                .named("$createPatient")
                .withParameters(parameters)
                .returnResourceType(Patient.class)
                .execute();

        return patient.getIdElement().toUnqualifiedVersionless().getIdPart();
    }

    @Override
    public List<PatientDto> searchPatients(String given, String family) {
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
    public List<PatientDto> searchPatient(String cpr) {
        IGenericClient client = getFhirClient(authService.getToken());

        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.IDENTIFIER.exactly().systemAndCode("urn:oid:1.2.208.176.1.2", cpr))
                .usingStyle(SearchStyleEnum.POST)
                .returnBundle(Bundle.class)
                .execute();

        return response.getEntry().stream()
                .map(bundleEntryComponent -> (Patient)bundleEntryComponent.getResource())
                .map(PatientMapper::mapPatient)
                .collect(Collectors.toList());
    }

    private <T extends Resource> List<T> lookupByCriteria(Class<T> resourceClass, List<ICriterion> criteria) {
        IGenericClient client = getFhirClient(authService.getToken());
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
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token.accessToken());
        client.registerInterceptor(logInt);
        client.registerInterceptor(authInterceptor);
        return client;
    }
}
