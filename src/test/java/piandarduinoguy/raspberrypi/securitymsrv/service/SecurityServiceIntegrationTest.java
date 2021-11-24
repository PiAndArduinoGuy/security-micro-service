package piandarduinoguy.raspberrypi.securitymsrv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.test.context.TestPropertySource;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityStatus;
import piandarduinoguy.raspberrypi.securitymsrv.exception.SecurityConfigStateException;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    @SpyBean
    private SecurityService securityService;

    @Test
    @DisplayName("Given a base64 encoded image of a person " +
            "when detectPersonFromUploadedImage called " +
            "then return that a person was detected and the expected annotated image must be saved.")
    void canDetectPersonWhenImageContainsPerson() throws Exception {
        String base64EncodedImageNoPerson = testUtils.createBase64EncodedImageFromImageFile(new File("src/test/resources/test_new_capture_person.jpeg"));
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
    void canDetectNoPersonWhenImageContainsNoPerson() throws Exception {
        byte[] imageBytes = testUtils.getExpectedCapturedImageBytesFromFile(new File("src/test/resources/test_new_capture_no_person.jpeg"));

        assertFalse(securityService.detectPerson(imageBytes));
        testUtils.assertThatNoAnnotatedImageCreated();

        testUtils.deleteTestTemporaryImageFile();
    }

    @Test
    @DisplayName("Given a SecurityConfig object to save " +
            "when saveSecurityConfig called " +
            "then publish updated SecurityConfig object and returned.")
    void canPublishUpdatedSecurityConfig() {
        SecurityConfig securityConfig = new SecurityConfig(SecurityStatus.SAFE, SecurityState.DISARMED);

        SecurityConfig savedSecurityConfig = securityService.saveSecurityConfig(securityConfig);
        assertThat(savedSecurityConfig.getSecurityState()).isEqualTo(SecurityState.DISARMED);
        assertThat(savedSecurityConfig.getSecurityStatus()).isEqualTo(SecurityStatus.SAFE);


        testUtils.assertExpectedSecurityConfigPublishedOnOutputChannel(securityConfig);
        testUtils.deleteSecurityConfigFile();
    }

    @Test
    @DisplayName("Given the security config has security state of armed " +
            "when armAlarm method called " +
            "then throw an exception.")
    void canPreventArmingWhenNotAlreadyArmed() throws Exception {
        SecurityConfig armedSecurityConfig = new SecurityConfig(SecurityStatus.SAFE, SecurityState.ARMED);
        testUtils.createSecurityConfigFile(armedSecurityConfig);

        assertThatThrownBy(() -> securityService.armAlarm())
                .isInstanceOf(SecurityConfigStateException.class)
                .hasMessage("Security can not be armed with it in a state of ARMED already.");

        verify(securityService, times(0)).saveSecurityConfig(any());
        testUtils.deleteSecurityConfigFile();
    }

    @Test
    @DisplayName("Given the security config has security status of breached " +
            "when armAlarm method called " +
            "then throw an exception.")
    void canPreventArmingWhenBreached() throws Exception {
        SecurityConfig armedSecurityConfig = new SecurityConfig(SecurityStatus.BREACHED, SecurityState.DISARMED);
        testUtils.createSecurityConfigFile(armedSecurityConfig);

        assertThatThrownBy(() -> securityService.armAlarm())
                .isInstanceOf(SecurityConfigStateException.class)
                .hasMessage("Security can not be armed with security status BREACHED.");

        verify(securityService, times(0)).saveSecurityConfig(any());
        testUtils.deleteSecurityConfigFile();
    }


    @Test
    @DisplayName("Given the security config dictates the possibility to arm security " +
            "when armAlarm method called " +
            "then update security config dictating an armed state and returned updated security config.")
    void canArmSecurity() throws Exception {
        SecurityConfig unarmedSecurityConfig = new SecurityConfig(SecurityStatus.SAFE, SecurityState.DISARMED);
        testUtils.createSecurityConfigFile(unarmedSecurityConfig);

        SecurityConfig updatedSecurityConfig = securityService.armAlarm();

        assertThat(updatedSecurityConfig.getSecurityStatus()).isEqualTo(SecurityStatus.SAFE);
        assertThat(updatedSecurityConfig.getSecurityState()).isEqualTo(SecurityState.ARMED);

        testUtils.deleteSecurityConfigFile();
    }

    @DisplayName("Given the security config has security status safe " +
            "when silenceAlarm called " +
            "then throw exception")
    @Test
    void canPreventAlarmSilenceWhenSecurityConfigSafe() throws Exception {
        testUtils.createSecurityConfigFile(new SecurityConfig(SecurityStatus.SAFE, SecurityState.ARMED));

        assertThatThrownBy(() -> securityService.silenceAlarm())
                .isInstanceOf(SecurityConfigStateException.class)
                .hasMessage("Security cannot be silenced with it in a SAFE status.");
        verify(securityService, times(0)).saveSecurityConfig(any());

        testUtils.deleteSecurityConfigFile();
    }

    @DisplayName("Given the security config has security state disarmed " +
            "when silenceAlarm called " +
            "then throw exception")
    @Test
    void canPreventAlarmSilenceWhenSecurityConfigDisarmed() throws Exception {
        testUtils.createSecurityConfigFile(new SecurityConfig(SecurityStatus.BREACHED, SecurityState.DISARMED));

        assertThatThrownBy(() -> securityService.silenceAlarm())
                .isInstanceOf(SecurityConfigStateException.class)
                .hasMessage("Security cannot be silenced with it in a DISARMED state.");
        verify(securityService, times(0)).saveSecurityConfig(any());

        testUtils.deleteSecurityConfigFile();
    }

    @DisplayName("Given the security config has security state armed and security status breached " +
            "when silenceAlarm called " +
            "then silence alarm")
    @Test
    void canSilenceAlarmWhenSecurityConfigAllowsIt() throws Exception {
        testUtils.createSecurityConfigFile(new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED));

        try {
            SecurityConfig updatedSecurityConfig = securityService.silenceAlarm();
            assertThat(updatedSecurityConfig.getSecurityState()).isEqualTo(SecurityState.DISARMED);
            assertThat(updatedSecurityConfig.getSecurityStatus()).isEqualTo(SecurityStatus.SAFE);
        } catch (Exception e){
            fail(String.format("An exception was thrown of type %s but was not expected.", e.getClass().getSimpleName()));
        } finally {
            testUtils.deleteSecurityConfigFile();
        }


    }


}
