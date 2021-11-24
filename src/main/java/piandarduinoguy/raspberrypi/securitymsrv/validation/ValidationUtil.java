package piandarduinoguy.raspberrypi.securitymsrv.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityStatus;
import piandarduinoguy.raspberrypi.securitymsrv.exception.ImageFileException;
import piandarduinoguy.raspberrypi.securitymsrv.exception.PersonDetectorException;
import piandarduinoguy.raspberrypi.securitymsrv.exception.SecurityConfigStateException;

import java.io.File;
import java.util.List;

public class ValidationUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtil.class);

    private ValidationUtil(){
        throw new IllegalStateException("ValidateUtil class is a utility class not meant to be instantiated.");
    }

    public static void validateImageFile(File imageFile) {
        if (!imageFile.exists()) {
            throw new ImageFileException(String.format("The File %s does not exist.", imageFile.getPath()), HttpStatus.NOT_FOUND);
        }
    }

    public static void validateProcess(Process process) {
        if (process == null) {
            String exceptionMessage = "The process is null.";
            PersonDetectorException personDetectorException = new PersonDetectorException(exceptionMessage);
            LOGGER.warn(exceptionMessage, personDetectorException);
            throw personDetectorException;
        }
    }

    public static void validateProcessLogs(List<String> processLogs) {
        if (processLogs == null){
            throw new PersonDetectorException("processLogs are null.");
        } else {
            if (!processLogs.contains("Person detected.") &&
                    !processLogs.contains("Person not detected.")) {
                throw new PersonDetectorException("The process logs are not listed as valid logs, valid process logs are - 'Person detected.', 'Person not detected.'");
            }
        }
    }

    public static void validateSecurityConfigInArmableState(SecurityConfig securityConfig) {
        if (securityConfig.getSecurityState().equals(SecurityState.ARMED)) {
            logErrorMessageAndThrowSecurityConfigStateException("Security can not be armed with it in a state of ARMED already.");
        } else if (securityConfig.getSecurityStatus().equals(SecurityStatus.BREACHED)) {
            logErrorMessageAndThrowSecurityConfigStateException("Security can not be armed with security status BREACHED.");
        }
    }

    public static void validateSecurityCanBeSilenced(SecurityConfig securityConfig) {
        if (securityConfig.getSecurityState().equals(SecurityState.DISARMED)){
            logErrorMessageAndThrowSecurityConfigStateException("Security cannot be silenced with it in a DISARMED state.");
        } else if (securityConfig.getSecurityStatus().equals(SecurityStatus.SAFE)){
            logErrorMessageAndThrowSecurityConfigStateException("Security cannot be silenced with it in a SAFE status.");
        }
    }

    private static void logErrorMessageAndThrowSecurityConfigStateException(String s) {
        String message = s;
        LOGGER.error(message);
        throw new SecurityConfigStateException(message);
    }
}
