package dk.kvalitetsit.fut.patient;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.openapitools.model.CreatePatientDto;
import org.openapitools.model.PatientDto;

import java.util.List;

public interface PatientService {
    PatientDto getPatient(String careTeamId, String patientId) throws JsonProcessingException;
    PatientDto getPatient(String patientId) throws JsonProcessingException;
    List<PatientDto> getPatients(String given, String family);
    void removePatient(String uuid);
    PatientDto createPatient(CreatePatientDto patient);
}
