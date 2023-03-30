package dk.kvalitetsit.fut.episodeofcare;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import org.hl7.fhir.r4.model.*;

import org.apache.logging.log4j.util.Strings;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.openapitools.model.*;
import dk.kvalitetsit.fut.auth.AuthService;

public class EpisodeOfCareServiceImpl implements EpisodeOfCareService {
    private static final Logger logger = LoggerFactory.getLogger(EpisodeOfCareServiceImpl.class);

    private FhirContext fhirContext;
    private String fhirServiceEndpoint;
    private AuthService authService;

    public EpisodeOfCareServiceImpl(FhirContext fhirContext, String fhirServiceEndpoint, AuthService authService) {
        this.fhirContext = fhirContext;
        this.fhirServiceEndpoint = fhirServiceEndpoint;
        this.authService = authService;
    }

    @Override
    public List<EpisodeofcareDto> getEpisodeOfCaresForCareTeam(String careTeamId, List<String> status) {
        var teamCriteria = new ReferenceClientParam("team").hasId("https://organization.devenvcgi.ehealth.sundhed.dk/fhir/CareTeam/" + careTeamId);

        List<EpisodeOfCare> result;
        if (status != null) {
            var statusCriteria = CarePlan.STATUS.exactly().codes(status);
            result = lookupByCriteria(EpisodeOfCare.class, List.of(teamCriteria, statusCriteria));
        }
        else {
            result = lookupByCriteria(EpisodeOfCare.class, List.of(teamCriteria));
        }

        return result.stream()
                .map(episodeOfCare -> EpisodeOfCareMapper.mapEpisodeOfCare(episodeOfCare, ""))
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

    @Override
    public EpisodeofcareDto getEpisodeOfCare(String episodeOfCareId) {
        String episodeOfCareUrl = "https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/"+ episodeOfCareId;

        IGenericClient client = getFhirClientWithEpisodeOfCareContext(episodeOfCareUrl);
        EpisodeOfCare result = client
                .read()
                .resource(EpisodeOfCare.class)
                .withId(episodeOfCareId)
                .execute();

        Reference r = result.getDiagnosis().get(0).getCondition();

        Condition condition = client
                .read()
                .resource(Condition.class)
                .withUrl(result.getDiagnosis().get(0).getCondition().getReference())
                .execute();
        String code = condition.getCode().getCoding().get(0).getCode();
        //String display = condition.getCode().getCoding().get(0).getDisplay();

        return EpisodeOfCareMapper.mapEpisodeOfCare(result, code);
    }

    @Override
    public void updateEpisodeOfCare(String episodeOfCareId, OffsetDateTime start, OffsetDateTime end, EpisodeOfCareStatusDto status, String careTeamId) {
        final String replaceOperation = "{\"op\": \"replace\", \"path\": \"%s\", \"value\": \"%s\"}";
        List<String> patchOperations = new ArrayList<>();
        if (start != null) {
            DateTimeType newStart = new DateTimeType(Date.from(start.toInstant()));
            patchOperations.add(String.format(replaceOperation, "/period/start", newStart.asStringValue()));
        }
        if (end != null) {
//            DateTimeType newEnd = new DateTimeType(Date.from(end.toInstant()));
//            patchOperations.add(String.format(replaceOperation, "/period/end", newEnd.asStringValue()));
        }
        if (status != null) {
            EpisodeOfCare.EpisodeOfCareStatus newStatus = EpisodeOfCareMapper.mapEpisodeOfCareStatus(status);
            patchOperations.add(String.format(replaceOperation, "/status", newStatus.toCode()));
        }
        if (careTeamId != null) {
            // todo?
        }
        String patchBody = "[" + Strings.join(patchOperations, ',') + "]";


        logger.info(String.format("Patching EpisodeOfCare id=%s with json patch body:\n%s", episodeOfCareId, patchBody));

        String episodeOfCareUrl = "https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/"+ episodeOfCareId;
        IGenericClient client = getFhirClientWithEpisodeOfCareContext(episodeOfCareUrl);

        MethodOutcome outcome = client.patch()
                .withBody(patchBody)
                .withId("EpisodeOfCare/"+ episodeOfCareId)
                .execute();

        // optional for server to return actual updated resource.
        //EpisodeOfCare resultingResource = (EpisodeOfCare) outcome.getResource();

        logger.info(String.format("Updated EpisodeOfCare with id: %s", outcome.getId().toUnqualifiedVersionless().getIdPart()));
    }

    @Override
    public void deleteEpisodeOfCare(String episodeOfCareId) {
        EpisodeofcareDto episodeOfCare = this.getEpisodeOfCare(episodeOfCareId);
        updateEpisodeOfCare(episodeOfCareId, episodeOfCare.getStart(), OffsetDateTime.now(), EpisodeOfCareStatusDto.ENTERED_IN_ERROR, null);
    }

    private <T extends Resource> List<T> lookupByCriteria(Class<T> resourceClass, List<ICriterion> criteria) {
        IGenericClient client = getFhirClient();

        IQuery<Bundle> query = client
                .search()
                .forResource(resourceClass)
                .count(200) // Default is 20
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
        return this.getFhirClient(authService.getToken());
    }
    private IGenericClient getFhirClientWithPatientContext(String patientUrl) {
        AuthService.Token token = null;
        try {
            token = authService.getToken();

            ContextDto context = authService.getContext(token);
            String careTeamId = context.getCareTeams().get(0).getId();

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
            String careTeamId = context.getCareTeams().get(0).getId();

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


}
