package piandarduinoguy.raspberrypi.securitymsrv.publisher;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piandarduinoguy.raspberrypi.securitymsrv.data.domain.SecurityConfig;

@Service
public class SecurityConfigPublisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private FanoutExchange fanoutExchange;

    @Autowired
    private Binding binding;

    public void publishSecurityConfig(SecurityConfig securityConfig){
        rabbitTemplate.convertAndSend(fanoutExchange.getName(),binding.getRoutingKey(),securityConfig);
    }
}
