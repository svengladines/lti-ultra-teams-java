package be.occam.lti.ultra.teams.domain.service;

import be.occam.lti.ultra.teams.config.SystemProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class JWKSetService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final SystemProperties systemProperties;

    public JWKSetService(SystemProperties systemProperties) {
        this.systemProperties = systemProperties;
    }

    public JWKSet get() {
        try {
            /*
            String pk = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDd1gtL3dlZ1IhsZOKCEx3EUezMZENqtph0amTRPsj60cIU8YIteZ908VE/qOrGRBLqkDDOEiUi3CNbz8qwNGfMZ6eJIuTSPn06p/e0vRM6xbQ2U41FZAiyFs+d2G4Y33q9F90eo0oy4Fc6elSVuhvUeIBhsojBp4yC3ZA0YhMrE5As8VZ0kFB+KUoCAQAqqDv7CFDap2SKSP1JL948SIC2cEVfvaLIYdTtrTpZQYmppqf/lJMEip5aEmUPVfwTn4//cf+ZWPl0DuZknWvUQoKxwRHjB3EYyEj9yUYM3LH0OYJ75w4vUkE1ZyUY7fkQh3B5xHV9VnsXu9OlPftPAvw/AgMBAAECggEAHtI54JESvT3ftHux0tO4G4osZdt6WG3FMcuEdWQNRXf2SLWjWhEfW/6ZRMiB0gksPaXJ+h+gRD8ktIFizmeBkm9GYjHUa/edq5QhwN1SOiS03KVwJ9d1s9SihPJInbETGwmhQbv7N6VeyTOs496nNjaF080b3hga2CXTfsuvkRjU7GflGfQs2RzATdQZyWYxtqWtEVQ5xe2LcfNATzxjJvqvWZ6JhlGchF+rsyAouSR6Ydhok27Kp0cp80H/2k1B1MbfZDf00fAVTqHzszBLvOeHmegYR6vQNcZDRcjEnng+31tJeiBXX6pOfEKnZQSCr21TMwmgRSwAaLAHbWejwQKBgQD7DDZ6/BZURVpGh7yAGCUNcpSmHdTJUqKpvmsJlqkyt99+xnFof+7BnX+6g/R/NonhHuAn8/5CJdOUnfXjCBha59o++ugMH7jEvdkD8KcZy4rg6pC59wgAQgECTXTySjFDY3tl30HiXIf9xxfpVZJSX6faI4OFwtlXnWDo7t3y/wKBgQDiNlAs2naPkFsHLCcv8eZTYcwGpR0UapIkt4Ww/2bPm5Pfis7GaV5C0E5V4KKc2XHk2mm0hOti0rvrREQ4BOdtQ8UDPAsYCfYVirq5lkNevoGGYqXqPLmGnKVUoRw/uIPi1WPjbTz9Ijt+H1oc11BjBwHk5zuURUmQvv3WIJM2wQKBgBqsMoGglPLBJ5VEyKdHaXBjUx3PH5OAPx4PyCmNo05rMMi0Zso2hXR2umJjsK9vaPjNIf28s6/teuxYWA6WBphp98snN05KdIQas5ryj55y/L9mzJelgmkcmiTXe+xeK5vATxUEwmg9colpBMZavCAaX6gSi9/DUvo3E1vMPTKbAoGBANVseOhYlVON1tRFjBNoHu68bko2reD9SzTpEXGVBeMJ3dIinEQGVbCj58SOU924KgTJLMHsRkjOmg22MsbHmbE1J0ON+smLXGmmodVHN/jDtGCd4fvFnySp1jKqboSQCZ6RxdRk1A3fplGwMszTODy198uHfma9mFLAX60OvtYBAoGAR9ACsusyoUxx7el+7HGRdAvdYrzlMxEq5ZGPH5pkozV13HZHYuQgc3zrQwmywTIm2+EPGPnhfrgIpn9gp4REIRec1bnKkMwFGbMI77ab/hXJEVMY7jhhkhh+7s6UkrRaWSLDgqjKI0O9YURmGF7+b61tfjZ6WLDqjPXNF5FZyM4=";
            byte[] keyBytes = Base64.getDecoder().decode(pk.getBytes(StandardCharsets.UTF_8));
            //byte[] keyBytes = Base64.getDecoder().decode(pk);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyBytes);

            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
             */
            String pub = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3dYLS93ZWdSIbGTighMdxFHszGRDaraYdGpk0T7I+tHCFPGCLXmfdPFRP6jqxkQS6pAwzhIlItwjW8/KsDRnzGeniSLk0j59Oqf3tL0TOsW0NlONRWQIshbPndhuGN96vRfdHqNKMuBXOnpUlbob1HiAYbKIwaeMgt2QNGITKxOQLPFWdJBQfilKAgEAKqg7+whQ2qdkikj9SS/ePEiAtnBFX72iyGHU7a06WUGJqaan/5STBIqeWhJlD1X8E5+P/3H/mVj5dA7mZJ1r1EKCscER4wdxGMhI/clGDNyx9DmCe+cOL1JBNWclGO35EIdwecR1fVZ7F7vTpT37TwL8PwIDAQAB";
            byte[] publicBytes = Base64.getDecoder().decode(pub);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

            logger.info("key id system property is {}", systemProperties.jwkId());
            logger.info("public key system property is {}", systemProperties.jwkPublic());

            RSAKey key = new RSAKey.Builder(publicKey)
                    .keyID(systemProperties.jwkId())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .build();

            JWKSet jwkSet = new JWKSet(key);
            logger.info("jwk key set is {}", jwkSet.toPublicJWKSet().toString(true));
            return jwkSet;
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PrivateKey privateKey() {
        try {
            String pk = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDd1gtL3dlZ1IhsZOKCEx3EUezMZENqtph0amTRPsj60cIU8YIteZ908VE/qOrGRBLqkDDOEiUi3CNbz8qwNGfMZ6eJIuTSPn06p/e0vRM6xbQ2U41FZAiyFs+d2G4Y33q9F90eo0oy4Fc6elSVuhvUeIBhsojBp4yC3ZA0YhMrE5As8VZ0kFB+KUoCAQAqqDv7CFDap2SKSP1JL948SIC2cEVfvaLIYdTtrTpZQYmppqf/lJMEip5aEmUPVfwTn4//cf+ZWPl0DuZknWvUQoKxwRHjB3EYyEj9yUYM3LH0OYJ75w4vUkE1ZyUY7fkQh3B5xHV9VnsXu9OlPftPAvw/AgMBAAECggEAHtI54JESvT3ftHux0tO4G4osZdt6WG3FMcuEdWQNRXf2SLWjWhEfW/6ZRMiB0gksPaXJ+h+gRD8ktIFizmeBkm9GYjHUa/edq5QhwN1SOiS03KVwJ9d1s9SihPJInbETGwmhQbv7N6VeyTOs496nNjaF080b3hga2CXTfsuvkRjU7GflGfQs2RzATdQZyWYxtqWtEVQ5xe2LcfNATzxjJvqvWZ6JhlGchF+rsyAouSR6Ydhok27Kp0cp80H/2k1B1MbfZDf00fAVTqHzszBLvOeHmegYR6vQNcZDRcjEnng+31tJeiBXX6pOfEKnZQSCr21TMwmgRSwAaLAHbWejwQKBgQD7DDZ6/BZURVpGh7yAGCUNcpSmHdTJUqKpvmsJlqkyt99+xnFof+7BnX+6g/R/NonhHuAn8/5CJdOUnfXjCBha59o++ugMH7jEvdkD8KcZy4rg6pC59wgAQgECTXTySjFDY3tl30HiXIf9xxfpVZJSX6faI4OFwtlXnWDo7t3y/wKBgQDiNlAs2naPkFsHLCcv8eZTYcwGpR0UapIkt4Ww/2bPm5Pfis7GaV5C0E5V4KKc2XHk2mm0hOti0rvrREQ4BOdtQ8UDPAsYCfYVirq5lkNevoGGYqXqPLmGnKVUoRw/uIPi1WPjbTz9Ijt+H1oc11BjBwHk5zuURUmQvv3WIJM2wQKBgBqsMoGglPLBJ5VEyKdHaXBjUx3PH5OAPx4PyCmNo05rMMi0Zso2hXR2umJjsK9vaPjNIf28s6/teuxYWA6WBphp98snN05KdIQas5ryj55y/L9mzJelgmkcmiTXe+xeK5vATxUEwmg9colpBMZavCAaX6gSi9/DUvo3E1vMPTKbAoGBANVseOhYlVON1tRFjBNoHu68bko2reD9SzTpEXGVBeMJ3dIinEQGVbCj58SOU924KgTJLMHsRkjOmg22MsbHmbE1J0ON+smLXGmmodVHN/jDtGCd4fvFnySp1jKqboSQCZ6RxdRk1A3fplGwMszTODy198uHfma9mFLAX60OvtYBAoGAR9ACsusyoUxx7el+7HGRdAvdYrzlMxEq5ZGPH5pkozV13HZHYuQgc3zrQwmywTIm2+EPGPnhfrgIpn9gp4REIRec1bnKkMwFGbMI77ab/hXJEVMY7jhhkhh+7s6UkrRaWSLDgqjKI0O9YURmGF7+b61tfjZ6WLDqjPXNF5FZyM4=";
            byte[] keyBytes = Base64.getDecoder().decode(pk.getBytes(StandardCharsets.UTF_8));
            //byte[] keyBytes = Base64.getDecoder().decode(pk);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyBytes);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            return privateKey;
        }
        catch( Exception e ) {
            throw new RuntimeException(e);
        }
    }

}
