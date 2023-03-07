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

    private String authServerUrl;

    public AuthService(String authServerUrl) {
        this.authServerUrl = authServerUrl;
    }

    public String getToken() throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", "Gr6_medarbejder9");
        map.add("password", "Test1266");
        map.add("client_id", "oio_mock");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(authServerUrl, request, String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<Object, String> map2 = mapper.readValue(response.getBody(), Map.class);

        return map2.get("access_token");


    }
}
