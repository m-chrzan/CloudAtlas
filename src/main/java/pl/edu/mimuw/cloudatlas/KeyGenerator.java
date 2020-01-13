package pl.edu.mimuw.cloudatlas;

import java.io.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyGenerator {
    private final static int NUM_KEY_BITS = 1024;
    private final static String ENCRYPTION_ALGORITHM = "RSA";

    // TODO
    private static void printKeyToFile(byte[] keyValue, String fileName) {
        System.out.println("\n BEGIN KEY");
        for (byte k : keyValue) {
            System.out.print(k);
        }
        System.out.println("\n EOF KEY");
        String keyString = new String(keyValue);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(keyString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateKeys() throws NoSuchAlgorithmException {
        PublicKey publicKey;
        PrivateKey privateKey;

        KeyPairGenerator keyGenerator =
                KeyPairGenerator.getInstance(ENCRYPTION_ALGORITHM);
        keyGenerator.initialize(NUM_KEY_BITS);
        KeyPair keyPair = keyGenerator.generateKeyPair();

        String publicKeyFile = System.getProperty("public_key_file");
        String privateKeyFile = System.getProperty("private_key_file");

        privateKey = keyPair.getPrivate();
        printKeyToFile(privateKey.getEncoded(), privateKeyFile);
        publicKey = keyPair.getPublic();
        printKeyToFile(publicKey.getEncoded(), publicKeyFile);
    }

    public static PublicKey getPublicKey(String key){
        try {
            byte[] byteKey = Base64.getDecoder().decode(key.getBytes());
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
            return kf.generatePublic(X509publicKey);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey getPrivateKey(String key){
        try {
            byte[] byteKey = Base64.getDecoder().decode(key.getBytes());
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
            return kf.generatePrivate(X509publicKey);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readKeyFromFile(String filename) throws IOException {
        String key;
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        key = reader.readLine();
        reader.close();
        return key;
    }

    public static void main(String[] args) {
        try {
            generateKeys();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
