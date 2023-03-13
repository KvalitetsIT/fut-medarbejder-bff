package dk.kvalitetsit.fut.careplan;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import dk.kvalitetsit.fut.auth.AuthService;
import org.hl7.fhir.r4.model.*;
import org.openapitools.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CarePlanServiceImpl implements CarePlanService {
    private static final Logger logger = LoggerFactory.getLogger(CarePlanServiceImpl.class);

    private final Map<String, PatientDto> patients = new HashMap<>();
    private FhirContext fhirContext;
    private String careplanServiceUrl;
    private AuthService authService;

    public CarePlanServiceImpl(FhirContext fhirContext, String careplanServiceUrl, AuthService authService) {
        this.fhirContext = fhirContext;
        this.careplanServiceUrl = careplanServiceUrl;
        this.authService = authService;
    }



    @Override
    public List<EpisodeofcareDto> getEpisodeOfCares(String careTeamId) {
        var teamCriteria = new ReferenceClientParam("team").hasId("https://organization.devenvcgi.ehealth.sundhed.dk/fhir/CareTeam/" + careTeamId);

        List<EpisodeOfCare> result = lookupByCriteria(EpisodeOfCare.class, List.of(teamCriteria));

        return result.stream()
                .map(episodeOfCare -> CarePlanMapper.mapEpisodeOfCare(episodeOfCare))
                .collect(Collectors.toList());
    }

    @Override
    public String createEpisodeOfCare(String careTeamId, String patientId, CreateEpisodeOfCareDto.ProvenanceEnum provenancePolicy, List<String> conditionCodes) {

        String patientUrl = "https://patient.devenvcgi.ehealth.sundhed.dk/fhir/Patient/"+patientId;
        String organizationUrl = "https://organization.devenvcgi.ehealth.sundhed.dk/fhir/Organization/48062";
        String careTeamUrl = "https://organization.devenvcgi.ehealth.sundhed.dk/fhir/CareTeam/"+careTeamId;

        String provenanceUrnUuid = IdType.newRandomUuid().getValue();
        String episodeOfCareUrnUuid = IdType.newRandomUuid().getValue();
        String conditionUrnUuid = IdType.newRandomUuid().getValue();

        Parameters parameters = new Parameters();
        Parameters.ParametersParameterComponent ppc = parameters.addParameter();
        ppc.setName("episodeOfCareAndProvenances");


        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);

        // create provenance
        Provenance provenance = new Provenance(InstantType.now());
        provenance.getMeta().addProfile("http://ehealth.sundhed.dk/fhir/StructureDefinition/ehealth-provenance");
        provenance.addTarget(new Reference(episodeOfCareUrnUuid))
                .addPolicy(provenancePolicy.getValue())
                .addAgent()
                .setWho(new Reference(patientUrl));

        // add to bundle
        Bundle.BundleEntryComponent provenanceEntry = bundle.addEntry();
        provenanceEntry.setFullUrl(provenanceUrnUuid)
                .setResource(provenance)
                .getRequest().setMethod(Bundle.HTTPVerb.POST).setUrl("Provenance");

        // create episode of care
        EpisodeOfCare episodeOfCare = new EpisodeOfCare();
        episodeOfCare.getMeta().addProfile("http://ehealth.sundhed.dk/fhir/StructureDefinition/ehealth-episodeofcare");
        episodeOfCare.setStatus(EpisodeOfCare.EpisodeOfCareStatus.PLANNED);
        episodeOfCare.setPatient(new Reference(patientUrl));
        episodeOfCare.setManagingOrganization(new Reference(organizationUrl));
        episodeOfCare.getPeriod().setStart(new Date());
        episodeOfCare.addDiagnosis()
                .setCondition(new Reference(conditionUrnUuid));


        episodeOfCare.addExtension()
                .setUrl("http://ehealth.sundhed.dk/fhir/StructureDefinition/ehealth-episodeofcare-caremanagerOrganization")
                .setValue(new Reference(organizationUrl));

        episodeOfCare.addTeam(new Reference(careTeamUrl));

        // add to bundle
        bundle.addEntry()
                .setFullUrl(episodeOfCareUrnUuid)
                .setResource(episodeOfCare)
                .getRequest().setMethod(Bundle.HTTPVerb.POST).setUrl("EpisodeOfCare");

        // create condition
        Condition condition = new Condition();
        condition.getMeta().addProfile("http://ehealth.sundhed.dk/fhir/StructureDefinition/ehealth-condition");
        condition.getCode().addCoding().setSystem("urn:oid:1.2.208.176.2.4").setCode("DE10");
        condition.setSubject(new Reference(patientUrl));
        condition.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/workflow-episodeOfCare")
                .setValue(new Reference(episodeOfCareUrnUuid));

        // add to bundle
        bundle.addEntry()
                .setFullUrl(conditionUrnUuid)
                .setResource(condition)
                .getRequest().setMethod(Bundle.HTTPVerb.POST).setUrl("Condition");

        ppc.setResource(bundle);

        //System.out.println(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(parameters));

        IGenericClient client = getFhirClientWithPatientContext(patientUrl);
        Bundle result = client.operation()
                //.onType(CarePlan.class)
                .onServer()
                .named("$create-episode-of-care")
                .withParameters(parameters)
                .returnResourceType(Bundle.class)
                .execute();


        boolean success = result.getEntry().stream().allMatch(bundleEntryComponent -> bundleEntryComponent.getResponse().getStatus().matches("201 Created"));
        if (success) {
            Bundle.BundleEntryComponent eoc = result.getEntry().stream()
                    .filter(bundleEntryComponent -> bundleEntryComponent.getResponse().getLocation().contains("EpisodeOfCare"))
                    .findFirst().get();

            // location will look like 'UriType[https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/119833/_history/1]'
            String search = "EpisodeOfCare/";
            int start = eoc.getResponse().getLocation().indexOf(search) + search.length();
            int end = eoc.getResponse().getLocation().indexOf("/", start);
            String episodeOfCareId = eoc.getResponse().getLocation().substring(start, end);

            return episodeOfCareId;
        }
        return null;
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

            ContextDto context = authService.getContext(token.accessToken());
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

            ContextDto context = authService.getContext(token.accessToken());
            String careTeamId = context.getCareTeams().get(0).getUuid();

            token = authService.refreshTokenWithCareTeamAndEpisodeOfCareContext(token, careTeamId, episodeOfCareUrl);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return this.getFhirClient(token);
    }
    private IGenericClient getFhirClient(AuthService.Token token) {
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token.accessToken());

        IGenericClient client = fhirContext.newRestfulGenericClient(careplanServiceUrl);
        client.registerInterceptor(authInterceptor);

        return client;
    }

    public EpisodeofcareDto getEpisodeOfCare(String id) {
        String episodeOfCareUrl = "https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/"+id;

        IGenericClient client = getFhirClientWithEpisodeOfCareContext(episodeOfCareUrl);
        EpisodeOfCare result = client
                .read()
                .resource(EpisodeOfCare.class)
                .withId(id)
                .execute();

        return CarePlanMapper.mapEpisodeOfCare(result);
    }

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
                .map(consent -> CarePlanMapper.mapConsent(consent))
                .collect(Collectors.toList());
    }

    public String createConsent(String episodeOfCareId, CreateConsentDto.CategoryEnum category, CreateConsentDto.StatusEnum status) {
        EpisodeofcareDto episodeOfCareDto = this.getEpisodeOfCare(episodeOfCareId);

        String patientUrl = "https://patient.devenvcgi.ehealth.sundhed.dk/fhir/Patient/"+episodeOfCareDto.getPatientId();
        String episodeOfCareUrl = "https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/"+episodeOfCareId;

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

    public ConsentDto getConsents(String episodeOfCareId, String consentId) {
        ConsentDto result = null;

        String episodeOfCareUrl = "https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/"+episodeOfCareId;

        IGenericClient client = getFhirClientWithEpisodeOfCareContext(episodeOfCareUrl);
        try {
            Consent consent = client
                    .read()
                    .resource(Consent.class)
                    .withId(consentId)
                    .execute();

            result = CarePlanMapper.mapConsent(consent);
        }
        catch (ResourceNotFoundException rnfe) {
            String msg = String.format("No Consent found with id: %s", consentId);
            logger.error(msg);
            throw new dk.kvalitetsit.fut.controller.exception.ResourceNotFoundException(msg);
        }

        return result;
    }
}
