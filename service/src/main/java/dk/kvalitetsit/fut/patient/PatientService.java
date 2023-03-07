package dk.kvalitetsit.fut.patient;

import org.openapitools.model.CreatePatientDto;
import org.openapitools.model.PatientDto;

import java.util.List;

public interface PatientService {
    PatientDto getPatient(String uuid) throws Exception;

    List<PatientDto> getPatients();

    void removePatient(String uuid) throws Exception;

    PatientDto createPatient(CreatePatientDto patient);
}
