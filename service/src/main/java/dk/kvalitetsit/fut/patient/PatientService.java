package dk.kvalitetsit.fut.patient;

import org.openapitools.model.PatientDto;

import java.util.List;

public interface PatientService {
    List<PatientDto> searchPatients(String given, String family);
    PatientDto getPatient(String patientId);
    String createPatient(String cpr);
}
