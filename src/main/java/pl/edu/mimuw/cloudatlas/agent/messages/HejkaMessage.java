package pl.edu.mimuw.cloudatlas.agent.messages;

import java.util.Map;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueTime;

public class HejkaMessage extends RemoteGossipGirlMessage {
    private long senderGossipId;
    private Map<PathName, ValueTime> zoneTimestamps;
    private Map<Attribute, ValueTime> queryTimestamps;

    public HejkaMessage(String messageId, long timestamp, long senderGossipId, Map<PathName, ValueTime> zoneTimestamps, Map<Attribute, ValueTime> queryTimestamps) {
        super(messageId, timestamp, Type.HEJKA);
        this.senderGossipId = senderGossipId;
        this.zoneTimestamps = zoneTimestamps;
        this.queryTimestamps = queryTimestamps;
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
}
