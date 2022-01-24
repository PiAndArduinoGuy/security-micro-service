package piandarduinoguy.raspberrypi.securitymsrv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityStatus;
import piandarduinoguy.raspberrypi.securitymsrv.exception.ImageFileException;
import piandarduinoguy.raspberrypi.securitymsrv.exception.SecurityConfigFileException;
import piandarduinoguy.raspberrypi.securitymsrv.exception.SecurityConfigStateException;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
class SecurityServiceUnitTest {
    @SpyBean
    private ObjectMapper objectMapper;

    @MockBean
    private PersonDetectorService personDetectorService;

    @SpyBean
    private SecurityService securityService;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TestUtils testUtils;

    @Value("${resources.base.location}")
    private String resourcesBaseLocation;

    @Value("${new-capture.file-name}")
    private String newCaptureFileName;

    @Value("${new-capture.annotated.file-name}")
    private String newCaptureAnnotatedFileName;


    @Test
    @DisplayName("Given a security config file exists with security status BREACHED and security state ARMED, " +
            "when getSecurityConfig called, " +
            "then returned with expected domain object attributes set.")
    void canGetSecurityConfig() throws Exception {
        testUtils.createSecurityConfigFile(new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED));

        SecurityConfig securityConfig = securityService.getSecurityConfig();

        assertThat(securityConfig.getSecurityStatus()).isEqualTo(SecurityStatus.BREACHED);
        assertThat(securityConfig.getSecurityState()).isEqualTo(SecurityState.ARMED);

        testUtils.deleteSecurityConfigFile();
    }

    @Test
    @DisplayName("Given a security config domain object with attributes BREACHED for security status and ARMED for security state, " +
            "when saveSecurityConfig called, " +
            "then save a security_config.json file with fields set accordingly and return saved security config.")
    void canSaveSecurityConfig() throws Exception {
        SecurityConfig securityConfig = new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED);

        SecurityConfig savedSecurityConfig = securityService.saveSecurityConfig(securityConfig);

        testUtils.assertThatExpectedSecurityConfigJsonFileSaved(securityConfig);
        assertThat(savedSecurityConfig).isNotNull();
        assertThat(savedSecurityConfig.getSecurityStatus()).isEqualTo(SecurityStatus.BREACHED);
        assertThat(savedSecurityConfig.getSecurityState()).isEqualTo(SecurityState.ARMED);

        testUtils.deleteSecurityConfigFile();
    }

    @Test
    @DisplayName("Given object mapper throws an IO exception " +
            "when saveSecurityConfig called " +
            "then throw SecurityConfigFileSaveException with expected message.")
    void canThrowSecurityConfigFileExceptionIfObjectMapperWriteValueMethodThrowsIOException() throws Exception {
        doThrow(new IOException("An IO exception has occurred.")).when(objectMapper).writeValue(any(File.class), any(SecurityConfig.class));

        SecurityConfig securityConfig = new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED);

        assertThatThrownBy(() -> securityService.saveSecurityConfig(securityConfig))
                .isInstanceOf(SecurityConfigFileException.class)
                .hasMessage("Could not save the security config file object SecurityConfig(securityStatus=BREACHED, securityState=ARMED) to src/test/resources/application/security_config.json due to an IOException with message \"An IO exception has occurred.\".");

        testUtils.assertThatNoSecurityConfigFileCreated();
    }

    @Test
    @DisplayName("Given object mapper throws an IO exception " +
            "when saveSecurityConfig called " +
            "then throw SecurityConfigFileSaveException with expected message.")
    void canThrowSecurityConfigFileExceptionIfObjectMapperReadMethodThrowsIOException() throws Exception {
        doThrow(new IOException("An IO exception has occurred.")).when(objectMapper).readValue(eq(testUtils.testSecurityConfigFile), eq(SecurityConfig.class));
        assertThatThrownBy(() -> securityService.getSecurityConfig())
                .isInstanceOf(SecurityConfigFileException.class)
                .hasMessage("Could not retrieve security config due to an IOException with message \"An IO exception has occurred.\".");

        testUtils.assertThatNoSecurityConfigFileCreated();

    }

    @Test
    @DisplayName("Given an annotated image exists " +
            "when getAnnotatedImage method called " +
            "then the expected is returned as base64 encoded image.")
    void canReturnAnnotatedImage() throws Exception {
        testUtils.createExpectedAnnotatedImageFile();

        String base64AnnotatedImage = securityService.getBase64AnnotatedImage();

        assertThat(testUtils.getExpectedBase64EncodedAnnotatedImage()).isEqualToIgnoringCase(base64AnnotatedImage);

        testUtils.deleteAnnotatedImage();
    }

    @Test
    @DisplayName("Given FileUtils.readFileToByteArray method throws an IOException " +
            "when getBase64AnnotatedImage called " +
            "then an ImageFileException is thrown.")
    void canThrowImageFileExceptionWhenGetBase64AnnotatedImageCalled() throws Exception {
        testUtils.createExpectedAnnotatedImageFile();

        try (MockedStatic<FileUtils> mockFileUtils = mockStatic(FileUtils.class)) {
            mockFileUtils.when(() -> FileUtils.readFileToByteArray(any())).thenThrow(new IOException("I am an IOException"));
            assertThatThrownBy(() -> securityService.getBase64AnnotatedImage())
                    .isInstanceOf(ImageFileException.class)
                    .hasMessage("The image test_new_capture_annotated.jpeg could not be encoded to base64 string due to an IOException being thrown with message \"I am an IOException\".");
        }

        testUtils.deleteAnnotatedImage();
    }

    @Test
    @DisplayName("Given SecurityConfig has BREACHED status and ARMED state " +
            "when silenceAlarm method called " +
            "then SecurityConfig status set to SAFE and state set to DISARMED")
    void canSilenceAlarm() throws Exception {
        SecurityConfig breachedAndArmedSecurityConfig = new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED);
        testUtils.createSecurityConfigFile(breachedAndArmedSecurityConfig);

        securityService.silenceAlarm();

        SecurityConfig silencedSecurityConfig = new SecurityConfig(SecurityStatus.SAFE, SecurityState.DISARMED);
        testUtils.assertThatExpectedSecurityConfigJsonFileSaved(silencedSecurityConfig);
        testUtils.deleteSecurityConfigFile();
    }


    @Test
    @DisplayName("Given no security config file " +
            "when getSecurityConfig method called " +
            "then throw exception")
    void canThrowExceptionIfNoSecurityConfigFile() throws Exception {
        assertThatThrownBy(() -> securityService.getSecurityConfig())
                .isInstanceOf(SecurityConfigFileException.class)
                .hasMessage("The SecurityConfig file was not found. FileNotFoundException with message \"src/test/resources/application/security_config.json (No such file or directory)\" was thrown.");
    }
}
