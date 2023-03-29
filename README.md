![Build Status](https://github.com/KvalitetsIT/fut-patient-bff/workflows/CICD/badge.svg)
# FUT Administrative Demo - BFF

A small project demonstrating the use of the [eHealth Infrastructure](https://ehealth-dk.atlassian.net/wiki/spaces/EDTW/overview) for retrieving Episodes of Care, Patients, Tasks, creating consent, creating CarePlans, among other things. This is the backend for the [FUT Administrative Demo - Web](https://github.com/KvalitetsIT/fut-medarbejder-web) project.

## Requirements
- OpenJDK 17 or Docker

## Try it out
Options:
- Run main in `dk.kvalitetsit.fut.Application`.
- Run the documentation Dockerfile (see below).

## Endpoints

### Documentation

Documentation of the API is build as a separate Docker image. Documentation is build using Swagger. The documentation  image is post-fixed with `-documentation`. The file `documentation/docker/compose/docker-compose.yml` contains a setup that starts both the service and documentation image. The documentation can be accessed at `http://localhost/test` and the service can be called through the Swagger UI.

### Service

The service is listening for connections on port 8080. Spring boot actuator is listening for connections on port 8081. This is used as prometheus scrape endpoint and health monitoring.

Prometheus scrape endpoint: `http://localhost:8081/actuator/prometheus`  
Health URL that can be used for readiness probe: `http://localhost:8081/actuator/health`

## Known issues
Login and security is currently not implemented in this demo application. All authentication is handled automatically from mock employee credentials supplied as environment variables.

## Configuration

| Environment variable | Description                                                        | Required |
|----------------------|--------------------------------------------------------------------|----------|
| FUT_AUTH_USERNAME    | The mock employee username for the eHealth Infrastructure Platform | Yes      |
| FUT_AUTH_PASSWORD    | The mock employee password for the eHealth Infrastructure Platform | Yes      |
| E_HEALTH_ENV         | The name of the test environment                                   | Yes      |

