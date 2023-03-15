package dk.kvalitetsit.fut.plandefinition;

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
import dk.kvalitetsit.fut.careplan.CarePlanMapper;
import dk.kvalitetsit.fut.careplan.CarePlanService;
import org.apache.logging.log4j.util.Strings;
import org.hl7.fhir.r4.model.*;
import org.openapitools.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class PlanDefinitionServiceImpl implements PlanDefinitionService {
    private static final Logger logger = LoggerFactory.getLogger(PlanDefinitionServiceImpl.class);

    private FhirContext fhirContext;
    private String fhirServiceEndpoint;
    private AuthService authService;

    public PlanDefinitionServiceImpl(FhirContext fhirContext, String fhirServiceEndpoint, AuthService authService) {
        this.fhirContext = fhirContext;
        this.fhirServiceEndpoint = fhirServiceEndpoint;
        this.authService = authService;
    }


    @Override
    public List<PlandefinitionDto> getPlanDefinitions(String title) {
        IGenericClient client = getFhirClient();

        Bundle result = client.search()
                .forResource(PlanDefinition.class)
                .where(PlanDefinition.STATUS.exactly().code(Enumerations.PublicationStatus.ACTIVE.toCode()))
                .and(PlanDefinition.TITLE.matches().value(title))
                .returnBundle(Bundle.class)
                .execute();

        return result.getEntry().stream()
                .map(bundleEntryComponent -> (PlanDefinition)bundleEntryComponent.getResource())
                .map(planDefinition -> PlanDefinitionMapper.mapPlanDefinition(planDefinition))
                .collect(Collectors.toList());
    }

    private IGenericClient getFhirClient() {
        IGenericClient client = null;
        AuthService.Token token = null;
        token = authService.getToken();
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token.accessToken());

        client = fhirContext.newRestfulGenericClient(fhirServiceEndpoint);
        client.registerInterceptor(authInterceptor);

        return client;
    }
}
