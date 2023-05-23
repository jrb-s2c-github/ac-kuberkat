package za.co.s2c.ac.cryppproxy.feign.key_use;

import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import za.co.s2c.ac.cryppproxy.feign.common.ApiCredentialBean;

@SuperBuilder
@Value
public class EncryptionRequestBean extends ApiCredentialBean {
    KeyBean key;

    String alg;

    @ToString.Exclude
    transient String plain;

    String mode;

    String iv;

    String ad;
}
