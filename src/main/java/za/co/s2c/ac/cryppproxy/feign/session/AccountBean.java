package za.co.s2c.ac.cryppproxy.feign.session;

import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
public class AccountBean {
    String acct_id;

    @ToString.Exclude
    transient String bearerToken;
}
