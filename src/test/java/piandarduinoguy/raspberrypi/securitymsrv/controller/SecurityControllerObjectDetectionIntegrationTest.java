package piandarduinoguy.raspberrypi.securitymsrv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.Problem;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityStatus;
import piandarduinoguy.raspberrypi.securitymsrv.exception.PersonDetectorException;
import piandarduinoguy.raspberrypi.securitymsrv.service.PersonDetectorService;
import piandarduinoguy.raspberrypi.securitymsrv.service.SecurityService;
import piandarduinoguy.raspberrypi.securitymsrv.validation.ValidationUtil;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
class SecurityControllerObjectDetectionIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityService securityService;

    @SpyBean
    private PersonDetectorService personDetectorService;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${resources.base.location}")
    private String resourcesBaseLocation;

    @Value("${new-capture.annotated.file-name}")
    private String newCaptureAnnotatedFileName;

    @DisplayName("Given a valid multipart file containing image of a person and security status is armed" +
            "when post to the /security-check endpoint is made " +
            "then the image file gets saved at the expected location and detections performed and security config is updated.")
    @Test
    void canSaveAnnotatedImageAndUpdateSecurityConfigToBreach() throws Exception {
        MockMultipartFile image = TestUtils.createMockMultipartImageFile();
        SecurityConfig securityConfig = new SecurityConfig(SecurityStatus.SAFE, SecurityState.ARMED);
        testUtils.createSecurityConfigFile(securityConfig);

        mockMvc.perform(
                multipart("/security-check").file(image)).
                andExpect(status().isAccepted());

        testUtils.assertThatExpectedTempImageFileCreated(image);
        File expectedProcessedImage = new File(String.format("%s/%s.jpeg", resourcesBaseLocation, newCaptureAnnotatedFileName));
        assertTrue(expectedProcessedImage.exists());

        SecurityConfig updatedSecurityConfig = new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED);
        testUtils.assertThatExpectedSecurityConfigJsonFileSaved(updatedSecurityConfig);


        testUtils.deleteTestTemporaryImageFile();
        testUtils.deleteAnnotatedImage();
        testUtils.deleteSecurityConfigFile();
    }

    @DisplayName("Given a valid multipart file containing image of a person and security status is disarmed" +
            "when post to the /security-check endpoint is made " +
            "then the image file gets saved at the expected location and detections performed but security config is not updated.")
    @Test
    void doesNotSaveAnnotatedImageAndDoesNotUpdateSecurityConfig() throws Exception {
        MockMultipartFile image = TestUtils.createMockMultipartImageFile();
        SecurityConfig securityConfig = new SecurityConfig(SecurityStatus.SAFE, SecurityState.DISARMED);
        testUtils.createSecurityConfigFile(securityConfig);

        mockMvc.perform(
                multipart("/security-check").file(image)).
                andExpect(status().isAccepted());

        testUtils.assertThatExpectedTempImageFileCreated(image);
        File expectedProcessedImage = new File(String.format("%s/%s.jpeg", resourcesBaseLocation, newCaptureAnnotatedFileName));
        assertTrue(expectedProcessedImage.exists());
        testUtils.assertThatExpectedSecurityConfigJsonFileSaved(securityConfig); // ensure no update is made to the security config


        testUtils.deleteTestTemporaryImageFile();
        testUtils.deleteAnnotatedImage();
        testUtils.deleteSecurityConfigFile();
    }

    @DisplayName("Given an IOException thrown " +
            "when post to the /security-check endpoint is made " +
            "then return expected Zalando problem")
    @Test
    void canReturnZalandoProblemIfSaveImageMethodThrowsIoException() throws Exception {
        MockMultipartFile image = TestUtils.createMockMultipartImageFile();

        try (MockedStatic<FileUtils> mockFileUtils = mockStatic(FileUtils.class)) {
            mockFileUtils.when(() -> FileUtils.writeByteArrayToFile(any(), any())).thenThrow(new IOException("I am an IOException"));
            MvcResult mvcResult = mockMvc.perform(
                    multipart("/security-check").file(image)).
                    andExpect(status().isInternalServerError())
                    .andReturn();
            String actualZalandoProblemJsonString = mvcResult.getResponse().getContentAsString();
            Problem expectedZalandoProblem = createExpectedZalandoProblem("The image test_new_capture.jpeg could not be saved to the directory src/test/resources/application/. An IOException was thrown with message \"I am an IOException\".");
            String expectedZalandoProblemJsonString = objectMapper.writeValueAsString(expectedZalandoProblem);
            assertThat(actualZalandoProblemJsonString).isEqualToIgnoringCase(expectedZalandoProblemJsonString);
            testUtils.assertThatNoAnnotatedImageCreated();
        }

        testUtils.deleteTestTemporaryImageFile();
    }

    @DisplayName("Given an IOException thrown " +
            "when get to the /annotated-image endpoint is made " +
            "then return expected Zalando problem")
    @Test
    void canReturnZalandoProblemIfGetAnnotatedImageMethodThrowsIoException() throws Exception {
        testUtils.createExpectedAnnotatedImageFile();

        try (MockedStatic<FileUtils> mockFileUtils = mockStatic(FileUtils.class)) {
            mockFileUtils.when(() -> FileUtils.readFileToByteArray(any())).thenThrow(new IOException("I am an IOException"));
            MvcResult mvcResult = mockMvc.perform(
                    get("/annotated-image"))
                    .andReturn();
            String actualZalandoProblemJsonString = mvcResult.getResponse().getContentAsString();
            Problem expectedZalandoProblem = createExpectedZalandoProblem("The image test_new_capture_annotated.jpeg could not be encoded to base64 string due to an IOException being thrown with message \"I am an IOException\".");
            String expectedZalandoProblemJsonString = objectMapper.writeValueAsString(expectedZalandoProblem);
            assertThat(actualZalandoProblemJsonString).isEqualToIgnoringCase(expectedZalandoProblemJsonString);
        }

        testUtils.deleteAnnotatedImage();
    }

    @Test
    void canReturnZalandoProblemIfValidatePythonProcessMethodThrowsPersonDetectorException() throws Exception{
        MockMultipartFile image = TestUtils.createMockMultipartImageFile();
        doNothing().when(personDetectorService).runPersonDetectorProcess(image.getBytes());

        try (MockedStatic<ValidationUtil> mockValidationUtil = mockStatic(ValidationUtil.class)) {
            mockValidationUtil.when(() -> ValidationUtil.validateProcess(any())).thenThrow(new PersonDetectorException("I am a PersonDetectorException."));
            MvcResult mvcResult = mockMvc.perform(
                    multipart("/security-check").file(image)).
                    andExpect(status().isInternalServerError())
                    .andReturn();
            String actualZalandoProblemJsonString = mvcResult.getResponse().getContentAsString();
            Problem expectedZalandoProblem = createExpectedZalandoProblem("I am a PersonDetectorException.");
            String expectedZalandoProblemJsonString = objectMapper.writeValueAsString(expectedZalandoProblem);
            assertThat(actualZalandoProblemJsonString).isEqualToIgnoringCase(expectedZalandoProblemJsonString);
            testUtils.assertThatNoAnnotatedImageCreated();
        }

        testUtils.deleteTestTemporaryImageFile();
    }

    @Test
    void canReturnZalandoProblemIfValidatePythonProcessLogsMethodThrowsPersonDetectorException() throws Exception{
        MockMultipartFile image = TestUtils.createMockMultipartImageFile();

        try (MockedStatic<ValidationUtil> mockValidationUtil = mockStatic(ValidationUtil.class)) {
            mockValidationUtil.when(() -> ValidationUtil.validateProcessLogs(any())).thenThrow(new PersonDetectorException("I am a PersonDetectorException."));
            MvcResult mvcResult = mockMvc.perform(
                    multipart("/security-check").file(image)).
                    andExpect(status().isInternalServerError())
                    .andReturn();
            String actualZalandoProblemJsonString = mvcResult.getResponse().getContentAsString();
            Problem expectedZalandoProblem = createExpectedZalandoProblem("I am a PersonDetectorException.");
            String expectedZalandoProblemJsonString = objectMapper.writeValueAsString(expectedZalandoProblem);
            assertThat(actualZalandoProblemJsonString).isEqualToIgnoringCase(expectedZalandoProblemJsonString);
        }

        // we need to run the real runPersonDetectorProcess method cause we cannot mock privates,
        // thus an annotated image is created by running the python process, delete it.
        // this also means we cannot assert that an annotated image was not created.
        testUtils.deleteAnnotatedImage();
        testUtils.deleteTestTemporaryImageFile();
    }


    private Problem createExpectedZalandoProblem(String detail) {
        Problem expectedZalandoProblem = new Problem();
        expectedZalandoProblem.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        expectedZalandoProblem.setDetail(detail);
        expectedZalandoProblem.setTitle(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        return expectedZalandoProblem;
    }
}
