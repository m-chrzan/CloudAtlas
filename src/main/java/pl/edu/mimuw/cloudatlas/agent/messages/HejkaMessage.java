package pl.edu.mimuw.cloudatlas.agent.messages;

import java.util.Map;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueTime;

public class HejkaMessage extends RemoteGossipGirlMessage {
    private long senderGossipId;
    private PathName senderPath;
    private PathName receiverPath;
    private Map<PathName, ValueTime> zoneTimestamps;
    private Map<Attribute, ValueTime> queryTimestamps;

    public HejkaMessage(String messageId, long timestamp, long senderGossipId, PathName senderPath, PathName receiverPath, Map<PathName, ValueTime> zoneTimestamps, Map<Attribute, ValueTime> queryTimestamps) {
        super(messageId, timestamp, Type.HEJKA);
        this.senderGossipId = senderGossipId;
        this.senderPath = senderPath;
        this.receiverPath = receiverPath;
        this.zoneTimestamps = zoneTimestamps;
        this.queryTimestamps = queryTimestamps;
    }

    public long getSenderGossipId() {
        return senderGossipId;
    }

    public PathName getSenderPath() {
        return senderPath;
    }

    public PathName getReceiverPath() {
        return receiverPath;
    }

    public Map<PathName, ValueTime> getZoneTimestamps() {
        return zoneTimestamps;
    }

    public Map<Attribute, ValueTime> getQueryTimestamps() {
        return queryTimestamps;
    }
}
