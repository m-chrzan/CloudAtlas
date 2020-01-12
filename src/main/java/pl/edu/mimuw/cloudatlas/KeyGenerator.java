package pl.edu.mimuw.cloudatlas;

import java.security.*;

public class KeyGenerator {
    private final static int NUM_KEY_BITS = 1024;
    private final static String ENCRYPTION_ALGORITHM = "RSA";

    // TODO
    // TODO read key values from files
    // TODO gradlew
    private static void printKeyToFile(byte[] keyValue, String fileName) {

    }

    private static void generateKeys() throws NoSuchAlgorithmException {
        PublicKey publicKey;
        PrivateKey privateKey;

        KeyPairGenerator keyGenerator =
                KeyPairGenerator.getInstance(ENCRYPTION_ALGORITHM);
        keyGenerator.initialize(NUM_KEY_BITS);
        KeyPair keyPair = keyGenerator.generateKeyPair();

        privateKey = keyPair.getPrivate();
        printKeyToFile(privateKey.getEncoded(), "query_signer");
        publicKey = keyPair.getPublic();
        printKeyToFile(publicKey.getEncoded(), "query_signer.pub");
    }

    public static void main() {
        try {
            generateKeys();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
