package dk.kvalitetsit.fut.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class AuthService {
    private final String authTokenUrl;
    private final String authUserinfoUrl;

    public AuthService(String authServerUrl, String authUserinfoUrl) {
        this.authTokenUrl = authServerUrl;
        this.authUserinfoUrl = authUserinfoUrl;
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
/*
    public UserInfoDto getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        UserInfoDto dto = new UserInfoDto();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(accessToken);


        //ResponseEntity<String> response = restTemplate.getForObject()

        return dto;
    }
    */

}
