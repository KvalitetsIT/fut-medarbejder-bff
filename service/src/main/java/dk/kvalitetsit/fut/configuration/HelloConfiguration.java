package dk.kvalitetsit.fut.configuration;

import dk.kvalitetsit.fut.service.HelloService;
import dk.kvalitetsit.fut.service.HelloServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelloConfiguration{
    @Bean
    public HelloService helloService() {
        return new HelloServiceImpl();
    }
}
