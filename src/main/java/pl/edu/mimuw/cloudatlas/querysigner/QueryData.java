package pl.edu.mimuw.cloudatlas.querysigner;

import java.io.Serializable;

public class QueryData implements Serializable {
    // Original source code
    private String code;
    // Query signature
    private byte[] signature;
    // Query signing timestamp
    private long timestamp;

    private boolean installed;

    public QueryData(String code, byte[] signature) {
        this.code = code;
        this.signature = signature;
        this.timestamp = System.currentTimeMillis();
        this.installed = true;
    }

    public QueryData(String code, byte[] signature, long timestamp, boolean installed) {
        this.code = code;
        this.signature = signature;
        this.timestamp = timestamp;
        this.installed = installed;
    }

    public QueryData() {}

    public String getCode() {
        return code;
    }

    public byte[] getSignature() {
        return signature;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isInstalled() { return installed; }

    public void setInstalled(boolean installed) { this.installed = installed; }
}
