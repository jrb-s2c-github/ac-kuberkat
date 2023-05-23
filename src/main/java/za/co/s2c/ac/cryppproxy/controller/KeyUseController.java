package za.co.s2c.ac.cryppproxy.controller;

import feign.Feign;
import feign.FeignException;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import za.co.s2c.ac.cryppproxy.feign.key_use.*;

@RestController
@Slf4j
public class KeyUseController extends ApiKeyAuthBase {

    private KeyUseClient keyUseClient;

    KeyUseController() {
        keyUseClient = Feign.builder().
                encoder(new GsonEncoder()).decoder(new GsonDecoder()).
                target(KeyUseClient.class, "https://eu.smartkey.io");
    }

    @PostMapping("keys/{keyId}/encrypt")
    public CipherTextBean encrypt(@PathVariable String keyId, @RequestBody EncryptionRequestBean encryptionRequest) {
        CipherTextBean result = null;
        log.trace("Encrypting using key " + keyId + " with request " + encryptionRequest);
        try {
            String apiKey = getAppApiKey(encryptionRequest);
            result = keyUseClient.encrypt(keyId, apiKey, encryptionRequest);
            log.trace("Encrypted: " + result);
        } catch (FeignException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), e.getMessage());
        } catch (FeignException.BadRequest e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getMessage());
        }

        return result;
    }

    @PostMapping("keys/{keyId}/decrypt")
    public ClearTextBean decrypt(@PathVariable String keyId, @RequestBody DecryptionRequestBean decryptionRequest) {
        ClearTextBean result = null;
        log.trace("Decrypting using key " + keyId + " with request " + decryptionRequest);
        try {
            KeyUseClient keyUseClient = Feign.builder().
                    encoder(new GsonEncoder()).decoder(new GsonDecoder()).
                    target(KeyUseClient.class, "https://eu.smartkey.io");

            String apiKey = getAppApiKey(decryptionRequest);
            result = keyUseClient.decrypt(keyId, apiKey, decryptionRequest);
            log.trace("Decrypted: " + result);
        } catch (FeignException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), e.getMessage());
        } catch (FeignException.BadRequest e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getMessage());
        }

        return result;
    }

}
