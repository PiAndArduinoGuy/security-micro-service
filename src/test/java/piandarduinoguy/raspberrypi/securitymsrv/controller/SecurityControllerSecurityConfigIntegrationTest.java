package piandarduinoguy.raspberrypi.securitymsrv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.Problem;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
class SecurityControllerSecurityConfigIntegrationTest {
    @Autowired
    private SecurityController securityController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${resources.base.location}")
    private String resourcesBaseLocation;

    @Value("${new-capture.annotated.file-name}")
    private String newCaptureAnnotatedFileName;

    @Autowired
    private TestUtils testUtils;

    private final SecurityConfig existingSecurityConfig = new SecurityConfig(SecurityStatus.BREACHED, SecurityState.DISARMED);

    @LocalServerPort
    private int port;


    @DisplayName("Given a security config json object that is valid " +
            "when put endpoint hit to update security config " +
            "then 201 CREATED response returned and security config updated as expected.")
    @Test
    void canSaveUpdatedSecurityConfig() throws Exception {
        SecurityConfig updatedSecurityConfig = new SecurityConfig(SecurityStatus.SAFE, SecurityState.ARMED);
        HttpEntity<SecurityConfig> httpEntity = new HttpEntity<>(updatedSecurityConfig);
        ResponseEntity<Void> responseEntity = restTemplate.exchange("http://localhost:" + port + "/security/update/security-config", HttpMethod.PUT, httpEntity, Void.class);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        testUtils.assertThatExpectedSecurityConfigJsonFileSaved(updatedSecurityConfig);

        testUtils.deleteSecurityConfigFile();
    }

    @DisplayName("Given a security config json object exists " +
            "when get endpoint hit to " +
            "then 200 OK response security config returned.")
    @Test
    void canGetSecurityConfig() throws Exception {
        testUtils.createSecurityConfigFile(existingSecurityConfig);

        ResponseEntity<SecurityConfig> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "/security/security-config", SecurityConfig.class);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        SecurityConfig returnedSecurityConfig = responseEntity.getBody();
        assertThat(returnedSecurityConfig).isNotNull();
        assertThat(returnedSecurityConfig.getSecurityStatus()).isEqualTo(existingSecurityConfig.getSecurityStatus());
        assertThat(returnedSecurityConfig.getSecurityState()).isEqualTo(existingSecurityConfig.getSecurityState());

        testUtils.deleteSecurityConfigFile();
    }

    @DisplayName("Given an annotated image exists " +
            "when get to the /annotated-image endpoint is made " +
            "then a base64 encoded image is returned with status 200 OK.")
    @Test
    void canGetAnnotatedImage() throws Exception {
        testUtils.createExpectedAnnotatedImageFile();

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "/security/annotated-image", String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualToIgnoringCase(testUtils.getExpectedBase64EncodedAnnotatedImage());

        testUtils.deleteAnnotatedImage();
    }

    @DisplayName("Given an annotated image does not exist " +
            "when get to the /annotated-image endpoint is made " +
            "then return expected Zalando problem.")
    @Test
    void canReturnZalandoProblemIfNoAnnotatedImageExists() {
        ResponseEntity<Problem> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "/security/annotated-image", Problem.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Problem problem = responseEntity.getBody();
        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getDetail()).isEqualToIgnoringCase("The File src/test/resources/application/test_new_capture_annotated.jpeg does not exist.");
        assertThat(problem.getTitle()).isEqualToIgnoringCase(HttpStatus.NOT_FOUND.getReasonPhrase());

    }

    @DisplayName("Given securityConfig is ARMED and BREACHED " +
            "when post to the /silence-alarm endpoint is made" +
            "then return OK response and securityConfig status is SAFE and state is DISARMED")
    @Test
    void canSilenceAlarm() throws Exception {
        SecurityConfig breachedAndArmedSecurityConfig = new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED);
        testUtils.createSecurityConfigFile(breachedAndArmedSecurityConfig);
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/security/silence-alarm", null, Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        SecurityConfig silencedSecurityConfig = new SecurityConfig(SecurityStatus.SAFE, SecurityState.DISARMED);
        testUtils.assertThatExpectedSecurityConfigJsonFileSaved(silencedSecurityConfig);

        testUtils.deleteSecurityConfigFile();
    }
}
