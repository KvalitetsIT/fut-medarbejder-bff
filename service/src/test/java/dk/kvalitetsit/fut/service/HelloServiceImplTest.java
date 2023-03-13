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

    //@Test
    public void testme() {
        System.out.println("hello");
        FhirContext context = FhirContext.forR4();

//        IGenericClient client = context.newRestfulGenericClient("https://patient.devenvcgi.ehealth.sundhed.dk/fhir/Patient");

        // Create a context and get the client factory so it can be configured
        FhirContext ctx = FhirContext.forR4();
//        IRestfulClientFactory clientFactory = ctx.getRestfulClientFactory();

// In reality the token would have come from an authorization server
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJXMmxWUFYzWGpzTFFfWDhFRHdUV1pQLXU4UWFIMzNuUU11ZVB3algwcDN3In0.eyJleHAiOjE2NzgxMDg5MjgsImlhdCI6MTY3ODEwODYyOCwianRpIjoiNGFlYzZjMjEtMDZjOS00MGRmLTg5ZWUtMjdjMTU4NGMxMzhmIiwiaXNzIjoiaHR0cHM6Ly9zYW1sLmRldmVudmNnaS5laGVhbHRoLnN1bmRoZWQuZGsvYXV0aC9yZWFsbXMvZWhlYWx0aCIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJjZDNiMjk5NC04ZjVmLTQyMmQtYTY2NS01ZjY3MTI3YTljYTYiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJvaW9fbW9jayIsInNlc3Npb25fc3RhdGUiOiIxNmU2N2RjYS1kZjZhLTQ4OWUtOGE3Yi1hODlhNWFkMDMzMDYiLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIlNTTFBhcnR5LnJlYWQiLCJPcmdhbml6YXRpb24ucmVhZCIsIkNvbW11bmljYXRpb24ucmVhZCIsIlRhc2suc2VhcmNoIiwiTWVkaWEuc2VhcmNoIiwiUXVlc3Rpb25uYWlyZVJlc3BvbnNlLnJlYWQiLCJQYXRpZW50LnJlYWQiLCJFcGlzb2RlT2ZDYXJlLmRlbGV0ZSIsIiRzZWFyY2gtbWVhc3VyZW1lbnRzIiwiQWN0aXZpdHlEZWZpbml0aW9uLnJlYWQiLCJBY3Rpb25HdWlkYW5jZS5yZWFkIiwiT3JnYW5pemF0aW9uLnNlYXJjaCIsIkNsaW5pY2FsIFZpZXdlciIsIkRldmljZU1ldHJpYy5yZWFkIiwiQ2l0aXplbiBFbnJvbGxlciIsInVtYV9hdXRob3JpemF0aW9uIiwiVGFzay5jcmVhdGUiLCJDb21tdW5pY2F0aW9uLnVwZGF0ZSIsIkNhcmVQbGFuJHVwZGF0ZS1jYXJlLXRlYW1zIiwiQ29uc2VudC5wYXRjaCIsIlRhc2sudXBkYXRlIiwiTGlicmFyeSRldmFsdWF0ZSIsIlByYWN0aXRpb25lci5zZWFyY2giLCJTU0xPcmRlckxpbmUuY3JlYXRlIiwiRGV2aWNlLnNlYXJjaCIsIlF1ZXN0aW9ubmFpcmUuc2VhcmNoIiwiQ2xpbmljYWxJbXByZXNzaW9uLndyaXRlIiwiTGlicmFyeS5yZWFkIiwiUmVsYXRlZFBlcnNvbi5jcmVhdGUiLCJTU0xCbGFja0xpc3Qud3JpdGUiLCJTU0xPcmRlckxpbmUud3JpdGUiLCJEb2N1bWVudFJlZmVyZW5jZS5zZWFyY2giLCJNZWRpYS5yZWFkIiwiUHJvY2VkdXJlUmVxdWVzdC5yZWFkIiwiRXBpc29kZU9mQ2FyZS5yZWFkIiwiUGxhbkRlZmluaXRpb24uc2VhcmNoIiwiJHN1Ym1pdC1tZWFzdXJlbWVudCIsIlByb2NlZHVyZVJlcXVlc3QuY3JlYXRlIiwiUmVsYXRlZFBlcnNvbi5kZWxldGUiLCJEZXZpY2VNZXRyaWMuc2VhcmNoIiwiQ2xpbmljYWxJbXByZXNzaW9uLnJlYWQiLCJSZXN0cmljdGlvbkNhdGVnb3J5JG5vbmUiLCJSZXN0cmljdGlvbkNhdGVnb3J5JG1lYXN1cmluZy1zdXBwb3J0IiwiRXBpc29kZU9mQ2FyZSRjcmVhdGUtZXBpc29kZS1vZi1jYXJlIiwiQ2FyZVBsYW4udXBkYXRlIiwiQ2xpbmljYWxJbXByZXNzaW9uLmRlbGV0ZSIsIkNvbnNlbnQucmVhZCIsIlNTTFBhY2thZ2UucmVhZCIsIkNvbW11bmljYXRpb24uZGVsZXRlIiwiQ29uc2VudC5jcmVhdGUiLCJUYXNrLmRlbGV0ZSIsIk9ic2VydmF0aW9uLnJlYWQiLCJSZXN0cmljdGlvbkNhdGVnb3J5JG1lYXN1cmVtZW50LW1vbml0b3JpbmciLCJBY3Rpb25HaXVkYW5jZS5yZWFkIiwiUHJvY2VkdXJlUmVxdWVzdC5zZWFyY2giLCJTU0xPcmRlci5yZWFkIiwiUGxhbkRlZmluaXRpb24kYXBwbHkiLCJTU0xPcmRlci53cml0ZSIsIlNTTEFubm90YXRpb24ucmVhZCIsIk1vbml0b3JpbmcgQWRqdXN0ZXIiLCJDYXJlUGxhbi5yZWFkIiwiUXVlc3Rpb25uYWlyZVJlc3BvbnNlLnNlYXJjaCIsIlNTTFByb2JsZW0ucmVhZCIsIkNhcmVUZWFtLnNlYXJjaCIsIkFwcG9pbnRtZW50LnJlYWQiLCJDYXJlUGxhbi5zZWFyY2giLCJQbGFuRGVmaW5pdGlvbi5yZWFkIiwiU1NMQmxhY2tMaXN0LnJlYWQiLCJFcGlzb2RlT2ZDYXJlLnVwZGF0ZSIsIlByb2NlZHVyZVJlcXVlc3QudXBkYXRlIiwiQ29uc2VudC51cGRhdGUiLCJQZXJzb24kbWF0Y2giLCJQYXRpZW50JGNyZWF0ZVBhdGllbnRGcm9tUGVyc29uIiwiU1NMV2hpdGVMaXN0LnJlYWQiLCJQcm9jZWR1cmVSZXF1ZXN0LnBhdGNoIiwiT3JkZXIgUGxhY2VyIiwiQ29tbXVuaWNhdGlvblJlcXVlc3QudXBkYXRlIiwiU1NMVHJhY2VMaW5lLnJlYWQiLCJSZWxhdGVkUGVyc29uLndyaXRlIiwiJFNTTEZpbmRPckNyZWF0ZVBhcnR5IiwiQ29tbXVuaWNhdGlvbi5wYXRjaCIsIkNhcmVQbGFuLnBhdGNoIiwiQ2xpbmljYWxJbXByZXNzaW9uLnVwZGF0ZSIsIkVwaXNvZGVPZkNhcmUud3JpdGUiLCJFcGlzb2RlT2ZDYXJlLnBhdGNoIiwiRG9jdW1lbnRSZWZlcmVuY2UucmVhZCIsIkNhcmVQbGFuLndyaXRlIiwiRGV2aWNlLnJlYWQiLCJDb25zZW50LnNlYXJjaCIsIk1vbml0b3JpbmcgQXNzaXN0b3IiLCJDb21tdW5pY2F0aW9uLmNyZWF0ZSIsIlRhc2sucmVhZCIsIkNvbW11bmljYXRpb24ud3JpdGUiLCJFcGlzb2RlT2ZDYXJlLmNyZWF0ZSIsIkNvbW11bmljYXRpb25SZXF1ZXN0LnJlYWQiLCJDYXJlUGxhbi5jcmVhdGUiLCJDb21tdW5pY2F0aW9uUmVxdWVzdC5jcmVhdGUiLCJDbGluaWNhbEltcHJlc3Npb24ucGF0Y2giLCJQcmFjdGl0aW9uZXIucmVhZCIsIkNhcmVQbGFuJHN1Z2dlc3QtY2FyZS10ZWFtcyIsIlByb2NlZHVyZVJlcXVlc3Qud3JpdGUiLCJQYXRpZW50LndyaXRlIiwiUXVlc3Rpb25uYWlyZS5yZWFkIiwiRGV2aWNlVXNlU3RhdGVtZW50LnJlYWQiLCJDbGluaWNhbEltcHJlc3Npb24uc2VhcmNoIiwiQWN0aXZpdHlEZWZpbml0aW9uLnNlYXJjaCIsIkVwaXNvZGVPZkNhcmUkaXMtY29udGV4dC1hbGxvd2VkIiwiU1NMQ2F0YWxvZ3VlSXRlbS5yZWFkIiwiQ2FyZVBsYW4uZGVsZXRlIiwiUGF0aWVudC5zZWFyY2giLCJDb25kaXRpb24uY3JlYXRlIiwiQ29uZGl0aW9uLnNlYXJjaCIsIm9mZmxpbmVfYWNjZXNzIiwiUmVsYXRlZFBlcnNvbi5wYXRjaCIsIkxpYnJhcnkuc2VhcmNoIiwiQXBwb2ludG1lbnQud3JpdGUiLCJSZWxhdGVkUGVyc29uLnVwZGF0ZSIsIlNTTFByb2JsZW0ud3JpdGUiLCJSZWxhdGVkUGVyc29uLnJlYWQiLCJDb25kaXRpb24ucmVhZCIsIlNTTE9yZGVyTGluZS5yZWFkIiwiRXBpc29kZU9mQ2FyZS5zZWFyY2giLCJWaWV3LnJlYWQiLCJDYXJlVGVhbS5yZWFkIiwiSW5jaWRlbnQgTWFuYWdlciIsIk9ic2VydmF0aW9uLnNlYXJjaCIsIkVwaXNvZGVPZkNhcmUkdXBkYXRlLWNhcmUtdGVhbXMiLCJkZWZhdWx0LXJvbGVzLWVoZWFsdGgiLCJTU0xDYXRhbG9ndWUucmVhZCIsIkNsaW5pY2FsSW1wcmVzc2lvbi5jcmVhdGUiLCJDYXJlUGxhbiR1cGRhdGUucmVzcG9uc2liaWxpdHkiLCJEZXZpY2VVc2VTdGF0ZW1lbnQuc2VhcmNoIiwiQ29tbXVuaWNhdGlvbi5zZWFyY2giLCJUYXNrLndyaXRlIiwiQ29tbXVuaWNhdGlvblJlcXVlc3Quc2VhcmNoIiwiU1NMT3JkZXIuY3JlYXRlIiwiUmVsYXRlZFBlcnNvbi5zZWFyY2giLCJQcm9jZWR1cmVSZXF1ZXN0LmRlbGV0ZSIsIlRhc2sucGF0Y2giXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJzaWQiOiIxNmU2N2RjYS1kZjZhLTQ4OWUtOGE3Yi1hODlhNWFkMDMzMDYiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInVzZXJfdHlwZSI6IlBSQUNUSVRJT05FUiIsInVzZXJfaWQiOiJodHRwczovL29yZ2FuaXphdGlvbi5kZXZlbnZjZ2kuZWhlYWx0aC5zdW5kaGVkLmRrL2ZoaXIvUHJhY3RpdGlvbmVyLzEzNzEzOSIsIm5hbWUiOiJHcjZfbWVkYXJiZWpkZXI5IEpvaG5zb24iLCJjb250ZXh0Ijp7Im9yZ2FuaXphdGlvbl9pZCI6Imh0dHBzOi8vb3JnYW5pemF0aW9uLmRldmVudmNnaS5laGVhbHRoLnN1bmRoZWQuZGsvZmhpci9Pcmdhbml6YXRpb24vNDgwNjIiLCJjYXJlX3RlYW1faWQiOiJodHRwczovL29yZ2FuaXphdGlvbi5kZXZlbnZjZ2kuZWhlYWx0aC5zdW5kaGVkLmRrL2ZoaXIvQ2FyZVRlYW0vMTM1ODg0In0sInByZWZlcnJlZF91c2VybmFtZSI6ImdyNl9tZWRhcmJlamRlcjkiLCJnaXZlbl9uYW1lIjoiR3I2X21lZGFyYmVqZGVyOSIsImZhbWlseV9uYW1lIjoiSm9obnNvbiJ9.fSl2hRi3kxfADMxKjAOyenXZRwRMNjfpAUwi6WQDKcL0LuY3JEwbeBSCAOwZq8_i-qyIUzMOWBbQk0CoLJ89eEFGMqQbxSdPFvi5m8ypjhWLQ0z0F9RN-JgIO2rU3tSsKv-qkxT5D46hC3pTobdC4dNAUFUlj3dm2_VpaoQaWSn7x9j34rJBFT-796PTL99VILHmg9GdAWDNQ4XTO4PuMOHV_tOheqqFBqwcYo2j_P7yZ3a0n9rOMAadhu9bE-BG12u0l8OPyNxd0ER2-RHUV4Lp9mh2VJ1TAO5Nr3c49uDCDQXwR5L5B8E1bFt1ZSr9KddanozO8H9qo9rnunYcqQ";

        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);

