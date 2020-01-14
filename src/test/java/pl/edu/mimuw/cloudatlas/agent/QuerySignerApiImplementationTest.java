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
    public void testQueryVerification() {
        QuerySignerApiImplementation queryApi;

        try {
            String publicKeyFile = "build/tmp/query_signer.pub";
            String privateKeyFile = "build/tmp/query_signer";
            PublicKey publicKey = KeyUtils.getPublicKey(publicKeyFile);
            PrivateKey privateKey = KeyUtils.getPrivateKey(privateKeyFile);
            queryApi = new QuerySignerApiImplementation(publicKey, privateKey);
            QueryData signedQuery = queryApi.signInstallQuery("&a", "SELECT 1 AS ONE");
            QuerySignerApiImplementation.validateInstallQuery("&a", signedQuery, publicKey);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (QuerySigner.InvalidQueryException e) {
            e.printStackTrace();
        }

    }
}
