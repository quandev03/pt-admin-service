package vn.vnsky.bcss.admin.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@UtilityClass
public final class CryptoUtil {

    private static final String CERT_ADDED_LINE_REGEX = "-----.*-----";

    private static final String RSA_ALGORITHM = "RSA";

    @SneakyThrows
    public RSAPublicKey readPublicKey(Resource publicKeyResource) {
        String publicKeyStr = publicKeyResource.getContentAsString(StandardCharsets.UTF_8);
        return readPublicKey(publicKeyStr);
    }

    @SneakyThrows
    public RSAPublicKey readPublicKey(String publicKeyStr) {
        publicKeyStr = publicKeyStr
                .replaceAll(CERT_ADDED_LINE_REGEX, "")
                .replace("\r", "")
                .replace("\n", "");

        byte[] rsaPublicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
        KeyFactory kf = KeyFactory.getInstance(RSA_ALGORITHM);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(rsaPublicKeyBytes);
        return (RSAPublicKey) kf.generatePublic(x509EncodedKeySpec);
    }

    @SneakyThrows
    public RSAPrivateKey readPrivateKey(Resource privateKeyResource) {
        String privateKeyStr = privateKeyResource.getContentAsString(StandardCharsets.UTF_8);
        return readPrivateKey(privateKeyStr);
    }

    @SneakyThrows
    public RSAPrivateKey readPrivateKey(String privateKeyStr) {
        privateKeyStr = privateKeyStr
                .replaceAll(CERT_ADDED_LINE_REGEX, "")
                .replace("\r", "")
                .replace("\n", "");
        byte[] privateKeyEncoded = Base64.getDecoder().decode(privateKeyStr);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyEncoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    @SneakyThrows
    public X509Certificate readCertificate(String certStr) {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        String b64Cert = certStr
                .replaceAll(CERT_ADDED_LINE_REGEX, "")
                .replace("\n", "")
                .replace("\r", "");
        byte[] certBytes = Base64.getDecoder().decode(b64Cert);
        InputStream certIS = new ByteArrayInputStream(certBytes);
        return (X509Certificate) cf.generateCertificate(certIS);
    }

    @SneakyThrows
    public static KeyPair generateRsaKey() {
        KeyPair keyPair;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }
}
