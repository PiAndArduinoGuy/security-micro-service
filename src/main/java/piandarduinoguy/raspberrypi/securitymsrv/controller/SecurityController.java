package piandarduinoguy.raspberrypi.securitymsrv.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;

public interface SecurityController {

    @PutMapping("update/security-config")
    default ResponseEntity<Void> updateSecurityConfig(@RequestBody SecurityConfig securityConfig) {
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
    default ResponseEntity<String> getAnnotatedImage() {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("silence-alarm")
    default ResponseEntity<Void> silenceAlarm() {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
