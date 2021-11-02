package piandarduinoguy.raspberrypi.securitymsrv.service;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;
import piandarduinoguy.raspberrypi.securitymsrv.exception.ImageFileException;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
class PersonDetectorServiceUnitTest {
    @Autowired
    private PersonDetectorService personDetectorService;

    @Autowired
    private TestUtils testUtils;

    @DisplayName("Given python process logs contain the 'Person detected.' string " +
            "when hasPersonBeenDetectFromPythonLogs method called " +
            "then return true")
    @Test
    @DirtiesContext
    void canReturnTrueIfPythonLogsReportPersonDetected() throws Exception {
        personDetectorService.runPersonDetectorProcess(testUtils.getExpectedCapturedImageBytesFromFile(new File("src/test/resources/test_new_capture_person.jpeg")));

        assertTrue(personDetectorService.hasPersonBeenDetected());
        testUtils.deleteTestTemporaryImageFile();
        testUtils.deleteAnnotatedImage();
    }

    @Test
    @DisplayName("Given a valid multipart file " +
            "when saveImage called " +
            "then image is saved as expected.")
    void canSaveUploadedImageToBeProcessed() throws Exception {
        String base64EncodedImage = testUtils.createBase64EncodedImageFromImageFile(new File("src/test/resources/test_new_capture_person.jpeg"));
        byte[] imageBytes = Base64.getDecoder().decode(base64EncodedImage);

        personDetectorService.runPersonDetectorProcess(imageBytes);

        testUtils.assertThatExpectedTempImageFileCreated(base64EncodedImage);
        testUtils.deleteTestTemporaryImageFile();
    }

    @Test
    @DisplayName("Given FileUtils.writeByteArrayToFile method throws an IOException " +
            "when saveImage called " +
            "then an ImageFileException is thrown.")
    void canThrowImageFileExceptionWhenSaveImageCalled() throws Exception {
        String base64EncodedImage = testUtils.createBase64EncodedImageFromImageFile(new File("src/test/resources/test_new_capture_person.jpeg"));
        byte[] imageBytes = Base64.getDecoder().decode(base64EncodedImage);

        try (MockedStatic<FileUtils> mockFileUtils = mockStatic(FileUtils.class)) {
            mockFileUtils.when(()->FileUtils.writeByteArrayToFile(any(), any())).thenThrow(new IOException("I am an IOException"));
            assertThatThrownBy(() -> personDetectorService.runPersonDetectorProcess(imageBytes))
                    .isInstanceOf(ImageFileException.class)
                    .hasMessage("The image test_new_capture.jpeg could not be saved to the directory src/test/resources/application/. An IOException was thrown with message \"I am an IOException\".");
        }

        testUtils.deleteTestTemporaryImageFile();
    }

    @DisplayName("Given python process logs contain the 'No person detected.' string " +
            "when hasPersonBeenDetectFromPythonLogs method called " +
            "then return false")
    @Test
    @DirtiesContext
    void canReturnFalseIfPythonLogsReportNoPersonDetected() throws Exception {
        personDetectorService.runPersonDetectorProcess(testUtils.getExpectedCapturedImageBytesFromFile(new File("src/test/resources/test_new_capture_no_person.jpeg")));

        assertFalse(personDetectorService.hasPersonBeenDetected());

        testUtils.deleteTestTemporaryImageFile();
    }

    @DisplayName("Given python process start method throws IO Exception" +
            "when runPythonPersonDetectorProcess method called " +
            "then throw expected exception")
    @Test
    @DirtiesContext
    void canThrowExceptionWhenProcessStartMethodThrowsException() throws Exception {
        personDetectorService.runPersonDetectorProcess(testUtils.getExpectedCapturedImageBytesFromFile(new File("src/test/resources/test_new_capture_no_person.jpeg")));

        assertFalse(personDetectorService.hasPersonBeenDetected());

        testUtils.deleteTestTemporaryImageFile();
    }
}
