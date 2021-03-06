package piandarduinoguy.raspberrypi.securitymsrv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.Base64EncodedImageDto;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityStatus;
import piandarduinoguy.raspberrypi.securitymsrv.data.mapper.ImageMapper;
import piandarduinoguy.raspberrypi.securitymsrv.service.SecurityService;

@RestController
public class SecurityControllerImpl implements SecurityController {
    @Autowired
    private SecurityService securityService;

    public ResponseEntity<SecurityConfig> updateSecurityConfig(SecurityConfig securityConfig) {
        SecurityConfig updatedSecurityConfig = securityService.saveSecurityConfig(securityConfig);

        return new ResponseEntity<>(updatedSecurityConfig, HttpStatus.CREATED);
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
    public ResponseEntity<Base64EncodedImageDto> getAnnotatedImage() {
        Base64EncodedImageDto base64EncodedImageDto = new Base64EncodedImageDto();
        base64EncodedImageDto.setBase64EncodedImage(securityService.getBase64AnnotatedImage());
        return new ResponseEntity<>(base64EncodedImageDto, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SecurityConfig> silenceAlarm() {
        return new ResponseEntity<>(this.securityService.silenceAlarm(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SecurityConfig> armAlarm(){
        return new ResponseEntity<>(this.securityService.armAlarm(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SecurityConfig> disarmAlarm() {
        return new ResponseEntity<>(this.securityService.disarmAlarm(),HttpStatus.OK);
    }
}
