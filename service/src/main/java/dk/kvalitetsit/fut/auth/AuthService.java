package dk.kvalitetsit.fut.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.model.CareTeamDto;
import org.openapitools.model.ContextDto;
import org.openapitools.model.UserInfoDto;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Map;

public class AuthService {
    public record Token(String accessToken, String refreshToken){};
    private final String USERNAME;
    private final String PASSWORD;
    private final String authTokenUrl;
    private final String authUserinfoUrl;
    private final String authContextUrl;

    public AuthService(String username, String password,
                       String authServerUrl, String authUserinfoUrl, String authContextUrl) {
        this.USERNAME = username;
        this.PASSWORD = password;
        this.authTokenUrl = authServerUrl;
        this.authUserinfoUrl = authUserinfoUrl;
        this.authContextUrl = authContextUrl;
    }

    private Token refreshToken(String refreshToken, String careTeamId, String episodeOfCareId, String patientId) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("refresh_token", refreshToken);
        map.add("client_id", "oio_mock");
        if (careTeamId != null) {
            map.add("care_team_id", careTeamId);
        }
        if (episodeOfCareId != null) {
            map.add("episode_of_care_id", episodeOfCareId);
        }

        if (patientId != null) {
            map.add("patient_id", patientId);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(authTokenUrl, request, String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<Object, String> map2 = mapper.readValue(response.getBody(), Map.class);

        return new Token(map2.get("access_token"), map2.get("refresh_token"));
    }

    private Token createToken(String careTeamId, String patientId) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", USERNAME);
        map.add("password", PASSWORD);
        map.add("client_id", "oio_mock");

        if (careTeamId != null) {
            map.add("care_team_id", careTeamId);
        }

        if (patientId != null) {
            map.add("patient_id", patientId);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(authTokenUrl, request, String.class);
        ObjectMapper mapper = new ObjectMapper();
        Map<Object, String> map2 = mapper.readValue(response.getBody(), Map.class);

        return new Token(map2.get("access_token"), map2.get("refresh_token"));
    }

    public Token getToken() {
        try {
            return this.createToken(null, null);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Token getTokenWithCareTeamContext(String careTeamId) throws JsonProcessingException {
        return createToken(careTeamId, null);
    }

    public Token getTokenWithPatientContext(String patientId) throws JsonProcessingException {
        return this.createToken(null, patientId);
    }

    public Token refreshTokenWithCareTeamContext(Token token, String careTeamId) throws JsonProcessingException {
        return refreshToken(token.refreshToken(), careTeamId, null, null);
    }

    public Token refreshTokenWithCareTeamAndEpisodeOfCareContext(Token token, String careTeamId, String episodeOfCareId) throws JsonProcessingException {
        return refreshToken(token.refreshToken(), careTeamId, episodeOfCareId, null);
    }

    public Token refreshTokenWithCareTeamAndPatientContext(Token token, String careTeamId, String patientId) throws JsonProcessingException {
        return refreshToken(token.refreshToken(), careTeamId, null, patientId);
    }

    public Token refreshTokenWithEpisodeOfCareContext(Token token, String episodeOfCareId) throws JsonProcessingException {
        return refreshToken(token.refreshToken(), null, episodeOfCareId, null);
    }

    public UserInfoDto getUserInfo(Token token) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(token.accessToken());

        ResponseEntity<String> response = restTemplate.exchange(
                authUserinfoUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        ObjectMapper mapper = new ObjectMapper();
        Map<Object, String> map = mapper.readValue(response.getBody(), Map.class);
        UserInfoDto dto = new UserInfoDto();
        dto.setName(map.get("name"));
        dto.setUuid(map.get("user_id"));

        // Her havde det været pænere lige at kalde på Participant-resursen og sikre sig,
        // at man får det faktiske id, på den participant, der er logget ind.
        int id = Integer.parseInt(dto.getUuid().substring(dto.getUuid().lastIndexOf("/") + 1));
        dto.setUserId(id);
        dto.setCpr(map.get("cpr"));
        dto.setUserType(map.get("user_type"));
        dto.setPreferredUsername("preferred_username");

        return dto;
    }

    public ContextDto getContext(Token token) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(token.accessToken());

        ResponseEntity<String> response = restTemplate.exchange(
                authContextUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        ObjectMapper mapper = new ObjectMapper();
        Map<Object, ArrayList> map = mapper.readValue(response.getBody(), Map.class);
        ArrayList<Map<String, String>> careTeams = map.get("care_teams");

        ContextDto dto = new ContextDto();
        dto.setCareTeams(careTeams.stream().map((careteam -> {
                CareTeamDto d = new CareTeamDto();
                d.setId(careteam.get("id"));
                return d;
        })).toList());

        return dto;
    }

}
