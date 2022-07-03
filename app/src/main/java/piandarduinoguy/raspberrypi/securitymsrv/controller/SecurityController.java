package piandarduinoguy.raspberrypi.securitymsrv.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.Base64EncodedImageDto;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;

@CrossOrigin
public interface SecurityController {

    @PutMapping("update/security-config")
    default ResponseEntity<SecurityConfig> updateSecurityConfig(@RequestBody SecurityConfig securityConfig) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping(value = "security-check")
    default ResponseEntity<Void> performSecurityCheck(@RequestBody String base64EncodedImage) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("security-config")
    default ResponseEntity<SecurityConfig> getSecurityConfig() {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("annotated-image")
    default ResponseEntity<Base64EncodedImageDto> getAnnotatedImage() {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping("silence-alarm")
    default ResponseEntity<SecurityConfig> silenceAlarm() {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping("arm-alarm")
    default ResponseEntity<SecurityConfig> armAlarm(){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping("disarm-alarm")
    default ResponseEntity<SecurityConfig> disarmAlarm(){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
