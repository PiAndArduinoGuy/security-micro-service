package piandarduinoguy.raspberrypi.securitymsrv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityStatus;
import piandarduinoguy.raspberrypi.securitymsrv.exception.ImageFileException;
import piandarduinoguy.raspberrypi.securitymsrv.exception.SecurityConfigFileException;
import piandarduinoguy.raspberrypi.securitymsrv.publisher.SecurityConfigPublisher;
import piandarduinoguy.raspberrypi.securitymsrv.validation.ValidationUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;

@Service
public class SecurityService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityService.class);
    private ObjectMapper objectMapper;
    private PersonDetectorService personDetectorService;
    private SecurityConfigPublisher securityConfigPublisher;

    @Value("${resources.base.location}")
    private String resourcesBaseLocation;

    @Value("${new-capture.annotated.file-name}")
    private String newCaptureAnnotatedFileName;

    @Autowired
    public SecurityService(ObjectMapper objectMapper,
                           PersonDetectorService personDetectorService,
                           SecurityConfigPublisher securityConfigPublisher) {
        this.objectMapper = objectMapper;
        this.personDetectorService = personDetectorService;
        this.securityConfigPublisher = securityConfigPublisher;
    }

    public SecurityConfig getSecurityConfig() {
        try {
            return objectMapper.readValue(new File(resourcesBaseLocation + "security_config.json"), SecurityConfig.class);
        } catch (FileNotFoundException fileNotFoundException) {
            throw new SecurityConfigFileException(String.format(
                    "The SecurityConfig file was not found. FileNotFoundException with message \"%s\" was thrown.",
                    fileNotFoundException.getMessage()));
        } catch (IOException ioException) {
            throw new SecurityConfigFileException(String.format(
                    "Could not retrieve security config due to an IOException with message \"%s\".",
                    ioException.getMessage()));
        }
    }

    public SecurityConfig saveSecurityConfig(SecurityConfig securityConfig) {
        try {
            objectMapper.writeValue(new File(resourcesBaseLocation + "security_config.json"), securityConfig);
            this.securityConfigPublisher.publishSecurityConfig(securityConfig);
            return this.getSecurityConfig();
        } catch (IOException ioException) {
            throw new SecurityConfigFileException(String.format(
                    "Could not save the security config file object %s to %s due to an IOException with message \"%s\".",
                    securityConfig,
                    resourcesBaseLocation + "security_config.json",
                    ioException.getMessage()));
        }
    }

    public String getBase64AnnotatedImage() {
        File annotatedImageFile = new File(String.format("%s/%s.jpeg", resourcesBaseLocation, newCaptureAnnotatedFileName));
        ValidationUtil.validateImageFile(annotatedImageFile);
        try {
            return Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(annotatedImageFile));
        } catch (IOException ioException) {
            throw new ImageFileException(String.format(
                    "The image %s could not be encoded to base64 string due to an IOException being thrown with message \"%s\".",
                    annotatedImageFile.getName(),
                    ioException.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean detectPerson(byte[] imageBytes) {
        this.personDetectorService.runPersonDetectorProcess(imageBytes);
        return this.personDetectorService.hasPersonBeenDetected();
    }

    public SecurityConfig silenceAlarm() {
        ValidationUtil.validateSecurityCanBeSilenced(this.getSecurityConfig());
        SecurityConfig securityConfig = new SecurityConfig(SecurityStatus.SAFE, SecurityState.DISARMED);
        return this.saveSecurityConfig(securityConfig);
    }

    public SecurityConfig armAlarm() {
        ValidationUtil.validateSecurityCanBeArmed(this.getSecurityConfig());
        SecurityConfig updatedSecurityConfig = this.getSecurityConfig();
        updatedSecurityConfig.setSecurityState(SecurityState.ARMED);
        return this.saveSecurityConfig(updatedSecurityConfig);
    }
}
