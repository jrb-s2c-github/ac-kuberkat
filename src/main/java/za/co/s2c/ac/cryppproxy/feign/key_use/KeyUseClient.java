package za.co.s2c.ac.cryppproxy.feign.key_use;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface KeyUseClient {
    @RequestLine("POST crypto/v1/keys/{keyId}/encrypt")
    @Headers("Authorization: Basic {apiKey}")
    CipherTextBean encrypt(@Param String keyId, @Param String apiKey, EncryptionRequestBean encryptionRequest);

    @RequestLine("POST crypto/v1/keys/{keyId}/decrypt")
    @Headers("Authorization: Basic {apiKey}")
    ClearTextBean decrypt(@Param String keyId, @Param String apiKey, DecryptionRequestBean decryptionRequest);
}
