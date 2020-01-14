package pl.edu.mimuw.cloudatlas.agent;

import org.junit.Test;
import pl.edu.mimuw.cloudatlas.querysigner.KeyUtils;
import pl.edu.mimuw.cloudatlas.querysigner.QueryData;
import pl.edu.mimuw.cloudatlas.querysigner.QuerySigner;
import pl.edu.mimuw.cloudatlas.querysigner.QuerySignerApiImplementation;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class QuerySignerApiImplementationTest {

    @Test
    public void testQueryInstallVerification() {
        QuerySignerApiImplementation queryApi;

        try {
            String publicKeyFile = "build/tmp/query_signer.pub";
            String privateKeyFile = "build/tmp/query_signer";
            PublicKey publicKey = KeyUtils.getPublicKey(publicKeyFile);
            PrivateKey privateKey = KeyUtils.getPrivateKey(privateKeyFile);
            queryApi = new QuerySignerApiImplementation(publicKey, privateKey);
            QueryData signedQuery = queryApi.signInstallQuery("&a", "SELECT 1 AS ONE");
            QuerySignerApiImplementation.validateInstallQuery("&a", signedQuery, publicKey);
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | QuerySigner.InvalidQueryException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testQueryUninstallVerification() {
        QuerySignerApiImplementation queryApi;
        try {
            String publicKeyFile = "build/tmp/query_signer.pub";
            String privateKeyFile = "build/tmp/query_signer";
            PublicKey publicKey = KeyUtils.getPublicKey(publicKeyFile);
            PrivateKey privateKey = KeyUtils.getPrivateKey(privateKeyFile);
            queryApi = new QuerySignerApiImplementation(publicKey, privateKey);
            QueryData signedQuery = queryApi.signUninstallQuery("&a");
            QuerySignerApiImplementation.validateUninstallQuery("&a", signedQuery, publicKey);
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | QuerySigner.InvalidQueryException e) {
            e.printStackTrace();
        }
    }
}
