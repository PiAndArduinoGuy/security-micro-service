package piandarduinoguy.raspberrypi.securitymsrv.data.mapper;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import piandarduinoguy.raspberrypi.securitymsrv.exception.ImageFileException;

import java.io.IOException;

public class MultipartImageFileMapper {

    private MultipartImageFileMapper(){
        throw new IllegalStateException("MultipartImageFileMapper class is a utility(mapper) class not meant to be instantiated.");
    }

    public static byte[] multipartImageFileToByteArrayImage(MultipartFile multipartImageFile) {
        try {
            return multipartImageFile.getBytes();
        } catch (IOException ioException) {
            throw new ImageFileException(String.format("Could not convert multipart image file to byte array due to an IOException with message '%s'", ioException.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
