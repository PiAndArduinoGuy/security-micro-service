package piandarduinoguy.raspberrypi.securitymsrv.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ImageFileException extends RuntimeException {
    private HttpStatus httpStatus;

    public ImageFileException(String message, HttpStatus httpStatus){
        super(message);
        this.httpStatus = httpStatus;
    }
}
