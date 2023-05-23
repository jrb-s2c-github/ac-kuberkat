package za.co.s2c.ac.cryppproxy.feign.session;

import lombok.ToString;
import lombok.Value;

@Value
public class SessionBean {
    public String token_type;

    public String expires_in;

    @ToString.Exclude
    transient String access_token;

    String entity_id;
}
