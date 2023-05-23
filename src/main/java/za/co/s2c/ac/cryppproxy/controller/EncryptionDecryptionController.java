package za.co.s2c.ac.cryppproxy.controller;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import za.co.s2c.ac.cryppproxy.feign.apps.AppCredentialBean;
import za.co.s2c.ac.cryppproxy.feign.key_use.CipherTextBean;
import za.co.s2c.ac.cryppproxy.feign.key_use.ClearTextBean;
import za.co.s2c.ac.cryppproxy.feign.key_use.DecryptionRequestBean;
import za.co.s2c.ac.cryppproxy.feign.key_use.EncryptionRequestBean;
import za.co.s2c.ac.cryppproxy.feign.keys.KeyDescriptorBean;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

@RestController
@Slf4j
public class EncryptionDecryptionController extends ApiKeyAuthBase {
    private static final String EMAIL_KEY = "email_token_key";

    private static final String ID_NR_KEY = "id_token_key";

    Random randomGenerator = new Random(new Date().getTime());

    HazelcastInstance hzInstance;

    // key name 2 key ID
    Map<String, KeyDescriptorBean> keyCache;

    // TODO configure eviction of cache
    Map<String, String> password2PseudoAppSecret;

    // key name 2 pattern
    Map<String, String> patterns;

    @Autowired
    KeyUseController keyUseController;

    @Autowired
    KeysController keysController;

    public EncryptionDecryptionController() {
        hzInstance = Hazelcast.newHazelcastInstance();
        keyCache = hzInstance.getMap("keyCache");

        password2PseudoAppSecret = hzInstance.getMap("session2PseudoAppCredential");
    }

    @PostMapping("crypto/encrypt")
    public EncryptionResponse encrypt(@RequestBody EncryptionRequest request) {
        log.info("Encryption requested: " + request);

        String sessionId = password2PseudoAppSecret.get(request.getPassword()) != null ? request.getSessionId() : generateSessionKey();
        StringBuffer processedMessage = new StringBuffer();

        try {
            String appSecret = determineAppSecret(sessionId, request.getEmail(), request.getPassword(), request.getAccountId(), request.getAppId());

            StringTokenizer st = new StringTokenizer(request.getPlain());
            while (st.hasMoreElements()) {
                String toCheck = st.nextToken();
                String lastChars = "";
                char lastChar = toCheck.charAt(toCheck.length() - 1);
                if (lastChar == '.' || lastChar == ',' || lastChar == ';' || lastChar == ':') {
                    toCheck = toCheck.substring(0, toCheck.length() - 1);
                    lastChars = String.valueOf(lastChar);
                }

                if (EmailValidator.getInstance().isValid(toCheck)) {
                    processedMessage.append(encryptToken(request, appSecret, EMAIL_KEY, toCheck)).append(lastChars);
                } else if (isIDNumber(toCheck)) {
                    processedMessage.append(encryptToken(request, appSecret, ID_NR_KEY, toCheck)).append(lastChars);
                } else {
                    processedMessage.append(toCheck);
                }
                processedMessage.append(" ");
            }

            // lastly only cache once sure that all worked out
            password2PseudoAppSecret.put(request.getPassword(), doOneTimePad(appSecret, sessionId));
        } catch (Throwable t) {
            if (request.getPassword() == null) {
                // nothing to be done as full process did run without taking data from the cache
                throw t;
            }

            // do it again in full in case cache was stale
            password2PseudoAppSecret.remove(request.getPassword());
            keyCache.clear();
            return encrypt(request);
        }
        EncryptionResponse response = EncryptionResponse.builder().sessionId(sessionId).cipherText(processedMessage.toString()).build();
        log.info("Encryption finished: " + response);
        return response;
    }

    private String determineAppSecret(String sessionId, String email, String password, String accountId, String appId) {
        String appSecret = password2PseudoAppSecret.get(password);
        if (appSecret != null) {
            appSecret = decodePseudoAppCred(appSecret, sessionId);
        } else {
            AppCredentialBean appCredential = getAppApiKey(email, password, accountId, appId);
            appSecret = appCredential.getCredential().getSecret();
        }

        return appSecret;
    }

    private String decryptToken(DecryptionRequest request, String appSecret, String keyName, String cipherText) {
        KeyDescriptorBean keyDescriptor = keyCache.get(keyName);
        boolean keyCached = keyDescriptor != null;
        if (!keyCached) {
            keyDescriptor = getKey(request.getAppId(), keyName, appSecret);
            keyCache.put(keyName, keyDescriptor);
        }

        ClearTextBean clearText = null;

        DecryptionRequestBean decryptionRequest = request.getDecryptionRequestBean(base64Encode(cipherText), appSecret);
//        decryptionRequest.appCredential = appSecret;
        clearText = keyUseController.decrypt(keyDescriptor.getKid(), decryptionRequest);

        keyCache.put(keyName, keyDescriptor);
        return base64Decode(clearText.getPlain());

    }

