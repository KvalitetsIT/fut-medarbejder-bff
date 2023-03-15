package dk.kvalitetsit.fut.patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import dk.kvalitetsit.fut.auth.AuthService;
import org.hl7.fhir.r4.model.*;
import org.openapitools.model.PatientDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PatientServiceImpl implements PatientService {

    private FhirContext fhirContext;
    private String fhirServiceEndpoint;
    private AuthService authService;

    public PatientServiceImpl(FhirContext fhirContext, String fhirServiceEndpoint, AuthService authService) {
        this.fhirContext = fhirContext;
        this.fhirServiceEndpoint = fhirServiceEndpoint;
        this.authService = authService;
    }

    @Override
    public PatientDto getPatient(String patientId) {
        return null;
    }

    @Override
    public String createPatient(String cpr) {
        Parameters parameters = new Parameters();
        parameters.addParameter()
                .setName("crn")
                .setValue(new Identifier().setSystem("urn:oid:1.2.208.176.1.2").setValue(cpr));

        IGenericClient client = getFhirClient();
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

        IGenericClient client = fhirContext.newRestfulGenericClient(fhirServiceEndpoint);
        client.registerInterceptor(authInterceptor);
        return client;
    }
}
