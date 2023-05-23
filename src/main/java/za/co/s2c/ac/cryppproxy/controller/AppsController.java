package za.co.s2c.ac.cryppproxy.controller;

import feign.Feign;
import feign.FeignException;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import za.co.s2c.ac.cryppproxy.feign.apps.AppCredentialBean;
import za.co.s2c.ac.cryppproxy.feign.apps.AppsClient;

@RestController
public class AppsController {

    private AppsClient appsClient;

    AppsController() {

        appsClient = Feign.builder().
                encoder(new GsonEncoder()).decoder(new GsonDecoder()).
                target(AppsClient.class, "https://eu.smartkey.io");
    }

    @GetMapping("/apps/{appId}/credential")
    public AppCredentialBean getCredential(@PathVariable String appId, @RequestParam String bearerToken) {
        AppCredentialBean result = null;
        try {
            result = appsClient.getCredential(appId, bearerToken);
        } catch (FeignException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), e.getMessage());
        } catch (FeignException.BadRequest e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getMessage());
        }


        return result;
    }
}
