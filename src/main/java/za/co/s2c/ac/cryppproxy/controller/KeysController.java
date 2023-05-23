package za.co.s2c.ac.cryppproxy.controller;

import feign.Feign;
import feign.FeignException;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import za.co.s2c.ac.cryppproxy.feign.keys.KeyDescriptorBean;
import za.co.s2c.ac.cryppproxy.feign.keys.KeysClient;

@RestController
@Slf4j
public class KeysController extends ApiKeyAuthBase {

    private KeysClient keysClient;

    KeysController() {
        keysClient = Feign.builder().
                encoder(new GsonEncoder()).decoder(new GsonDecoder()).
                target(KeysClient.class, "https://eu.smartkey.io");
    }

    @PostMapping("keys/info")
    public KeyDescriptorBean info(@RequestBody KeyDescriptorBean keyDescriptorBean) {
        KeyDescriptorBean result = null;
        log.trace("Getting key description: " + keyDescriptorBean);
        try {
            String apiKey = getAppApiKey(keyDescriptorBean);
            result = keysClient.info(apiKey, keyDescriptorBean);
        } catch (FeignException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), e.getMessage());
        } catch (FeignException.BadRequest e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getMessage());
        }

        log.trace("Key description determined: " + result);
        return result;
    }
}
