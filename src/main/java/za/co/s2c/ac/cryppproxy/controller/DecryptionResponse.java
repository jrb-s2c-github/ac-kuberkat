package za.co.s2c.ac.cryppproxy.controller;

import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import za.co.s2c.ac.cryppproxy.feign.common.ApiCredentialBean;

@Value
@SuperBuilder
public class DecryptionResponse extends ApiCredentialBean {

    @ToString.Exclude
    String plain;

    @ToString.Exclude
    String sessionId;

}
