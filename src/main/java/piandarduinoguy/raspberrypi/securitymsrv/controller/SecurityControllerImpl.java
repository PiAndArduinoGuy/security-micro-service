package piandarduinoguy.raspberrypi.securitymsrv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityStatus;
import piandarduinoguy.raspberrypi.securitymsrv.data.mapper.ImageMapper;
import piandarduinoguy.raspberrypi.securitymsrv.service.SecurityService;

@RestController
public class SecurityControllerImpl implements SecurityController {
    @Autowired
    private SecurityService securityService;

    public ResponseEntity<Void> updateSecurityConfig(SecurityConfig securityConfig) {
        securityService.saveSecurityConfig(securityConfig);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<SecurityConfig> getSecurityConfig() {
        return new ResponseEntity<>(securityService.getSecurityConfig(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> performSecurityCheck(String base64EncodedImage) {
        byte[] imageByteArray = ImageMapper.base64ToByteArray(base64EncodedImage);
        if (securityService.detectPerson(imageByteArray) && isSecuritySystemArmed()) {
            SecurityConfig securityConfig = securityService.getSecurityConfig();
            securityConfig.setSecurityStatus(SecurityStatus.BREACHED);
            securityService.saveSecurityConfig(securityConfig);
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    private boolean isSecuritySystemArmed() {
        return securityService.getSecurityConfig().getSecurityState().equals(SecurityState.ARMED);
    }

    @Override
    public ResponseEntity<String> getAnnotatedImage() {
        return new ResponseEntity(securityService.getBase64AnnotatedImage(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> silenceAlarm() {
        this.securityService.silenceAlarm();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
