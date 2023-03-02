![Build Status](https://github.com/KvalitetsIT/fut-medarbejder-bff/workflows/CICD/badge.svg)
# fut-medarbejder-bff
This is an example BFF for the FUT platform.

## Endpoints

### Service

The service is listening for connections on port 8080.

Spring boot actuator is listening for connections on port 8081. This is used as prometheus scrape endpoint and health monitoring. 

Prometheus scrape endpoint: `http://localhost:8081/actuator/prometheus`  
Health URL that can be used for readiness probe: `http://localhost:8081/actuator/health`

### Documentation

Documentation of the API is build as a separate Docker image. Documentation is build using Swagger. The documentation 
image is post-fixed with `-documentation`. The file `documentation/docker/compose/docker-compose.yml` contains a  setup 
that starts both the service and documentation image. The documentation can be accessed at `http://localhost/test` 
and the service can be called through the Swagger UI. 

In the docker-compose setup is also an example on how to set custom endpoints for the Swagger documentation service.

## Configuration

| Environment variable | Description | Required |
|----------------------|-------------|---------- |
| JDBC_URL | JDBC connection URL | Yes |
| JDBC_USER | JDBC user          | Yes |
| JDBC_PASS | JDBC password      | Yes |
| LOG_LEVEL | Log Level for applikation  log. Defaults to INFO. | No |
| LOG_LEVEL_FRAMEWORK | Log level for framework. Defaults to INFO. | No |
| CORRELATION_ID | HTTP header to take correlation id from. Used to correlate log messages. Defaults to "x-request-id". | No
