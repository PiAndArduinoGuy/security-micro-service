package piandarduinoguy.raspberrypi.securitymsrv.data.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MultipartImageFileMapperUnitTest {

    @Autowired
    private TestUtils testUtils;

    @Test
    @DisplayName("Given a multipartfile " +
            "when multipartImageFileToByteArrayImage method called " +
            "then the multipartfile image is converted to expected byte array image")
    void canMapMultipartFileImageToByteArrayImage() throws Exception{
        MultipartFile multipartImageFile = TestUtils.createMockMultipartImageFile();

        byte[] byteImage = MultipartImageFileMapper.multipartImageFileToByteArrayImage(multipartImageFile);

        assertNotNull(byteImage);
        assertEquals(multipartImageFile.getBytes(), byteImage);
    }
}
