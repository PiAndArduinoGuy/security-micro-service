package piandarduinoguy.raspberrypi.securitymsrv.publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;

@EnableBinding(Source.class)
@Service
public class SecurityConfigPublisher {
    @Autowired
    private Source source;
    public void publishSecurityConfig(SecurityConfig securityConfig) {
        source.output().send(MessageBuilder.withPayload(securityConfig).build());
    }
}
