package piandarduinoguy.raspberrypi.securitymsrv.data.mapper;

import java.util.Base64;

public class ImageMapper {

    private ImageMapper() {
        throw new IllegalStateException("MultipartImageFileMapper class is a utility(mapper) class not meant to be instantiated.");
    }

    public static byte[] base64ToByteArray(String base64EncodedImage) {
        return Base64.getDecoder().decode(base64EncodedImage);
    }
}
