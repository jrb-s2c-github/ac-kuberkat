package za.co.s2c.ac.cryppproxy.feign.keys;

import lombok.Value;
import lombok.experimental.SuperBuilder;
import za.co.s2c.ac.cryppproxy.feign.common.ApiCredentialBean;

import java.io.Serializable;

@SuperBuilder
@Value
public class KeyDescriptorBean extends ApiCredentialBean implements Serializable {
    String kid;

    String name;

    String transient_key;
}
