package dk.kvalitetsit.fut.patient;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.openapitools.model.CreatePatientDto;
import org.openapitools.model.PatientDto;

import java.util.List;

public interface PatientService {
    List<PatientDto> searchPatients(String given, String family);
    String createPatient(String cpr);
    PatientDto getPatient(String patientId, String careTeamId) throws JsonProcessingException;
}
