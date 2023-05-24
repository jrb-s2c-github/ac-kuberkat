package za.co.s2c.ac.cryppproxy.controller;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import za.co.s2c.ac.cryppproxy.feign.key_use.DecryptionRequestBean;

@Getter
@EqualsAndHashCode
@ToString
public class DecryptionRequest  {
    private String alg;

    private String cipher;
    private String mode;

    private String email;

    @ToString.Exclude
    private transient String password;

    private String accountId;

    private String keyName;

    @ToString.Exclude
    private String appId;

    @ToString.Exclude
    private String sessionId;

    public DecryptionRequestBean getDecryptionRequestBean(String cipher, String appCredential) {
        return DecryptionRequestBean.builder().alg(alg).mode(mode).cipher(cipher).appId(appId).appCredential(appCredential).build();
    }

}
