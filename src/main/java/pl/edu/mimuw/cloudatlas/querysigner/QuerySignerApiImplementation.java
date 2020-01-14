package pl.edu.mimuw.cloudatlas.querysigner;

import pl.edu.mimuw.cloudatlas.ByteSerializer;
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
    private final static String ENCRYPTION_ALGORITHM = "RSA";
    private final static String DIGEST_ALGORITHM = "SHA-256";
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private Map<String, ValueQuery> queries;
    private Set<String> attribsSetByQueries;

    public QuerySignerApiImplementation(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.queries = new HashMap<>();
        this.attribsSetByQueries = new HashSet<>();
    }

    private static String byteArrayToString(byte[] arr, int offset, int len) {
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

    private byte[] encryptQuery(byte[] query) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher signCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        signCipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] encryptedBytes = signCipher.doFinal(query);
        System.out.println(
                "Bytes encrypted with " + ENCRYPTION_ALGORITHM +
                        ": " + byteArrayToString(
                        encryptedBytes, 0, encryptedBytes.length));
        return encryptedBytes;
    }

    private static byte[] decryptQuery(byte[] encryptedQuery, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        Cipher verifyCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        verifyCipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] decryptedBytes = verifyCipher.doFinal(encryptedQuery);
        System.out.println(
                "Bytes decrypted with " + ENCRYPTION_ALGORITHM +
                        ": " + byteArrayToString(
                        decryptedBytes, 0, decryptedBytes.length));
        return decryptedBytes;
    }

    private static byte[] cryptographicHash(byte[] serializedQuery) throws NoSuchAlgorithmException {
        MessageDigest digestGenerator =
                MessageDigest.getInstance(DIGEST_ALGORITHM);
        byte[] digest = digestGenerator.digest(serializedQuery);
        System.out.println(
                DIGEST_ALGORITHM + " digest: " +
                        byteArrayToString(
                                digest, 0, digest.length));
        return digest;
    }

    private static byte[] serializeQuery(String queryName, String queryCode) {
        ByteSerializer byteSerializer = new ByteSerializer();
        return byteSerializer.serialize(queryName + queryCode);
    }

    @Override
    public QueryData signInstallQuery(String queryName, String queryCode) throws RemoteException {
        QueryUtils.validateQueryName(queryName);
        try {
            byte[] serializedQuery = serializeQuery(queryName, queryCode);
            byte[] hashedQuery = cryptographicHash(serializedQuery);
            byte[] querySignature = encryptQuery(hashedQuery);
            return new QueryData(queryCode, querySignature);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getLocalizedMessage());
        }
    }

    public static void validateInstallQuery(String queryName, QueryData query, PublicKey publicKey) throws RemoteException,IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, QuerySigner.InvalidQueryException {
        QueryUtils.validateQueryName(queryName);
        byte[] decryptedQuery = decryptQuery(query.getSignature(), publicKey);
        byte[] serializedQuery = serializeQuery(queryName, query.getCode());
        byte[] hashedSerializedQuery = cryptographicHash(serializedQuery);
        String decryptedQueryString = byteArrayToString(decryptedQuery, 0, decryptedQuery.length);
        String hashedSerializedQueryString = byteArrayToString(hashedSerializedQuery, 0, hashedSerializedQuery.length);
        if (!decryptedQueryString.equals(hashedSerializedQueryString)) {
            throw new QuerySigner.InvalidQueryException();
        }
    }

    // TODO
    @Override
    public QueryData signUninstallQuery(String queryName) throws RemoteException {
        return null;
    }

    // TODO
    public static void validateUninstallQuery(String queryName, QueryData query, PublicKey publicKey) throws RemoteException {

    }
}
