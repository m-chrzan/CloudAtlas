package pl.edu.mimuw.cloudatlas.querysigner;

import pl.edu.mimuw.cloudatlas.model.ValueQuery;

import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryUtils {

    public static void validateQueryName(String queryName) throws RemoteException {
        Pattern queryNamePattern = Pattern.compile("&[a-zA-Z][\\w_]*");
        Matcher matcher = queryNamePattern.matcher(queryName);
        if (!matcher.matches()) {
            throw new RemoteException("Invalid query identifier");
        }
    }

    public static QueryData constructQueryData(ValueQuery valueQuery) {
        return new QueryData(
                valueQuery.getCode(),
                valueQuery.getSignature(),
                valueQuery.getTimestamp(),
                valueQuery.isInstalled()
        );
    }
}