// Register the interceptor with your client (either style)
        IGenericClient client = ctx.newRestfulGenericClient("https://patient.devenvcgi.ehealth.sundhed.dk/fhir");
        client.registerInterceptor(authInterceptor);

        Bundle result = client
                .search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value("Jørgensen"))
                .returnBundle(Bundle.class)
                .execute();
        result.getEntry().forEach(b -> System.out.println(b.getFullUrl()));
    }

    @Test
    public void testme2() {
        FhirContext context = FhirContext.forR4();
        FhirContext ctx = FhirContext.forR4();

        String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJXMmxWUFYzWGpzTFFfWDhFRHdUV1pQLXU4UWFIMzNuUU11ZVB3algwcDN3In0.eyJleHAiOjE2Nzg0NDM1MjUsImlhdCI6MTY3ODQ0MzIyNSwianRpIjoiZWEzYTA4MWQtMjRlMS00ODM3LTlhMzYtYTQyMWNjZGUwMDkzIiwiaXNzIjoiaHR0cHM6Ly9zYW1sLmRldmVudmNnaS5laGVhbHRoLnN1bmRoZWQuZGsvYXV0aC9yZWFsbXMvZWhlYWx0aCIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJlMzBjMmNhNy00OGQwLTRiOWYtOTY5NS1mNDNkMzUyOWE4NmIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJvaW9fbW9jayIsInNlc3Npb25fc3RhdGUiOiJkYjI2NDA4Ni02MTcyLTRiMTgtYTYzMS1jN2Q0OGM0OGZkMDMiLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIk9yZ2FuaXphdGlvbi5yZWFkIiwiQ29tbXVuaWNhdGlvbi5yZWFkIiwiVGFzay5zZWFyY2giLCJNZWRpYS5zZWFyY2giLCJSZXBvcnQgVXNlciIsIlBhdGllbnQucmVhZCIsIkVwaXNvZGVPZkNhcmUuZGVsZXRlIiwiQWN0aXZpdHlEZWZpbml0aW9uLnJlYWQiLCJBY3Rpb25HdWlkYW5jZS5yZWFkIiwiJGZldGNoLWNhcmVwbGFuLWN1c3RvbWl6YXRpb24tc3RhdHMiLCJEb2N1bWVudFJlZmVyZW5jZS51cGRhdGUiLCJ1bWFfYXV0aG9yaXphdGlvbiIsIlRhc2suY3JlYXRlIiwiJGZldGNoLWNhcmV0ZWFtLXN0YXRzIiwiQWN0aXZpdHlEZWZpbml0aW9uLnBhdGNoIiwiRGV2aWNlTWV0cmljLmNyZWF0ZSIsIlF1ZXN0aW9ubmFpcmUuc2VhcmNoIiwiQ2xpbmljYWxJbXByZXNzaW9uLndyaXRlIiwiTGlicmFyeS5yZWFkIiwiRGV2aWNlTWV0cmljLnBhdGNoIiwiU1NMQmxhY2tMaXN0LndyaXRlIiwiQ2xpbmljYWwgU3VwcG9ydGVyIiwiJGZldGNoLXByYWN0aXRpb25lci1zdGF0cyIsIlNTTE9yZGVyTGluZS53cml0ZSIsIk1lZGlhLnJlYWQiLCJQcm9jZWR1cmVSZXF1ZXN0LnJlYWQiLCJFcGlzb2RlT2ZDYXJlLnJlYWQiLCIkc3VibWl0LW1lYXN1cmVtZW50IiwiRGV2aWNlTWV0cmljLnNlYXJjaCIsIlJlc3RyaWN0aW9uQ2F0ZWdvcnkkbm9uZSIsIkFjdGl2aXR5RGVmaW5pdGlvbi5kZWxldGUiLCJQbGFuRGVmaW5pdGlvbi5wYXRjaCIsIkFjdGlvbkdpdWRhbmNlLndyaXRlIiwiQ2FyZVBsYW4udXBkYXRlIiwiRGV2aWNlLnVwZGF0ZSIsIiRmZXRjaC1tZWFzdXJlbWVudC1zdGF0cyIsIkNsaW5pY2FsSW1wcmVzc2lvbi5kZWxldGUiLCJRdWVzdGlvbm5haXJlLmNyZWF0ZSIsIlRhc2suZGVsZXRlIiwiUGxhbkRlZmluaXRpb24udXBkYXRlIiwiUmVzdHJpY3Rpb25DYXRlZ29yeSRtZWFzdXJlbWVudC1tb25pdG9yaW5nIiwiQWN0aW9uR2l1ZGFuY2UucmVhZCIsIkNhcmUgVGVhbSBBZG1pbmlzdHJhdG9yIiwiQ2xpbmljYWwgQWRtaW5pc3RyYXRvciIsIlNTTE9yZGVyLndyaXRlIiwiQ2FyZVBsYW4ucmVhZCIsIlNTTFByb2JsZW0ucmVhZCIsIlNTTEJsYWNrTGlzdC5yZWFkIiwiUHJvY2VkdXJlUmVxdWVzdC51cGRhdGUiLCIkZmV0Y2gtcGF0aWVudC10b3RhbCIsIlBhdGllbnQkY3JlYXRlUGF0aWVudEZyb21QZXJzb24iLCJPcmRlciBQbGFjZXIiLCJDb21tdW5pY2F0aW9uUmVxdWVzdC51cGRhdGUiLCJSZWxhdGVkUGVyc29uLndyaXRlIiwiQ2FyZVRlYW0udXBkYXRlIiwiJFNTTEZpbmRPckNyZWF0ZVBhcnR5IiwiQ29tbXVuaWNhdGlvbi5wYXRjaCIsIkNhcmVQbGFuLnBhdGNoIiwiQ2xpbmljYWxJbXByZXNzaW9uLnVwZGF0ZSIsIkRldmljZVVzZVN0YXRlbWVudC5jcmVhdGUiLCJFcGlzb2RlT2ZDYXJlLndyaXRlIiwiRG9jdW1lbnRSZWZlcmVuY2UucGF0Y2giLCJFcGlzb2RlT2ZDYXJlLnBhdGNoIiwiRGV2aWNlLnJlYWQiLCJDYXJlUGxhbi53cml0ZSIsIkNvbnNlbnQuc2VhcmNoIiwiTW9uaXRvcmluZyBBc3Npc3RvciIsIkNvbW11bmljYXRpb24uY3JlYXRlIiwiQ29tbXVuaWNhdGlvbi53cml0ZSIsIkNvbW11bmljYXRpb25SZXF1ZXN0LmNyZWF0ZSIsIkNhcmVQbGFuJHN1Z2dlc3QtY2FyZS10ZWFtcyIsIkRldmljZVVzZVN0YXRlbWVudC5yZWFkIiwiQ29uZGl0aW9uLnNlYXJjaCIsIlJlbGF0ZWRQZXJzb24ucGF0Y2giLCJBcHBvaW50bWVudC53cml0ZSIsIkxpYnJhcnkuc2VhcmNoIiwiU1NMUHJvYmxlbS53cml0ZSIsIkRldmljZVVzZVN0YXRlbWVudC51cGRhdGUiLCJDb25kaXRpb24ucmVhZCIsIlNTTE9yZGVyTGluZS5yZWFkIiwiRXBpc29kZU9mQ2FyZS5zZWFyY2giLCJEb2N1bWVudFJlZmVyZW5jZSRmaW5kLWRvY3VtZW50LXJlZmVyZW5jZXMiLCJJbmNpZGVudCBNYW5hZ2VyIiwiT2JzZXJ2YXRpb24uc2VhcmNoIiwiZGVmYXVsdC1yb2xlcy1laGVhbHRoIiwiJGZldGNoLWVwaXNvZGVvZmNhcmUtc3RhdHMiLCJBY3Rpdml0eURlZmluaXRpb24uY3JlYXRlIiwiQ2xpbmljYWxJbXByZXNzaW9uLmNyZWF0ZSIsIiRmZXRjaC1zc2wtb3JkZXJzIiwiUmVsYXRlZFBlcnNvbi5zZWFyY2giLCJTU0xQYXJ0eS5yZWFkIiwiUXVlc3Rpb25uYWlyZVJlc3BvbnNlLnJlYWQiLCIkc2VhcmNoLW1lYXN1cmVtZW50cyIsIkRldmljZS5jcmVhdGUiLCJPcmdhbml6YXRpb24uc2VhcmNoIiwiQ2xpbmljYWwgVmlld2VyIiwiRGV2aWNlTWV0cmljLnJlYWQiLCJDaXRpemVuIEVucm9sbGVyIiwiJHRyYW5zZm9ybS10by1RRkREIiwiQ2FyZVRlYW0uZGVsZXRlIiwiQ29tbXVuaWNhdGlvbi51cGRhdGUiLCJQbGFuRGVmaW5pdGlvbi5kZWxldGUiLCJDYXJlUGxhbiR1cGRhdGUtY2FyZS10ZWFtcyIsIiRmZXRjaC1wYXRpZW50LWRldmljZXMiLCJTZXJ2aWNlIGFuZCBMb2dpc3RpY3MiLCJUYXNrLnVwZGF0ZSIsIkNvbnNlbnQucGF0Y2giLCJEb2N1bWVudFJlZmVyZW5jZS53cml0ZSIsIkxpYnJhcnkkZXZhbHVhdGUiLCJQcmFjdGl0aW9uZXIuc2VhcmNoIiwiU1NMT3JkZXJMaW5lLmNyZWF0ZSIsIkRldmljZS5zZWFyY2giLCJSZWxhdGVkUGVyc29uLmNyZWF0ZSIsIkRvY3VtZW50UmVmZXJlbmNlLnNlYXJjaCIsIkRldmljZVVzZVN0YXRlbWVudC5wYXRjaCIsIlBsYW5EZWZpbml0aW9uLnNlYXJjaCIsIlByb2NlZHVyZVJlcXVlc3QuY3JlYXRlIiwiUmVsYXRlZFBlcnNvbi5kZWxldGUiLCJDbGluaWNhbEltcHJlc3Npb24ucmVhZCIsIlJlc3RyaWN0aW9uQ2F0ZWdvcnkkbWVhc3VyaW5nLXN1cHBvcnQiLCJBY3Rpdml0eURlZmluaXRpb24ud3JpdGUiLCJEZXZpY2VNZXRyaWMudXBkYXRlIiwiQWN0aXZpdHlEZWZpbml0aW9uLnVwZGF0ZSIsIkVwaXNvZGVPZkNhcmUkY3JlYXRlLWVwaXNvZGUtb2YtY2FyZSIsIkNvbnNlbnQucmVhZCIsIlNTTFBhY2thZ2UucmVhZCIsIiRmZXRjaC1wcmFjdGl0aW9uZXItZ2RwciIsIkNvbW11bmljYXRpb24uZGVsZXRlIiwiQ29uc2VudC5jcmVhdGUiLCJPYnNlcnZhdGlvbi5yZWFkIiwiUHJvY2VkdXJlUmVxdWVzdC5zZWFyY2giLCJTU0xPcmRlci5yZWFkIiwiUGxhbkRlZmluaXRpb24kYXBwbHkiLCJTU0xBbm5vdGF0aW9uLnJlYWQiLCJNb25pdG9yaW5nIEFkanVzdGVyIiwiUXVlc3Rpb25uYWlyZVJlc3BvbnNlLnNlYXJjaCIsIkNhcmVUZWFtLnNlYXJjaCIsIkFwcG9pbnRtZW50LnJlYWQiLCJDYXJlUGxhbi5zZWFyY2giLCJQbGFuRGVmaW5pdGlvbi5yZWFkIiwiRXBpc29kZU9mQ2FyZS51cGRhdGUiLCJDb25zZW50LnVwZGF0ZSIsIlBlcnNvbiRtYXRjaCIsIlNTTFdoaXRlTGlzdC5yZWFkIiwiUHJvY2VkdXJlUmVxdWVzdC5wYXRjaCIsIlNTTFRyYWNlTGluZS5yZWFkIiwiUGxhbkRlZmluaXRpb24ud3JpdGUiLCJDYXJlVGVhbS53cml0ZSIsIiRmZXRjaC1jYXJlcGxhbi1kdXJhdGlvbi1zdGF0cyIsIlZpZXcud3JpdGUiLCJEb2N1bWVudFJlZmVyZW5jZS5kZWxldGUiLCJEb2N1bWVudFJlZmVyZW5jZS5yZWFkIiwiVGFzay5yZWFkIiwiRXBpc29kZU9mQ2FyZS5jcmVhdGUiLCJDb21tdW5pY2F0aW9uUmVxdWVzdC5yZWFkIiwiQ2FyZVBsYW4uY3JlYXRlIiwiQ2xpbmljYWxJbXByZXNzaW9uLnBhdGNoIiwiUHJhY3RpdGlvbmVyLnJlYWQiLCJDYXJlVGVhbS5jcmVhdGUiLCJQcm9jZWR1cmVSZXF1ZXN0LndyaXRlIiwiUGF0aWVudC53cml0ZSIsIkRldmljZS5wYXRjaCIsIlF1ZXN0aW9ubmFpcmUucmVhZCIsIiR0cmFuc2Zvcm0tZnJvbS1RRkREIiwiQ2xpbmljYWxJbXByZXNzaW9uLnNlYXJjaCIsIkJpbmFyeS5yZWFkIiwiQWN0aXZpdHlEZWZpbml0aW9uLnNlYXJjaCIsIkVwaXNvZGVPZkNhcmUkaXMtY29udGV4dC1hbGxvd2VkIiwiU1NMQ2F0YWxvZ3VlSXRlbS5yZWFkIiwiQ2FyZVBsYW4uZGVsZXRlIiwiUGF0aWVudC5zZWFyY2giLCJQbGFuRGVmaW5pdGlvbi5jcmVhdGUiLCJDb25kaXRpb24uY3JlYXRlIiwib2ZmbGluZV9hY2Nlc3MiLCJDYXJlVGVhbS5wYXRjaCIsIlF1ZXN0aW9ubmFpcmUucGF0Y2giLCJSZWxhdGVkUGVyc29uLnVwZGF0ZSIsIkluY2lkZW50IFJlcG9ydGVyIiwiUmVsYXRlZFBlcnNvbi5yZWFkIiwiUXVlc3Rpb25uYWlyZS51cGRhdGUiLCJWaWV3LnJlYWQiLCJDYXJlVGVhbS5yZWFkIiwiJGZldGNoLWNhcmVwbGFucy1ieS1wYXRpZW50IiwiRXBpc29kZU9mQ2FyZSR1cGRhdGUtY2FyZS10ZWFtcyIsIlF1ZXN0aW9ubmFpcmUgRWRpdG9yIiwiU1NMQ2F0YWxvZ3VlLnJlYWQiLCJDYXJlUGxhbiR1cGRhdGUucmVzcG9uc2liaWxpdHkiLCIkZmV0Y2gtcGF0aWVudC1nZHByIiwiRGV2aWNlVXNlU3RhdGVtZW50LnNlYXJjaCIsIkNvbW11bmljYXRpb24uc2VhcmNoIiwiRG9jdW1lbnRSZWZlcmVuY2UuY3JlYXRlIiwiQWN0aW9uR3VpZGFuY2Uud3JpdGUiLCJDb21tdW5pY2F0aW9uUmVxdWVzdC5zZWFyY2giLCJUYXNrLndyaXRlIiwiU1NMT3JkZXIuY3JlYXRlIiwiUHJvY2VkdXJlUmVxdWVzdC5kZWxldGUiLCJUYXNrLnBhdGNoIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwic2lkIjoiZGIyNjQwODYtNjE3Mi00YjE4LWE2MzEtYzdkNDhjNDhmZDAzIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJ1c2VyX3R5cGUiOiJQUkFDVElUSU9ORVIiLCJ1c2VyX2lkIjoiaHR0cHM6Ly9vcmdhbml6YXRpb24uZGV2ZW52Y2dpLmVoZWFsdGguc3VuZGhlZC5kay9maGlyL1ByYWN0aXRpb25lci8xMzcxMzYiLCJuYW1lIjoiR3I2X21lZGFyYmVqZGVyMSBKb2huc29uIiwiY29udGV4dCI6eyJvcmdhbml6YXRpb25faWQiOiJodHRwczovL29yZ2FuaXphdGlvbi5kZXZlbnZjZ2kuZWhlYWx0aC5zdW5kaGVkLmRrL2ZoaXIvT3JnYW5pemF0aW9uLzQ4MDYyIiwiY2FyZV90ZWFtX2lkIjoiaHR0cHM6Ly9vcmdhbml6YXRpb24uZGV2ZW52Y2dpLmVoZWFsdGguc3VuZGhlZC5kay9maGlyL0NhcmVUZWFtLzEzNTg4NCIsInBhdGllbnRfaWQiOiJodHRwczovL3BhdGllbnQuZGV2ZW52Y2dpLmVoZWFsdGguc3VuZGhlZC5kay9maGlyL1BhdGllbnQvMjU4OTgxIiwiZXBpc29kZV9vZl9jYXJlX2lkIjoiaHR0cHM6Ly9jYXJlcGxhbi5kZXZlbnZjZ2kuZWhlYWx0aC5zdW5kaGVkLmRrL2ZoaXIvRXBpc29kZU9mQ2FyZS8xMTk5NjUiLCJ0ZWFtX29uX2VvYyI6dHJ1ZX0sInByZWZlcnJlZF91c2VybmFtZSI6ImdyNl9tZWRhcmJlamRlcjEiLCJnaXZlbl9uYW1lIjoiR3I2X21lZGFyYmVqZGVyMSIsImZhbWlseV9uYW1lIjoiSm9obnNvbiJ9.kUevs0tAzH01plEG_sr5Y0Gnl3vmqAZJ3L0XyCnjT9wMlQ7cXyjPPtouEh4TDEmXYQw0eXPE6IGezT0QK7bzmOxEcN-Ilq-3k5dE3lQ13Q6iwBBz4x9votur1SyLiFJB9NwQhSJksNt5iL5BR_7wTbK3F4_3N-FuBDRyklVJmwYbmn9t1WJjDBCj892nrQCZ4rJFDTjLcpJAghzJJm9r3M1hqxjwSj9nJgYIttJ3CaTHJVam8e_nVJuOzZJHqFBQCkKf89DxZdzA8FtiXGf4WsxcEolyuM9FaAAJt3pryVrM0DmHqo6Qm8d7RcDi3SFLgzBcKFfBETxM8xZg3XmJcw";
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);

