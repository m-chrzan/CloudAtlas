package pl.edu.mimuw.cloudatlas.querysigner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyUtils {
    private final static String ENCRYPTION_ALGORITHM = "RSA";

    public static PublicKey getPublicKey(String filename){
        try {
            byte[] byteKey = Files.readAllBytes(Paths.get(filename));
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
            return kf.generatePublic(X509publicKey);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey getPrivateKey(String filename){
        try {
            byte[] byteKey = Files.readAllBytes(Paths.get(filename));
            PKCS8EncodedKeySpec PKCS8privateKey = new PKCS8EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
            return kf.generatePrivate(PKCS8privateKey);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
