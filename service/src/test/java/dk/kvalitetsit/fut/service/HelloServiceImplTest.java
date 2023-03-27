package dk.kvalitetsit.fut.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import dk.kvalitetsit.fut.service.model.HelloServiceInput;
import org.hl7.fhir.r4.model.*;
import org.junit.Before;
import org.junit.Test;
import org.openapitools.model.CreateEpisodeOfCareDto;
import org.openapitools.model.CreatePatientDto;
import org.openapitools.model.EpisodeofcareDto;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HelloServiceImplTest {
    private HelloService helloService;

    @Before
    public void setup() {
        helloService = new HelloServiceImpl();
    }

    @Test
    public void testValidInput() {
        var input = new HelloServiceInput(UUID.randomUUID().toString());

        var result = helloService.helloServiceBusinessLogic(input);
        assertNotNull(result);
        assertNotNull(result.now());
        assertEquals(input.name(), result.name());
    }
}