// Register the interceptor with your client (either style)
        IGenericClient client = ctx.newRestfulGenericClient("https://careplan.devenvcgi.ehealth.sundhed.dk/fhir");
        client.registerInterceptor(authInterceptor);

//        Bundle result = client
//                .search()
//                .forResource(EpisodeOfCare.class)
//                .where(new ReferenceClientParam("team").hasAnyOfIds("https://organization.devenvcgi.ehealth.sundhed.dk/fhir/CareTeam/135884"))
//                .returnBundle(Bundle.class)
//                .execute();

        EpisodeOfCare result = client
                .read()
                .resource(EpisodeOfCare.class)
                .withId("119965")
                .execute();

        System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(result));
        //result.getEntry().forEach(b -> System.out.println(b.getFullUrl()));
    }

    @Test
    public void mini() {
        String provenanceUrnUuid = UuidType.fromOid(UUID.randomUUID().toString()).getValue();
        System.out.println(new UuidType("test").toString());
        System.out.println(provenanceUrnUuid);
        System.out.println(IdType.newRandomUuid().getValue());
    }
    @Test
    public void skh() {
        String patientId = "patientId", organizationId = "organizationId", careTeamId = "careTeamId";

        String patientUrl = "https://patient.devenvcgi.ehealth.sundhed.dk/fhir/Patient/"+patientId;
        String organizationUrl = "https://organization.devenvcgi.ehealth.sundhed.dk/fhir/Organization/48062";
        String careTeamUrl = "https://organization.devenvcgi.ehealth.sundhed.dk/fhir/CareTeam/"+careTeamId;

        String provenanceUrnUuid = UriType.fromOid(UUID.randomUUID().toString()).getValue();
        String episodeOfCareUrnUuid = UriType.fromOid(UUID.randomUUID().toString()).getValue();
        String conditionUrnUuid = UriType.fromOid(UUID.randomUUID().toString()).getValue();

        Parameters parameters = new Parameters();
        Parameters.ParametersParameterComponent ppc = parameters.addParameter();
        ppc.setName("episodeOfCareAndProvenances");


        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);

        // create provenance
        Provenance provenance = new Provenance(InstantType.now());
        provenance.addTarget(new Reference(episodeOfCareUrnUuid))
                .addPolicy(CreateEpisodeOfCareDto.ProvenanceEnum.SUNDHEDSLOVEN.getValue())
                .addAgent()
                .setWho(new Reference(patientUrl));

        // add to bundle
        Bundle.BundleEntryComponent provenanceEntry = bundle.addEntry();
        provenanceEntry.setFullUrl(provenanceUrnUuid)
                .setResource(provenance)
                .getRequest().setMethod(Bundle.HTTPVerb.POST).setUrl("Provenance");

        // create episode of care
        EpisodeOfCare episodeOfCare = new EpisodeOfCare();
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
        condition.getCode().addCoding().setSystem("urn:oid:1.2.208.176.2.4").setCode("DE10");
        condition.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/workflow-episodeOfCare")
                .setValue(new Reference(episodeOfCareUrnUuid));

        // add to bundle
        bundle.addEntry()
                .setFullUrl(conditionUrnUuid)
                .setResource(condition)
                .getRequest().setMethod(Bundle.HTTPVerb.POST).setUrl("Condition");

        ppc.setResource(bundle);

        System.out.println(FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(parameters));
    }
}
