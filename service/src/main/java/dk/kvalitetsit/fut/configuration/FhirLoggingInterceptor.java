package dk.kvalitetsit.fut.configuration;

import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.slf4j.Logger;

public class FhirLoggingInterceptor extends LoggingInterceptor {

    public FhirLoggingInterceptor(Logger logger) {
        super.setLogger(logger);
        super.setLogRequestBody(false);
        super.setLogRequestHeaders(false);
        super.setLogRequestSummary(true);
        super.setLogResponseBody(false);
        super.setLogResponseHeaders(false);
        super.setLogResponseSummary(true);
    }
}