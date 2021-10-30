package piandarduinoguy.raspberrypi.securitymsrv.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.test.context.TestPropertySource;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityStatus;

import java.io.File;

import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
class SecurityServiceIntegrationTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Source channel;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private SecurityService securityService;

    @Test
    @DisplayName("Given an image with a person " +
            "when detectPersonFromUploadedImage called " +
            "then return that a person was detected and the expected annotated image must be saved.")
    void canDetectPersonWhenImageContainsPerson() throws Exception{
        byte[] imageBytes = testUtils.getExpectedCapturedImageBytesFromFile(new File("src/test/resources/test_new_capture_person.jpeg"));

        assertTrue(securityService.detectPerson(imageBytes));
        testUtils.assertThatExpectedAnnotatedImageCreated();

        testUtils.deleteTestTemporaryImageFile();
        testUtils.deleteAnnotatedImage();
    }

    @Test
    @DisplayName("Given an image with no person in it" +
            "when detectPersonFromUploadedImage called " +
            "then return that a person was not detected and that no annotated image was saved.")
    void canDetectNoPersonWhenImageContainsNoPerson() throws Exception{
        byte[] imageBytes = testUtils.getExpectedCapturedImageBytesFromFile(new File("src/test/resources/test_new_capture_no_person.jpeg"));

        assertFalse(securityService.detectPerson(imageBytes));
        testUtils.assertThatNoAnnotatedImageCreated();

        testUtils.deleteTestTemporaryImageFile();
    }

    @Test
    @DisplayName("Given a SecurityConfig object to save " +
            "when saveSecurityConfig called " +
            "then publish updated SecurityConfig object.")
    void canPublishUpdatedSecurityConfig(){
        SecurityConfig securityConfig = new SecurityConfig(SecurityStatus.SAFE, SecurityState.DISARMED);

        securityService.saveSecurityConfig(securityConfig);

        testUtils.assertExpectedSecurityConfigPublishedOnOutputChannel(securityConfig);
        testUtils.deleteSecurityConfigFile();
    }
}
