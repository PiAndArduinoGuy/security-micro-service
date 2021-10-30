package piandarduinoguy.raspberrypi.securitymsrv.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.test.annotation.DirtiesContext;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityStatus;
import piandarduinoguy.raspberrypi.securitymsrv.publisher.SecurityConfigPublisher;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

@SpringBootTest
class SecurityConfigPublisherUnitTest {
    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityConfigPublisher securityConfigPublisher;

    @Test
    @DirtiesContext
    void canSendPumpConfig() {
        SecurityConfig securityConfig = new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED);

        securityConfigPublisher.publishSecurityConfig(securityConfig);

        testUtils.assertExpectedSecurityConfigPublishedOnOutputChannel(securityConfig);
    }
}
