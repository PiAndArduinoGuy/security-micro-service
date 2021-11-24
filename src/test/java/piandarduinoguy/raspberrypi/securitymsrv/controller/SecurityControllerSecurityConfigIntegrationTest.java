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
        Problem zalandoProblem = responseEntity.getBody();
        assertExpectedZalandoProblem(zalandoProblem, HttpStatus.NOT_FOUND, "The File src/test/resources/application/test_new_capture_annotated.jpeg does not exist.");

    }

    @DisplayName("Given security config is DISARMED and SAFE " +
            "when put to te /arm-alarm endpoint is made " +
            "then return OK and update security config")
    @Test
    void canArmAlarm() throws Exception {
        testUtils.createSecurityConfigFile(new SecurityConfig(SecurityStatus.SAFE, SecurityState.DISARMED));

        HttpEntity httpEntity = new HttpEntity( null);
        ResponseEntity<SecurityConfig> responseEntity = restTemplate.exchange("http://localhost:" + port + "/security/arm-alarm", HttpMethod.PUT, httpEntity, SecurityConfig.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        SecurityConfig updatedSecurityConfig = responseEntity.getBody();
        testUtils.assertThatExpectedSecurityConfigJsonFileSaved(updatedSecurityConfig);

        testUtils.deleteSecurityConfigFile();
    }

    @DisplayName("Given security config is ARMED and SAFE " +
            "when put to te /arm-alarm endpoint is made " +
            "then return 400 bad request returned.")
    @Test
    void doesNotArmAlreadyArmedAlarm() throws Exception {
        testUtils.createSecurityConfigFile(new SecurityConfig(SecurityStatus.SAFE, SecurityState.ARMED));

        HttpEntity httpEntity = new HttpEntity( null);
        ResponseEntity<Problem> responseEntity = restTemplate.exchange("http://localhost:" + port + "/security/arm-alarm", HttpMethod.PUT, httpEntity, Problem.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Problem zalandoProblem = responseEntity.getBody();
        assertExpectedZalandoProblem(zalandoProblem, HttpStatus.BAD_REQUEST, "Security can not be armed with it in a state of ARMED already.");

        testUtils.deleteSecurityConfigFile();
    }

    @DisplayName("Given security config is DISARMED and BREACHED " +
            "when put to te /arm-alarm endpoint is made " +
            "then return 400 bad request returned.")
    @Test
    void doesNotArmBreachedSecurity() throws Exception {
        testUtils.createSecurityConfigFile(new SecurityConfig(SecurityStatus.BREACHED, SecurityState.DISARMED));

        HttpEntity httpEntity = new HttpEntity( null);
        ResponseEntity<Problem> responseEntity = restTemplate.exchange("http://localhost:" + port + "/security/arm-alarm", HttpMethod.PUT, httpEntity, Problem.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Problem zalandoProblem = responseEntity.getBody();
        assertExpectedZalandoProblem(zalandoProblem, HttpStatus.BAD_REQUEST, "Security can not be armed with security status BREACHED.");

        testUtils.deleteSecurityConfigFile();
    }

    @DisplayName("Given security config is DISARMED and BREACHED " +
            "when put to te /silence-alarm endpoint is made " +
            "then return 400 bad request returned.")
    @Test
    void doesNotSilenceDisarmedAlarm() throws Exception {
        testUtils.createSecurityConfigFile(new SecurityConfig(SecurityStatus.BREACHED, SecurityState.DISARMED));

        HttpEntity httpEntity = new HttpEntity( null);
        ResponseEntity<Problem> responseEntity = restTemplate.exchange("http://localhost:" + port + "/security/silence-alarm", HttpMethod.PUT, httpEntity, Problem.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Problem zalandoProblem = responseEntity.getBody();
        assertExpectedZalandoProblem(zalandoProblem, HttpStatus.BAD_REQUEST, "Security cannot be silenced with it in a DISARMED state.");

        testUtils.deleteSecurityConfigFile();
    }

    @DisplayName("Given security config is ARMED and SAFE " +
            "when put to te /silence-alarm endpoint is made " +
            "then return 400 bad request returned.")
    @Test
    void doesNotSilenceSafeAlarm() throws Exception {
        testUtils.createSecurityConfigFile(new SecurityConfig(SecurityStatus.SAFE, SecurityState.ARMED));

        HttpEntity httpEntity = new HttpEntity( null);
        ResponseEntity<Problem> responseEntity = restTemplate.exchange("http://localhost:" + port + "/security/silence-alarm", HttpMethod.PUT, httpEntity, Problem.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Problem zalandoProblem = responseEntity.getBody();
        assertExpectedZalandoProblem(zalandoProblem, HttpStatus.BAD_REQUEST, "Security cannot be silenced with it in a SAFE status.");

        testUtils.deleteSecurityConfigFile();
    }

    @DisplayName("Given securityConfig is ARMED and BREACHED " +
            "when put to the /silence-alarm endpoint is made" +
            "then return OK response and securityConfig status is SAFE and state is DISARMED")
    @Test
    void canSilenceAlarm() throws Exception {
        testUtils.createSecurityConfigFile(new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED));

        HttpEntity httpEntity = new HttpEntity( null);
        ResponseEntity<Problem> responseEntity = restTemplate.exchange("http://localhost:" + port + "/security/silence-alarm", HttpMethod.PUT, httpEntity, Problem.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        SecurityConfig updatedSecurityConfig = new SecurityConfig(SecurityStatus.SAFE, SecurityState.DISARMED);
        testUtils.assertThatExpectedSecurityConfigJsonFileSaved(updatedSecurityConfig);

        testUtils.deleteSecurityConfigFile();
    }

    private void assertExpectedZalandoProblem(Problem zalandoProblem, HttpStatus httpStatus, String detail){
        assertThat(zalandoProblem).isNotNull();
        assertThat(zalandoProblem.getStatus()).isEqualTo(httpStatus.value());
        assertThat(zalandoProblem.getDetail()).isEqualToIgnoringCase(detail);
        assertThat(zalandoProblem.getTitle()).isEqualToIgnoringCase(httpStatus.getReasonPhrase());

    }
}
