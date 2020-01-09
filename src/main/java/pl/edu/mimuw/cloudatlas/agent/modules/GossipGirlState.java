package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.agent.messages.NoCoTamMessage;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class GossipGirlState {
    public enum State {
        WAIT_FOR_STATE_INITIALIZER,
        WAIT_FOR_STATE_RESPONDER,
        SEND_HEJKA,
        SEND_INFO,
        WAIT_FOR_NO_CO_TAM,
        WAIT_FOR_FIRST_INFO,
        WAIT_FOR_INFO,
        ERROR
    }
    public PathName ourPath;
    public ValueContact theirContact;
    public long gossipId;
    public long theirGossipId;
    public long timeOffest;
    public State state;
    public ZMI hierarchy;
    public Map<Attribute, Entry<ValueQuery, ValueTime>> queries;
    public ValueTime hejkaSendTimestamp;
    public ValueTime hejkaReceiveTimestamp;
    public ValueTime noCoTamSendTimestamp;
    public ValueTime noCoTamSendReceiveTimestamp;
    private Map<PathName, ValueTime> theirZoneTimestamps;
    private Map<Attribute, ValueTime> theirQueryTimestamps;

    public GossipGirlState(long gossipId, PathName ourPath, ValueContact theirContact, boolean initiating) {
        this.gossipId = gossipId;
        this.ourPath = ourPath;
        this.theirContact = theirContact;
        if (initiating) {
            state = State.WAIT_FOR_STATE_INITIALIZER;
        } else {
            state = State.WAIT_FOR_STATE_RESPONDER;
        }
    }

    public void setState(ZMI hierarchy, Map<Attribute, Entry<ValueQuery, ValueTime>> queries) {
        switch (state) {
            case WAIT_FOR_STATE_INITIALIZER:
                this.hierarchy = hierarchy;
                this.queries = queries;
                state = State.SEND_HEJKA;
                break;
            case WAIT_FOR_STATE_RESPONDER:
                this.hierarchy = hierarchy;
                this.queries = queries;
                state = State.WAIT_FOR_FIRST_INFO;
                break;
            default:
                System.out.println("ERROR: tried to set gossip state when not expected");
                state = State.ERROR;
        }
    }

    public void sentHejka() {
        switch (state) {
            case SEND_HEJKA:
                state = state.WAIT_FOR_NO_CO_TAM;
                break;
            default:
                System.out.println("ERROR: tried to set gossip state when not expected");
                state = State.ERROR;
        }
    }

    public void handleNoCoTam(NoCoTamMessage message) {
        switch (state) {
            case WAIT_FOR_NO_CO_TAM:
                theirGossipId = message.getSenderGossipId();
                theirZoneTimestamps = message.getZoneTimestamps();
                theirQueryTimestamps = message.getQueryTimestamps();
                hejkaSendTimestamp = message.getHejkaSendTimestamp();
                hejkaReceiveTimestamp = message.getHejkaReceiveTimestamp();
                state = State.SEND_INFO;
                break;
            default:
                System.out.println("ERROR: tried to set gossip state when not expected");
                state = State.ERROR;
        }
    }

    public Map<PathName, ValueTime> getZoneTimestampsToSend() {
        Map<PathName, ValueTime> timestamps = new HashMap();
        collectZoneTimestamps(timestamps, hierarchy, theirContact.getName());
        return timestamps;
    }

    public Map<Attribute, ValueTime> getQueryTimestampsToSend() {
        Map<Attribute, ValueTime> queryTimestamps= new HashMap();
        for (Entry<Attribute, Entry<ValueQuery, ValueTime>> query : queries.entrySet()) {
            queryTimestamps.put(query.getKey(), query.getValue().getValue());
        }

        return queryTimestamps;
    }

    public List<ZMI> getZMIsToSend() {
        return new LinkedList();
    }

    public void collectZoneTimestamps(Map<PathName, ValueTime> timestamps, ZMI currentZMI, PathName recipientPath) {
        for (ZMI zmi : currentZMI.getSons()) {
            if (interestedIn(recipientPath, zmi.getPathName())) {
                ValueTime timestamp = (ValueTime) zmi.getAttributes().getOrNull("timestamp");
                if (timestamp != null) {
                    timestamps.put(zmi.getPathName(), timestamp);
                } else {
                    System.out.println("ERROR: collectZoneTimestamps encountered a zone with no timestamp");
                }
            } else {
                collectZoneTimestamps(timestamps, zmi, recipientPath);
            }
        }
    }

    public boolean interestedIn(PathName recipientPath, PathName zmiPath) {
        return isPrefix(zmiPath.levelUp(), recipientPath) && !isPrefix(zmiPath, recipientPath);
    }

    public boolean isPrefix(PathName prefix, PathName path) {
        List<String> prefixComponents = prefix.getComponents();
        List<String> pathComponents = path.getComponents();

        if (prefixComponents.size() > pathComponents.size()) {
            return false;
        }

        Iterator<String> prefixIterator = prefixComponents.iterator();
        Iterator<String> pathIterator = pathComponents.iterator();

        while (prefixIterator.hasNext()) {
            if (!prefixIterator.next().equals(pathIterator.next())) {
                return false;
            }
        }
        return true;
    }

    public void sentInfo() {
        switch (state) {
            case SEND_INFO:
                state = State.WAIT_FOR_INFO;
                break;
            default:
                System.out.println("ERROR: tried to set gossip state when not expected");
                state = State.ERROR;
        }
    }
}
