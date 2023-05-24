package za.co.s2c.ac.cryppproxy.controller;

import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import za.co.s2c.ac.cryppproxy.feign.common.ApiCredentialBean;

@Value
@SuperBuilder
public class EncryptionResponse extends ApiCredentialBean {
    public String cipherText;

    @ToString.Exclude
    public String sessionId;
}
