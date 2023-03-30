package dk.kvalitetsit.fut.task;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import dk.kvalitetsit.fut.auth.AuthService;
import dk.kvalitetsit.fut.configuration.FhirLoggingInterceptor;
import dk.kvalitetsit.fut.episodeofcare.EpisodeOfCareMapper;
import org.apache.logging.log4j.util.Strings;
import org.hl7.fhir.r4.model.*;
import org.openapitools.model.ContextDto;
import org.openapitools.model.TaskDto;
import org.openapitools.model.TaskStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TaskServiceImpl  {
    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
    private FhirContext fhirContext;
    private String fhirServiceEndpoint;
    private AuthService authService;

    public TaskServiceImpl(FhirContext fhirContext, String fhirServiceEndpoint, AuthService authService) {
        this.fhirContext = fhirContext;
        this.fhirServiceEndpoint = fhirServiceEndpoint;
        this.authService = authService;
    }

    //@Override
    public List<TaskDto> getTasks(String careTeamId, String status) {
        String careTeamUrl = "https://organization.devenvcgi.ehealth.sundhed.dk/fhir/CareTeam/"+careTeamId;

        AuthService.Token token = authService.getToken();
        //token = authService.refreshTokenWithCareTeamContext(token, careTeamId);

        IGenericClient client = getFhirClient(token);
        try {
            IQuery<Bundle> query = client.search().forResource(Task.class)
                    .where(new ReferenceClientParam("responsible").hasId(careTeamUrl))
                    .returnBundle(Bundle.class);

            if (status !=  null) {
                query.and(Task.STATUS.exactly().codes(status));
            }

            Bundle result = query.execute();

            return result.getEntry().stream()
                    .map(bundleEntryComponent -> (Task)bundleEntryComponent.getResource())
                    .map(task -> TaskMapper.mapTask(task))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return null;
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
        IGenericClient client = fhirContext.newRestfulGenericClient(fhirServiceEndpoint);
        FhirLoggingInterceptor logInt = new FhirLoggingInterceptor(logger);
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token.accessToken());
        client.registerInterceptor(logInt);
        client.registerInterceptor(authInterceptor);
        return client;
    }

    public TaskDto getTaskById(String careTeamId, String taskId) {
        String careTeamUrl = "https://organization.devenvcgi.ehealth.sundhed.dk/fhir/CareTeam/"+careTeamId;

        AuthService.Token token = authService.getToken();
        //token = authService.refreshTokenWithCareTeamContext(token, careTeamId);

        IGenericClient client = getFhirClient(token);
        try {
            Task task = client.read()
                    .resource(Task.class)
                    .withId("Task/" + taskId)
                    .execute();

            return TaskMapper.mapTask(task);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return null;
    }

    public void updateTask(String episodeOfCareId, String taskId, TaskStatusDto status) {
        final String replaceOperation = "{\"op\": \"replace\", \"path\": \"%s\", \"value\": \"%s\"}";

        List<String> patchOperations = new ArrayList<>();
        if (status != null) {
            Task.TaskStatus newStatus = TaskMapper.mapTaskStatus(status);
            patchOperations.add(String.format(replaceOperation, "/status", newStatus.toCode()));
        }
        String patchBody = "[" + Strings.join(patchOperations, ',') + "]";

        logger.debug(String.format("Patching EpisodeOfCare id=%s with json patch body:\n%s", episodeOfCareId, patchBody));

        String episodeOfCareUrl = "https://careplan.devenvcgi.ehealth.sundhed.dk/fhir/EpisodeOfCare/"+ episodeOfCareId;
        IGenericClient client = getFhirClientWithEpisodeOfCareContext(episodeOfCareUrl);

        MethodOutcome outcome = client.patch()
                .withBody(patchBody)
                .withId("Task/"+ taskId)
                .execute();

        logger.info(String.format("Updated Task with id: %s", outcome.getId().toUnqualifiedVersionless().getIdPart()));
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
}
