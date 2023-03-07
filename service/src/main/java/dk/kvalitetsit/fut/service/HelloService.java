package dk.kvalitetsit.fut.service;

import dk.kvalitetsit.fut.service.model.HelloServiceOutput;
import dk.kvalitetsit.fut.service.model.HelloServiceInput;

public interface HelloService {
    HelloServiceOutput helloServiceBusinessLogic(HelloServiceInput input);
}
