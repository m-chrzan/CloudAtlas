package pl.edu.mimuw.cloudatlas.querysigner;

import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.querysignerapi.QuerySignerApi;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.rmi.RemoteException;
import java.security.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuerySignerApiImplementation implements QuerySignerApi {
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private final static String ENCRYPTION_ALGORITHM = "RSA";
    private final static int NUM_KEY_BITS = 1024;
    private Map<String, ValueQuery> queries;
    private Set<String> attribsSetByQueries;

    QuerySignerApiImplementation() {
        this.queries = new HashMap<>();
        this.attribsSetByQueries = new HashSet<>();
        try {
            generateKeys();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private String byteArrayToString(byte[] arr, int offset, int len) {
        StringBuffer sb = new StringBuffer();
        for (int i = offset, n = Math.min(arr.length, offset + len); i < n; ++i) {
            String hex = Integer.toHexString(0xFF & arr[i]);
            if (hex.length() < 2) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private void generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGenerator =
                KeyPairGenerator.getInstance(ENCRYPTION_ALGORITHM);
        keyGenerator.initialize(NUM_KEY_BITS);
        KeyPair keyPair = keyGenerator.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
    }

    private byte[] encryptQuery(String query) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher signCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        signCipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] encryptedBytes = signCipher.doFinal(query.getBytes());
        System.out.println(
                "Bytes encrypted with " + ENCRYPTION_ALGORITHM +
                        ": " + byteArrayToString(
                        encryptedBytes, 0, encryptedBytes.length));
        return encryptedBytes;
    }

    private String decryptQuery(byte[] encryptedQuery) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        Cipher verifyCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        verifyCipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] decryptedBytes = verifyCipher.doFinal(encryptedQuery);
        System.out.println(
                "Bytes decrypted with " + ENCRYPTION_ALGORITHM +
                        ": " + byteArrayToString(
                        decryptedBytes, 0, decryptedBytes.length));
        return new String(decryptedBytes);
    }

    @Override
    public byte[] signQuery(String queryName, String queryCode) throws RemoteException {
        try {
            return encryptQuery(queryName + queryCode);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            throw new RemoteException(e.getLocalizedMessage());
        }
    }

    @Override
    public String checkQuery(byte[] encryptedQuery, String queryName, String queryCode) throws RemoteException {
        try {
            return decryptQuery(encryptedQuery);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
            throw new RemoteException(e.getLocalizedMessage());
        }
    }

    @Override
    public PublicKey getPublicKey() throws RemoteException {
        return publicKey;
    }

    @Override
    public void setPublicKey(PublicKey publicKey) throws RemoteException {
        this.publicKey = publicKey;
    }

    @Override
    public byte[] getQuerySignature(String queryName) throws RemoteException {
        return queries.get(queryName).getSignature();
    }
}
