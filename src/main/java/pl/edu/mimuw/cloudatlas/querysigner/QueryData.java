package pl.edu.mimuw.cloudatlas.querysigner;

import java.io.Serializable;

public class QueryData implements Serializable {
    // Original source code
    private String code;
    // Query signature
    private byte[] signature;
    // Query signing timestamp
    private long timestamp;

    public QueryData(String code, byte[] signature) {
        this.code = code;
        this.signature = signature;
        this.timestamp = System.currentTimeMillis();;
    }

    public String getCode() {
        return code;
    }

    public byte[] getSignature() {
        return signature;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
