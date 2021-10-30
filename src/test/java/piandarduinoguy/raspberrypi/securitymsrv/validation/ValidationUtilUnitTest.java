package piandarduinoguy.raspberrypi.securitymsrv.validation;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import piandarduinoguy.raspberrypi.securitymsrv.exception.ImageFileException;
import piandarduinoguy.raspberrypi.securitymsrv.exception.PersonDetectorException;
import piandarduinoguy.raspberrypi.securitymsrv.service.PersonDetectorService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ValidationUtilUnitTest {
    @Autowired
    private PersonDetectorService personDetectorService;
    @Test
    @DisplayName("Given an annotated image does not exist " +
            "when getAnnotatedImage method called " +
            "then the exception thrown.")
    void canThrowExceptionIfAnnotatedImageDoesNotExist() {
        File imageFile = new File("a/location/to/an/image/that/does/not/exist");
        assertThatThrownBy(() -> ValidationUtil.validateImageFile(imageFile))
                .isInstanceOf(ImageFileException.class)
                .hasMessage("The File a/location/to/an/image/that/does/not/exist does not exist.");
    }

    @DisplayName("Given process has null " +
            "when validatePythonProcess method called " +
            "then throw exception")
    @Test
    void canThrowExceptionIfPythonProcessNull() {

        AssertionsForClassTypes.assertThatThrownBy(() -> ValidationUtil.validateProcess(null))
                .isInstanceOf(PersonDetectorException.class)
                .hasMessage("The process is null.");
    }

    @DisplayName("Given process not null " +
            "when validatePythonProcess method called " +
            "then throw no exception")
    @Test
    void doesNotThrowExceptionIfPythonProcessNotNull() {
        Process mockProcess = mock(Process.class);

        try{
            ValidationUtil.validateProcess(mockProcess);
        } catch (PersonDetectorException personDetectorException){
            fail(String.format("No pythonDetectorException expected. Exception message is %s", personDetectorException.getMessage()));
        }
    }

    @DisplayName("Given process logs don't contain expected logs " +
            "when validatePythonProcessLogs method called " +
            "then throw exception")
    @Test
    void canThrowExceptionIfPythonProcessLogsDontContainExpectedLogs() {
        List<String> processLogsNotExpected = new ArrayList<>();
        processLogsNotExpected.add("Log that are not expected.");
        AssertionsForClassTypes.assertThatThrownBy(() -> ValidationUtil.validateProcessLogs(processLogsNotExpected))
                .isInstanceOf(PersonDetectorException.class)
                .hasMessage("The process logs are not listed as valid logs, valid process logs are - 'Person detected.', 'Person not detected.'");
    }

    @DisplayName("Given process logs null" +
            "when validatePythonProcessLogs method called " +
            "then throw exception")
    @Test
    void canThrowExceptionIfPythonProcessLogsNull() {
        AssertionsForClassTypes.assertThatThrownBy(() -> ValidationUtil.validateProcessLogs(null))
                .isInstanceOf(PersonDetectorException.class)
                .hasMessage("processLogs are null.");

    }

    @DisplayName("Given process logs contain expected 'Person detected.' log " +
            "when validatePythonProcessLogs method called " +
            "then throw no exception")
    @Test
    void doesNotThrowExceptionIfPythonProcessLogIsPersonDetected() {
        List<String> processLogsNotExpected = new ArrayList<>();
        processLogsNotExpected.add("Person detected.");
        try {
            ValidationUtil.validateProcessLogs(processLogsNotExpected);
        } catch (PersonDetectorException personDetectorException) {
            fail(String.format("pythonDetectorException exception was thrown that was no expected, exception message is '%s'", personDetectorException.getMessage()));
        }
    }

    @DisplayName("Given process logs contain expected 'Person not detected.' log " +
            "when validatePythonProcessLogs method called " +
            "then throw no exception")
    @Test
    void doesNotThrowExceptionIfPythonProcessLogIsPersonNotDetected() {
        List<String> expectedProcessLogs = new ArrayList<>();
        expectedProcessLogs.add("Person not detected.");
        try {
            ValidationUtil.validateProcessLogs(expectedProcessLogs);
        } catch (PersonDetectorException personDetectorException) {
            fail(String.format("pythonDetectorException exception was thrown that was no expected, exception message is '%s'", personDetectorException.getMessage()));
        }
    }

}
