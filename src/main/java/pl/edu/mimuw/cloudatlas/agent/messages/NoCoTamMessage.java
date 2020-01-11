package pl.edu.mimuw.cloudatlas.agent.messages;

import java.util.Map;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueTime;

public class NoCoTamMessage extends RemoteGossipGirlMessage {
    private long receiverGossipId;
    private long senderGossipId;
    private Map<PathName, ValueTime> zoneTimestamps;
    private Map<Attribute, ValueTime> queryTimestamps;
    private ValueTime hejkaSendTimestamp;
    private ValueTime hejkaReceiveTimestamp;

    public NoCoTamMessage(String messageId, long timestamp, long senderGossipId, long receiverGossipId, Map<PathName, ValueTime> zoneTimestamps, Map<Attribute, ValueTime> queryTimestamps, ValueTime hejkaSendTimestamp, ValueTime hejkaReceiveTimestamp) {
        super(messageId, timestamp, Type.NO_CO_TAM);
        this.receiverGossipId = receiverGossipId;
        this.senderGossipId = senderGossipId;
        this.zoneTimestamps = zoneTimestamps;
        this.queryTimestamps = queryTimestamps;
        this.hejkaSendTimestamp = hejkaSendTimestamp;
        this.hejkaReceiveTimestamp = hejkaReceiveTimestamp;
    }

    private NoCoTamMessage() {}

    public long getReceiverGossipId() {
        return receiverGossipId;
    }

    public long getSenderGossipId() {
        return senderGossipId;
    }

    public Map<PathName, ValueTime> getZoneTimestamps() {
        return zoneTimestamps;
    }

    public Map<Attribute, ValueTime> getQueryTimestamps() {
        return queryTimestamps;
    }

    public ValueTime getHejkaSendTimestamp() {
        return hejkaSendTimestamp;
    }

    public ValueTime getHejkaReceiveTimestamp() {
        return hejkaReceiveTimestamp;
    }
}
