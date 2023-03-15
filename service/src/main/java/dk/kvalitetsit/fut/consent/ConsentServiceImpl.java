package dk.kvalitetsit.fut.consent;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import dk.kvalitetsit.fut.auth.AuthService;
import org.hl7.fhir.r4.model.*;
import org.openapitools.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ConsentServiceImpl implements ConsentService {
    private static final Logger logger = LoggerFactory.getLogger(ConsentServiceImpl.class);

    private final Map<String, PatientDto> patients = new HashMap<>();
    private FhirContext fhirContext;
    private String fhirServiceEndpoint;
    private AuthService authService;

    public ConsentServiceImpl(FhirContext fhirContext, String fhirServiceEndpoint, AuthService authService) {
        this.fhirContext = fhirContext;
        this.fhirServiceEndpoint = fhirServiceEndpoint;
        this.authService = authService;
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

    @Override
    public String createConsent(String episodeOfCareId, String patientId, CreateConsentDto.CategoryEnum category, CreateConsentDto.StatusEnum status) {
        String episodeOfCareUrl = "https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/"+episodeOfCareId;
        String patientUrl = "https://patient.devenvcgi.ehealth.sundhed.dk/fhir/Patient/"+patientId;

        Consent consent = new Consent();
        consent.getMeta().addProfile("http://ehealth.sundhed.dk/fhir/StructureDefinition/ehealth-consent");
        consent.setStatus(Consent.ConsentState.ACTIVE); // TODO: should reflect incoming status
        consent.getScope().addCoding()
                .setSystem("http://terminology.hl7.org/CodeSystem/consentscope")
                .setCode("treatment")
                .setDisplay("Treatment");
        consent.addCategory()
                .addCoding()
                .setSystem("http://ehealth.sundhed.dk/cs/consent-category")
                .setCode("PITEOC"); // TODO: should reflect incomint category
        consent.setPatient(new Reference(patientUrl));
        consent.setPerformer(List.of(new Reference(patientUrl)));

        // the following two elements (policy rule and provision) are also required but that is not obvious from the documentation
        consent.getPolicyRule().addCoding().setSystem("http://terminology.hl7.org/CodeSystem/consentpolicycodes").setCode("cric");
        consent.getProvision().getPeriod().setStart(new Date());
        consent.getProvision().addActor()
                .setReference(new Reference(patientUrl))
                .getRole()
                .addCoding()
                .setSystem("http://terminology.hl7.org/CodeSystem/extra-security-role-type")
                .setCode("authserver");
        consent.getProvision().getDataFirstRep()
                .setMeaning(Consent.ConsentDataMeaning.RELATED)
                .setReference(new Reference(episodeOfCareUrl));

        // consent.create requires EpisodeOfCare and Patient in context. (EpisodeOfCare context adds implicit patient context)
        IGenericClient client = getFhirClientWithEpisodeOfCareContext(episodeOfCareUrl);
        MethodOutcome outcome = client.create()
                .resource(consent)
                .prettyPrint()
                .encodedJson()
                .execute();


        if (outcome.getCreated()) {
            return outcome.getId().toUnqualifiedVersionless().getIdPart();
        }
        return null;
    }

    @Override
    public ConsentDto getConsent(String episodeOfCareId, String consentId) {
        ConsentDto result = null;

        String episodeOfCareUrl = "https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/"+episodeOfCareId;

        IGenericClient client = getFhirClientWithEpisodeOfCareContext(episodeOfCareUrl);
        try {
            Consent consent = client
                    .read()
                    .resource(Consent.class)
                    .withId(consentId)
                    .execute();

            result = ConsentMapper.mapConsent(consent);
        }
        catch (ResourceNotFoundException rnfe) {
            String msg = String.format("No Consent found with id: %s", consentId);
            logger.error(msg);
            throw new dk.kvalitetsit.fut.controller.exception.ResourceNotFoundException(msg);
        }

        return result;
    }

    private IGenericClient getFhirClient() {
        try {
            return this.getFhirClient(authService.getToken());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private IGenericClient getFhirClientWithPatientContext(String patientUrl) {
        AuthService.Token token = null;
        try {
            token = authService.getToken();

            ContextDto context = authService.getContext(token);
            String careTeamId = context.getCareTeams().get(0).getUuid();

            token = authService.refreshTokenWithCareTeamAndPatientContext(token, careTeamId, patientUrl);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return this.getFhirClient(token);
    }

    private IGenericClient getFhirClientWithEpisodeOfCareContext(String episodeOfCareUrl) {

        AuthService.Token token = null;
        try {
            token = authService.getToken();

            ContextDto context = authService.getContext(token);
            String careTeamId = context.getCareTeams().get(0).getUuid();

            token = authService.refreshTokenWithCareTeamAndEpisodeOfCareContext(token, careTeamId, episodeOfCareUrl);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return this.getFhirClient(token);
    }
    private IGenericClient getFhirClient(AuthService.Token token) {
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token.accessToken());

        IGenericClient client = fhirContext.newRestfulGenericClient(fhirServiceEndpoint);
        client.registerInterceptor(authInterceptor);

        return client;
    }



    @Override
    public List<ConsentDto> getConsents(String episodeOfCareId) {
        String episodeOfCareUrl = "https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/"+episodeOfCareId;

        IGenericClient client = getFhirClientWithEpisodeOfCareContext(episodeOfCareUrl);
        Bundle result = client
                .search()
                .forResource(Consent.class)
                .where(Consent.DATA.hasId(episodeOfCareUrl))
                .returnBundle(Bundle.class)
                .execute();

        if (result.getEntry().isEmpty()) {
            return null;
        }
        if (result.getEntry().size() > 1) {
            logger.info(String.format("Multiple consents found for EpisodeOfCare with id: %s", episodeOfCareId));
        }

        return result.getEntry().stream()
                .map(bundleEntryComponent -> (Consent)bundleEntryComponent.getResource())
                .map(consent -> ConsentMapper.mapConsent(consent))
                .collect(Collectors.toList());
    }


}
