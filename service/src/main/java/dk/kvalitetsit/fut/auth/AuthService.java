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
    private final String authTokenUrl;
    private final String authUserinfoUrl;
    private final String authContextUrl;

    public AuthService(String authServerUrl, String authUserinfoUrl, String authContextUrl) {
        this.authTokenUrl = authServerUrl;
        this.authUserinfoUrl = authUserinfoUrl;
        this.authContextUrl = authContextUrl;
    }

    public String getToken() throws JsonProcessingException {
        return this.getToken("Gr6_medarbejder9", "Test1266");
    }

    public String getToken(String username, String password) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", username);
        map.add("password", password);
        map.add("client_id", "oio_mock");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(authTokenUrl, request, String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<Object, String> map2 = mapper.readValue(response.getBody(), Map.class);

        return map2.get("access_token");
    }

    public UserInfoDto getUserInfo(String accessToken) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(accessToken);

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
        dto.setUserId(map.get("user_id"));
        dto.setCpr(map.get("cpr"));
        dto.setUserType(map.get("user_type"));
        dto.setPreferredUsername("preferred_username");

        return dto;
    }

    public ContextDto getContext(String accessToken) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(accessToken);

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
                d.setUuid(careteam.get("id"));
                return d;
        })).toList());

        return dto;
    }

}
