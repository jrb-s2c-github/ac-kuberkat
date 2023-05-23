package za.co.s2c.ac.cryppproxy.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import za.co.s2c.ac.cryppproxy.feign.apps.AppCredentialBean;
import za.co.s2c.ac.cryppproxy.feign.common.ApiCredentialBean;
import za.co.s2c.ac.cryppproxy.feign.session.AccountBean;
import za.co.s2c.ac.cryppproxy.feign.session.AccountSelectionResultBean;
import za.co.s2c.ac.cryppproxy.feign.session.SessionBean;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class ApiKeyAuthBase {

    Base64.Encoder encoder = Base64.getEncoder();

    Base64.Decoder decoder = Base64.getDecoder();

    @Autowired
    SessionController sessionController;

    @Autowired
    AppsController appsController;

    protected AppCredentialBean getAppApiKey(String email, String password, String accountId, String appId) {
        SessionBean session = getSessionBean(email, password);
        log.trace("Selecting account: " + accountId);
        AccountBean account = AccountBean.builder().acct_id(accountId).bearerToken(session.getAccess_token()).build();
        AccountSelectionResultBean accountSelectionResult = sessionController.selectionAccount(account);
        log.trace("Account selected: " + accountSelectionResult);

        // get API credentials
        log.trace("Retrieving application password for " + appId);
        AppCredentialBean credential = appsController.getCredential(appId, session.getAccess_token());
        log.trace("Application password retrieved: " + credential);

        return credential;
    }

    SessionBean getSessionBean(String email, String password) {
        // get bearer token
        log.trace("Getting bearer token for " + email);
        SessionBean session = sessionController.getSessionId(email, password);
        log.trace("Bearer token retrieved: " + session);
        return session;
    }

    protected String getAppApiKey(ApiCredentialBean apiCredentialBean) {
        String appId = apiCredentialBean.getAppId();
        String appCredential = apiCredentialBean.getAppCredential();
        return getAppApiKey(appId, appCredential);
    }

    protected String getAppApiKey(String appId, String appCredential) {
        String apiKey =  appId + ":" + appCredential;
        apiKey = base64Encode(apiKey);
        return apiKey;
    }

     String base64Encode(String toEncode) {
        return new String(encoder.encode(toEncode.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    String base64Decode(String toEncode) {
        return new String(decoder.decode(toEncode.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
}
