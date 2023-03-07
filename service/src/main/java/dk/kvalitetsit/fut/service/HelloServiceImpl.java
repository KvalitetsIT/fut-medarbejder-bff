package dk.kvalitetsit.fut.service;

import dk.kvalitetsit.fut.service.model.HelloServiceInput;
import dk.kvalitetsit.fut.service.model.HelloServiceOutput;

import java.time.ZonedDateTime;

public class HelloServiceImpl implements HelloService {
    @Override
    public HelloServiceOutput helloServiceBusinessLogic(HelloServiceInput input) {
        return new HelloServiceOutput(input.name(), ZonedDateTime.now());
    }
}
