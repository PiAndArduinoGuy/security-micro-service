package piandarduinoguy.raspberrypi.securitymsrv.data.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Problem {
    private String title;
    private Integer status;
    private String detail;
}