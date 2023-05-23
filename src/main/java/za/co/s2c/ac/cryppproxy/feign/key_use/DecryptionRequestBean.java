package za.co.s2c.ac.cryppproxy.feign.key_use;

import lombok.Value;
import lombok.experimental.SuperBuilder;
import za.co.s2c.ac.cryppproxy.feign.common.ApiCredentialBean;

@SuperBuilder
@Value
public class DecryptionRequestBean extends ApiCredentialBean {
    String alg;

    String cipher;

    String mode;

    String iv;

    String ad;

    String tag;

    String masked;

}
