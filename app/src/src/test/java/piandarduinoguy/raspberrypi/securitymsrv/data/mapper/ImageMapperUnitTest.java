package piandarduinoguy.raspberrypi.securitymsrv.data.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;

import java.io.File;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ImageMapperUnitTest {

    @Autowired
    private TestUtils testUtils;

    @Test
    @DisplayName("Given a base64 encoded image " +
            "when base64ToByteArray method called " +
            "then the image is converted to expected byte array image")
    void canMapMultipartFileImageToByteArrayImage() throws Exception{
        String base64EncodedImage = testUtils.createBase64EncodedImageFromImageFile(new File("src/test/resources/test_new_capture_person.jpeg"));

        byte[] byteImage = ImageMapper.base64ToByteArray(base64EncodedImage);

        assertNotNull(byteImage);
        assertArrayEquals(Base64.getDecoder().decode(base64EncodedImage), byteImage);
    }
}
