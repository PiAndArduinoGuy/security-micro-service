package piandarduinoguy.raspberrypi.securitymsrv.service;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import piandarduinoguy.raspberrypi.securitymsrv.exception.ImageFileException;
import piandarduinoguy.raspberrypi.securitymsrv.exception.PersonDetectorException;
import piandarduinoguy.raspberrypi.securitymsrv.validation.ValidationUtil;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonDetectorService {
    @Value("${yolo.person-detector.base.location}")
    private String yoloPersonDetectorBaseLocation;

    @Value("${yolo.person-detector.threshold.confidence}")
    private String confidenceThreshold;

    @Value("${yolo.person-detector.threshold.non_maxima_suppression}")
    private String nonMaximaSuppressionThreshold;

    @Value("${resources.base.location}")
    private String resourcesBaseLocation;

    @Value("${new-capture.file-name}")
    private String newCaptureFileName;

    @Value("${new-capture.annotated.file-name}")
    private String newCaptureAnnotatedFileName;

    private ProcessBuilder personDetectorProcessBuilder;

    private Process personDetectorProcess;

    private final Logger LOGGER = LoggerFactory.getLogger(PersonDetectorService.class);

    @PostConstruct
    private void createPersonDetectorProcessBuilder() {
        final String imagePath = String.format("%s/%s.jpeg", resourcesBaseLocation, newCaptureFileName);
        final String yoloFilesBaseDirectory = String.format("%s/yolo-coco", yoloPersonDetectorBaseLocation);
        final String yoloScriptLocation = String.format("%s/yolo.py", yoloPersonDetectorBaseLocation);

        this.personDetectorProcessBuilder = new ProcessBuilder("python3", yoloScriptLocation, imagePath, yoloFilesBaseDirectory, confidenceThreshold, nonMaximaSuppressionThreshold, resourcesBaseLocation, newCaptureAnnotatedFileName);
        this.personDetectorProcessBuilder.redirectErrorStream(true);
    }


    public boolean hasPersonBeenDetected() {
        String pythonPersonDetectorProcessLogs = getPersonDetectorProcessLogs();
        return pythonPersonDetectorProcessLogs.contains("Person detected.");
    }

    public void runPersonDetectorProcess(byte[] imageBytes) {
        this.saveTemporaryImage(imageBytes);
        try {
            this.personDetectorProcess = this.personDetectorProcessBuilder.start();
        } catch (IOException e) {
            throw new PersonDetectorException(String.format("IOException occurred trying to run yolo.py script in directory %s.", yoloPersonDetectorBaseLocation));
        }
    }

    private void saveTemporaryImage(byte[] imageBytes) {
        File imageFile = new File(String.format("%s/%s.jpeg", resourcesBaseLocation, newCaptureFileName));
        try {
            FileUtils.writeByteArrayToFile(imageFile, imageBytes);
        } catch (IOException ioException) {
            throw new ImageFileException(String.format(
                    "The image %s could not be saved to the directory %s. An IOException was thrown with message \"%s\".",
                    imageFile.getName(),
                    resourcesBaseLocation,
                    ioException.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getPersonDetectorProcessLogs() {
        ValidationUtil.validateProcess(this.personDetectorProcess);
        List<String> results = readPersonDetectorProcessOutput();
        ValidationUtil.validateProcessLogs(results);
        StringBuilder pythonLogsStringBuilder = new StringBuilder("\n");
        for (String logMessage : results) {
            pythonLogsStringBuilder.append(logMessage);
            pythonLogsStringBuilder.append("\n");
        }
        LOGGER.info("Result from running python script: {}", pythonLogsStringBuilder.toString());
        return pythonLogsStringBuilder.toString();

    }

    private List<String> readPersonDetectorProcessOutput() {
        try (BufferedReader output = new BufferedReader(new InputStreamReader(this.personDetectorProcess.getInputStream()))) {
            return output.lines()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new PersonDetectorException("IOException occurred trying to read the yolo person detector python script output.");
        }
    }
}
