package za.co.s2c.ac.cryppproxy.controller;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@EqualsAndHashCode
public class CryptoMaterial {
    @ToString.Exclude
    String keyId;

    @ToString.Exclude
    String apiKey;
}