    private String encryptToken(EncryptionRequest request, String appSecret, String keyName, String plainText) {
        KeyDescriptorBean keyDescriptor = keyCache.get(keyName);
        boolean keyCached = keyDescriptor != null;
        if (!keyCached) {
            keyDescriptor = getKey(request.getAppId(), keyName, appSecret);
            keyCache.put(keyName, keyDescriptor);
        }

        EncryptionRequestBean encryptionRequest = request.getEncryptionRequestBean(base64Encode(plainText), appSecret);
//        encryptionRequest.appCredential = appSecret;
        CipherTextBean cipherText = null;
        cipherText = keyUseController.encrypt(keyDescriptor.getKid(), encryptionRequest);

        return base64Decode(cipherText.getCipher());
    }

    private boolean isBasicEmail(String word) {

        String regexPattern = "^(.+)@(\\S+)$";
        return Pattern.compile(regexPattern)
                .matcher(word)
                .matches();
    }

    private boolean isIDNumber(String word) {
        boolean isNumeric = word.chars().allMatch(x -> Character.isDigit(x));
        return isNumeric && word.length() == 13;
    }

    @PostMapping("crypto/decrypt")
    public DecryptionResponse decrypt(@RequestBody DecryptionRequest request) {
        log.info("Decryption requested: " + request);

        String sessionId = password2PseudoAppSecret.get(request.getPassword()) != null ? request.getSessionId() : generateSessionKey();
        StringBuffer processedMessage = new StringBuffer();

        try {
            String appSecret = determineAppSecret(request.getSessionId(), request.getEmail(), request.getPassword(), request.getAccountId(), request.getAppId());

            StringTokenizer st = new StringTokenizer(request.getCipher());
            while (st.hasMoreElements()) {
                String toCheck = st.nextToken();
                String lastChars = "";
                char lastChar = toCheck.charAt(toCheck.length() - 1);
                if (lastChar == '.' || lastChar == ',' || lastChar == ';' || lastChar == ':') {
                    toCheck = toCheck.substring(0, toCheck.length() - 1);
                    lastChars = String.valueOf(lastChar);
                }

                if (isBasicEmail(toCheck)) {
                    processedMessage.append(decryptToken(request, appSecret, EMAIL_KEY, toCheck)).append(lastChars);
                } else if (isIDNumber(toCheck)) {
                    processedMessage.append(decryptToken(request, appSecret, ID_NR_KEY, toCheck)).append(lastChars);
                } else {
                    processedMessage.append(toCheck);
                }
                processedMessage.append(" ");
            }
            // lastly only cache once sure that all worked out
            password2PseudoAppSecret.put(request.getPassword(), doOneTimePad(appSecret, sessionId));
        } catch (Throwable t) {

            if (request.getPassword() == null) {
                // nothing to be done as full process did run without taking data from the cache
                throw t;
            }

            // do it again in full in case cache is stale
            password2PseudoAppSecret.remove(request.getPassword());
            keyCache.clear();
            return decrypt(request);
        }

        DecryptionResponse response = DecryptionResponse.builder().sessionId(sessionId).plain(processedMessage.toString()).build();
        log.info("Decryption finished: " + response);
        return response;
    }


    private KeyDescriptorBean getKey(String appId, String keyName, String credential) {
        KeyDescriptorBean keyDescriptorBean = KeyDescriptorBean.builder()
                .name(keyName).appId(appId).appCredential(credential).build();
        KeyDescriptorBean keyDescriptor = keysController.info(keyDescriptorBean);
        return keyDescriptor;
    }

    private String generateSessionKey() {
//        return String.valueOf(randomGenerator.nextLong());
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 100;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        log.info("Session ID generated: " + generatedString);
        return generatedString;
    }

    private String doOneTimePad(String appSecret, String password) {
        byte[] out = xor(appSecret.getBytes(), password.getBytes());

        return new String(encoder.encode(out)).replaceAll("\\s", "");
    }

    private String decodePseudoAppCred(String appSecret, String key) {
        byte[] out = xor(decoder.decode(appSecret), key.getBytes());

        return new String(out);
    }

    private static byte[] xor(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i % key.length]);
        }
        return out;
    }


}
