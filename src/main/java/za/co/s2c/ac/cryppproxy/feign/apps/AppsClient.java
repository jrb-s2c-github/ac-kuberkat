package za.co.s2c.ac.cryppproxy.feign.apps;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers("Authorization: Bearer {bearerToken}")
public interface AppsClient {
    @RequestLine("GET /sys/v1/apps/{appId}/credential")
    AppCredentialBean getCredential(@Param String appId, @Param String bearerToken);
}
