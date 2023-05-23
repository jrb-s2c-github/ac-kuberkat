package za.co.s2c.ac.cryppproxy.feign.apps;

import lombok.ToString;
import lombok.Value;

@Value
public class AppCredentialBean {
    public String app_id;

    @ToString.Exclude
    transient CredentialBean credential;
}
