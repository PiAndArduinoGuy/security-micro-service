package piandarduinoguy.raspberrypi.securitymsrv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.AssertionsForClassTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
public class TestUtils {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Source channel;

    @Autowired
    private MessageCollector messageCollector;

    @Value("${resources.base.location}")
    private String resourcesBaseLocation;

    @Value("${new-capture.file-name}")
    private String newCaptureFileName;

    @Value("${new-capture.annotated.file-name}")
    private String newCaptureAnnotatedFileName;

    public File testSecurityConfigFile;

    private File testAnnotatedImageFile;

    private File testTemporaryImageFile;


    @PostConstruct
    public void createTestSecurityConfigFile() {
        this.testSecurityConfigFile = new File(resourcesBaseLocation + "security_config.json");
        this.testAnnotatedImageFile = new File(String.format("%s/%s.jpeg", resourcesBaseLocation, newCaptureAnnotatedFileName));
        this.testTemporaryImageFile = new File(String.format("%s/%s.jpeg", resourcesBaseLocation, newCaptureFileName));
    }

    public void assertThatExpectedSecurityConfigJsonFileSaved(SecurityConfig expectedSecurityConfig) throws Exception {
        SecurityConfig securityConfig = objectMapper.readValue(testSecurityConfigFile, SecurityConfig.class);
        assertThat(securityConfig.getSecurityStatus()).isEqualTo(expectedSecurityConfig.getSecurityStatus());
        assertThat(securityConfig.getSecurityState()).isEqualTo(expectedSecurityConfig.getSecurityState());
    }

    public void deleteSecurityConfigFile() {
        this.testSecurityConfigFile.delete();
    }

    public void createSecurityConfigFile(SecurityConfig securityConfig) throws IOException {
        objectMapper.writeValue(testSecurityConfigFile, securityConfig);
    }

    public void deleteTestTemporaryImageFile() {
        this.testTemporaryImageFile.delete();
    }

    public static MockMultipartFile createMockMultipartImageFile() throws Exception {
        return new MockMultipartFile(
                "image",
                "test_new_capture.jpeg",
                "multipart/form-data",
                new FileInputStream("src/test/resources/test_new_capture_person.jpeg"));
    }

    public void assertThatExpectedTempImageFileCreated(MockMultipartFile image) throws Exception {
        assertThat(testTemporaryImageFile).exists();
        byte[] expectedImageByteData = image.getBytes();
        byte[] savedImageByteData = FileUtils.readFileToByteArray(testTemporaryImageFile);
        assertThat(expectedImageByteData).isEqualTo(savedImageByteData);
    }

    public void assertThatExpectedTempImageFileCreated(byte[] expectedImageByteData) throws Exception {
        assertThat(testTemporaryImageFile).exists();
        byte[] savedImageByteData = FileUtils.readFileToByteArray(testTemporaryImageFile);
        assertThat(expectedImageByteData).isEqualTo(savedImageByteData);
    }

    public void assertThatExpectedAnnotatedImageCreated() {
        assertTrue(this.testAnnotatedImageFile.exists());
    }

    public void createExpectedAnnotatedImageFile() throws IOException {
        File annotatedImageFileSource = new File(String.format("src/test/resources/%s.jpeg", newCaptureAnnotatedFileName));
        byte[] annotatedImageBytes = FileUtils.readFileToByteArray(annotatedImageFileSource);
        FileUtils.writeByteArrayToFile(this.testAnnotatedImageFile, annotatedImageBytes);
    }

    public void deleteAnnotatedImage() {
        this.testAnnotatedImageFile.delete();
    }

    public String getExpectedBase64EncodedAnnotatedImage() throws IOException {
        byte[] annotatedImageBytes = FileUtils.readFileToByteArray(this.testAnnotatedImageFile);
        String expectedBase64EncodedAnnotatedImage = Base64.encode(annotatedImageBytes);
        return expectedBase64EncodedAnnotatedImage;
    }

    public byte[] getExpectedCapturedImageBytesFromFile(File sourcesImageFile) throws Exception {
        return FileUtils.readFileToByteArray(sourcesImageFile);
    }

    public void assertExpectedSecurityConfigPublishedOnOutputChannel(SecurityConfig securityConfig) {
        try{
            SecurityConfig receivedSecurityConfig = objectMapper.readValue((String) messageCollector.forChannel(channel.output()).poll().getPayload(), SecurityConfig.class);
            AssertionsForClassTypes.assertThat(receivedSecurityConfig).isNotNull();
            AssertionsForClassTypes.assertThat(receivedSecurityConfig.getSecurityState()).isEqualTo(securityConfig.getSecurityState());
            AssertionsForClassTypes.assertThat(receivedSecurityConfig.getSecurityStatus()).isEqualTo(securityConfig.getSecurityStatus());
        } catch (ClassCastException | JsonProcessingException exception){
            fail("Received message could not be marshalled to a SecurityConfig type, the message sent might not have been of type SecurityConfig", exception);
        }
    }

    public void createExpectedCapturedImageFileFrom(File sourcesImageFile) throws Exception{
        byte[] annotatedImageBytes = FileUtils.readFileToByteArray(sourcesImageFile);
        FileUtils.writeByteArrayToFile(this.testTemporaryImageFile, annotatedImageBytes);
    }

    public void assertThatNoAnnotatedImageCreated() {
        assertFalse(this.testAnnotatedImageFile.exists());
    }

    public void assertThatNoSecurityConfigFileCreated() {
        assertFalse(testSecurityConfigFile.exists());
    }
}
