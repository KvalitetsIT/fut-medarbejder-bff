package dk.kvalitetsit.fut.task;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import dk.kvalitetsit.fut.auth.AuthService;
import dk.kvalitetsit.fut.configuration.FhirLoggingInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.openapitools.model.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
