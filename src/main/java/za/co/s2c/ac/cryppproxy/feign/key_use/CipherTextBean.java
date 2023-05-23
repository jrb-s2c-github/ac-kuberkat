package za.co.s2c.ac.cryppproxy.feign.key_use;

import lombok.Value;

@Value
public class CipherTextBean {
    String kid;

    String cipher;

    String iv;

    String tag;

}
