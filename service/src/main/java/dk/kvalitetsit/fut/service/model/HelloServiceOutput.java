package dk.kvalitetsit.fut.service.model;

import java.time.ZonedDateTime;

public record HelloServiceOutput(String name, ZonedDateTime now) {
}
