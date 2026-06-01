package com.innowise.authservice.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@Getter
@Setter
public class JwtKeyConfig {
    @Value("${jwt.keystore.path}")
    private String keysPath;
    @Value("${jwt.keystore.password}")
    private String keysPassword;
    @Value("${jwt.keystore.alias}")
    private String keysAlias;

    @Bean
    public RSAKey rsaKeys() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try (InputStream inputStream = new ClassPathResource(keysPath).getInputStream()) {
            keyStore.load(inputStream, keysPassword.toCharArray());
        }

        RSAPrivateKey privateKey = (RSAPrivateKey) keyStore.getKey(keysAlias, keysPassword.toCharArray());

        RSAPublicKey publicKey = (RSAPublicKey) keyStore.getCertificate(keysAlias).getPublicKey();

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey){
        JWKSet jwkSet = new JWKSet(rsaKey);

        return new NimbusJwtEncoder(new ImmutableJWKSet<>(jwkSet));
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) throws JOSEException {
        return NimbusJwtDecoder
                .withPublicKey(rsaKey.toRSAPublicKey())
                .build();
    }

}
