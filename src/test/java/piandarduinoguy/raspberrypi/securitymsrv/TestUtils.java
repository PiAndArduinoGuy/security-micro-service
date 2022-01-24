package piandarduinoguy.raspberrypi.securitymsrv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
public class TestUtils {
    @Autowired
    private ObjectMapper objectMapper;

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

    public String createBase64EncodedImageFromImageFile(File imageFile) throws Exception {
        return Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(imageFile));
    }

    public void assertThatExpectedTempImageFileCreated(String base64EncodedImage) throws Exception {
        assertThat(testTemporaryImageFile).exists();
        String base64EncodedSavedImage = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(testTemporaryImageFile));
        assertThat(base64EncodedSavedImage).isEqualToIgnoringCase(base64EncodedImage);
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
        String expectedBase64EncodedAnnotatedImage = Base64.getEncoder().encodeToString(annotatedImageBytes);
        return expectedBase64EncodedAnnotatedImage;
    }

    public byte[] getExpectedCapturedImageBytesFromFile(File sourcesImageFile) throws Exception {
        return FileUtils.readFileToByteArray(sourcesImageFile);
    }

    public void createExpectedCapturedImageFileFrom(File sourcesImageFile) throws Exception {
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
