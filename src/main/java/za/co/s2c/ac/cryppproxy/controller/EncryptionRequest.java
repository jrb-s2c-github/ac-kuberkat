package za.co.s2c.ac.cryppproxy.controller;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import za.co.s2c.ac.cryppproxy.feign.key_use.EncryptionRequestBean;

@EqualsAndHashCode
@ToString
@SuperBuilder
@Getter
@NoArgsConstructor
public class EncryptionRequest  {


    private String alg;

    @ToString.Exclude
    private transient String plain;
    private String mode;

    private String email;

    @ToString.Exclude
    private transient String password;

    private String accountId;

    @ToString.Exclude
    private String appId;

    @ToString.Exclude
    private String sessionId;

    public EncryptionRequestBean getEncryptionRequestBean(String plainText, String appCredential) {
        return EncryptionRequestBean.builder().alg(alg).mode(mode).plain(plainText).appId(appId).appCredential(appCredential).build();
    }
}
