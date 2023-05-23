package za.co.s2c.ac.cryppproxy.feign.keys;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface KeysClient {
    @RequestLine("POST crypto/v1/keys/info")
    @Headers("Authorization: Basic {apiKey}")
    KeyDescriptorBean info(@Param String apiKey, KeyDescriptorBean keyDescriptorBean);

}
