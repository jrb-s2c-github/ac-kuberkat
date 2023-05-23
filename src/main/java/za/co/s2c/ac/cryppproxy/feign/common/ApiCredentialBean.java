package za.co.s2c.ac.cryppproxy.feign.common;

import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@Value
@NonFinal
public class ApiCredentialBean {
    @NonFinal
    private transient String appId;

    @NonFinal
    @ToString.Exclude
    private transient String appCredential;
}
