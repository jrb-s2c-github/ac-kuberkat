package za.co.s2c.ac.cryppproxy.feign.session;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface SessionClient {
    @RequestLine("POST /sys/v1/session/auth")
    SessionBean getSession();

    @RequestLine("POST /sys/v1/session/select_account")
    @Headers("Authorization: Bearer {bearerToken}")
    AccountSelectionResultBean selectAccount(AccountBean account, @Param String bearerToken);
}
