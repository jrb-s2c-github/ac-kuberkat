package za.co.s2c.ac.cryppproxy.controller;

import feign.Feign;
import feign.FeignException;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import za.co.s2c.ac.cryppproxy.feign.session.AccountBean;
import za.co.s2c.ac.cryppproxy.feign.session.AccountSelectionResultBean;
import za.co.s2c.ac.cryppproxy.feign.session.SessionBean;
import za.co.s2c.ac.cryppproxy.feign.session.SessionClient;

@RestController
public class SessionController {

    private SessionClient sessionClient;

    SessionController() {
        if (sessionClient == null) {
            sessionClient = Feign.builder().
                    encoder(new GsonEncoder()).decoder(new GsonDecoder()).
                    target(SessionClient.class, "https://eu.smartkey.io");
        }
    }

    @PostMapping("/session/auth")
    public SessionBean getSessionId(@RequestParam String email, @RequestParam String password) {
        SessionBean result = null;
        try {
            // do not reuse session client here as email and pwd can be different every time
            SessionClient sessionClient = Feign.builder().requestInterceptor(new BasicAuthRequestInterceptor(email, password)).
                    encoder(new GsonEncoder()).decoder(new GsonDecoder()).
                    target(SessionClient.class, "https://eu.smartkey.io");

            result = sessionClient.getSession();
        } catch (FeignException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), e.getMessage());
        } catch (FeignException.BadRequest e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getMessage());
        }
        System.out.println(result.toString());

        return result;
    }

    @PostMapping("session/select_account")
    public AccountSelectionResultBean selectionAccount(@RequestBody AccountBean account) {

        AccountSelectionResultBean result = null;
        try {
            sessionClient = Feign.builder().
                    encoder(new GsonEncoder()).decoder(new GsonDecoder()).
                    target(SessionClient.class, "https://eu.smartkey.io");

            result = sessionClient.selectAccount(account, account.getBearerToken());
        } catch (FeignException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), e.getMessage());
        } catch (FeignException.BadRequest e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getMessage());
        }
        System.out.println(result.toString());

        return result;
    }
}
