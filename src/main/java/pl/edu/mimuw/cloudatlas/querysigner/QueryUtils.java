package pl.edu.mimuw.cloudatlas.querysigner;

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
}
