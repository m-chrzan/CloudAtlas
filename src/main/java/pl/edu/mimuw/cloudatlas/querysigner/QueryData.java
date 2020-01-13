package pl.edu.mimuw.cloudatlas.querysigner;

import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Program;

public class QueryData {
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
