package piandarduinoguy.raspberrypi.securitymsrv.data.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SecurityConfig {
    private SecurityStatus securityStatus;
    private SecurityState securityState;
}
