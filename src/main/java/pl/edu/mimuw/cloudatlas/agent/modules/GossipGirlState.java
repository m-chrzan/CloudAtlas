package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import pl.edu.mimuw.cloudatlas.agent.messages.AttributesMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.HejkaMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.NoCoTamMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.QueryMessage;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ValueUtils;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class GossipGirlState {
    public enum State {
        WAIT_FOR_STATE_INITIALIZER,
        APPLY_HEJKA,
        WAIT_FOR_STATE_RESPONDER,
        SEND_HEJKA,
        SEND_NO_CO_TAM,
        SEND_INFO,
        SEND_INFO_AND_FINISH,
        WAIT_FOR_NO_CO_TAM,
        WAIT_FOR_FIRST_INFO,
        WAIT_FOR_INFO,
        FINISHED,
        ERROR
    }
    public ValueTime lastAction;
    public PathName ourPath;
    public ValueContact theirContact;
    public long gossipId;
    public long theirGossipId;
    public long timeOffest;
    public State state;
    public ZMI hierarchy;
    public Map<Attribute, ValueQuery> queries;
    public ValueTime hejkaSendTimestamp;
    public ValueTime hejkaReceiveTimestamp;
    public ValueTime noCoTamSendTimestamp;
    public ValueTime noCoTamReceiveTimestamp;
    public ValueDuration offset;
    private Map<PathName, ValueTime> theirZoneTimestamps;
    private Map<Attribute, ValueTime> theirQueryTimestamps;
    private List<PathName> zonesToSend;
    private List<Attribute> queriesToSend;
    private Set<PathName> waitingForZones;
    private Set<Attribute> waitingForQueries;
    private boolean initiating;

    public GossipGirlState(long gossipId, PathName ourPath, ValueContact theirContact, boolean initiating) {
        this.gossipId = gossipId;
        this.ourPath = ourPath;
        this.theirContact = theirContact;
        this.initiating = initiating;
        System.out.println("INFO: initializing Gossip state, their contact " + theirContact.toString());
        if (initiating) {
            state = State.WAIT_FOR_STATE_INITIALIZER;
        } else {
            state = State.APPLY_HEJKA;
        }
        this.lastAction = ValueUtils.currentTime();
    }

    public void setLastAction() {
        lastAction = ValueUtils.currentTime();
    }

    public void setState(ZMI hierarchy, Map<Attribute, ValueQuery> queries) {
        switch (state) {
            case WAIT_FOR_STATE_INITIALIZER:
                this.hierarchy = hierarchy;
                this.queries = queries;
                state = State.SEND_HEJKA;
                break;
            case WAIT_FOR_STATE_RESPONDER:
                this.hierarchy = hierarchy;
                this.queries = queries;
                state = State.SEND_NO_CO_TAM;
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

    public void sentNoCoTam() {
        switch (state) {
            case SEND_NO_CO_TAM:
                state = state.WAIT_FOR_FIRST_INFO;
                break;
            default:
                System.out.println("ERROR: tried to set gossip state when not expected");
                state = State.ERROR;
        }
    }

    public void handleHejka(HejkaMessage message) {
        switch (state) {
            case APPLY_HEJKA:
                theirGossipId = message.getSenderGossipId();
                theirZoneTimestamps = message.getZoneTimestamps();
                theirQueryTimestamps = message.getQueryTimestamps();
                hejkaSendTimestamp = message.getSentTimestamp();
                hejkaReceiveTimestamp = message.getReceivedTimestamp();
                state = State.WAIT_FOR_STATE_RESPONDER;
                break;
            default:
                System.out.println("ERROR: tried to set gossip state when not expected");
                state = State.ERROR;
        }
    }

    public void handleNoCoTam(NoCoTamMessage message) {
        System.out.println("DEBUG: in GossipGirlState handleNoCoTam");
        switch (state) {
            case WAIT_FOR_NO_CO_TAM:
                System.out.println("DEBUG: lets do this");
                theirGossipId = message.getSenderGossipId();
                theirZoneTimestamps = message.getZoneTimestamps();
                theirQueryTimestamps = message.getQueryTimestamps();
                hejkaSendTimestamp = message.getHejkaSendTimestamp();
                hejkaReceiveTimestamp = message.getHejkaReceiveTimestamp();
                noCoTamSendTimestamp = message.getSentTimestamp();
                noCoTamReceiveTimestamp = message.getReceivedTimestamp();
                computeOffset();
                System.out.println("DEBUG: set basic stuff");
                setZonesToSend();
                setQueriesToSend();
                setWaitingFor();
                System.out.println("DEBUG: set big stuff");
                state = State.SEND_INFO;
                break;
            default:
                System.out.println("ERROR: tried to set gossip state when not expected");
                state = State.ERROR;
        }
    }

    public void computeOffset() {
        ValueDuration rtd = (ValueDuration) (noCoTamReceiveTimestamp.subtract(hejkaSendTimestamp))
                .subtract(noCoTamSendTimestamp.subtract(hejkaReceiveTimestamp));
        offset = (ValueDuration) (noCoTamSendTimestamp.addValue(rtd.divide(new ValueInt(2l))))
                .subtract(noCoTamReceiveTimestamp);
        System.out.println("INFO: GossipGirlState calculated offset: " + offset.toString());
    }

    public ValueDuration delta() {
        ValueDuration delta = offset;
        if (!initiating) {
            delta = delta.negate();
        }
        return delta;
    }

    public AttributesMap modifyAttributes(AttributesMap attributes) {
        attributes.addOrChange("timestamp", attributes.getOrNull("timestamp").subtract(delta()));
        return attributes;
    }

    private void setWaitingFor() {
        setWaitingForZones();
        setWaitingForQueries();
    }

    private void setWaitingForZones() {
        waitingForZones = new HashSet(theirZoneTimestamps.keySet());
        for (PathName path : zonesToSend) {
            waitingForZones.remove(path);
        }
    }

    private void setWaitingForQueries() {
        waitingForQueries = new HashSet(theirQueryTimestamps.keySet());
        for (Attribute name : queriesToSend) {
            waitingForQueries.remove(name);
        }
    }

    public Map<PathName, ValueTime> getZoneTimestampsToSend() {
        Map<PathName, ValueTime> timestamps = new HashMap();
        System.out.println("Getting zone timestamps to send to " + theirContact.getName().toString());
        System.out.println("hierarchy is " + hierarchy.toString());
        collectZoneTimestamps(timestamps, hierarchy, theirContact.getName());
        return timestamps;
    }

    public Map<Attribute, ValueTime> getQueryTimestampsToSend() {
        Map<Attribute, ValueTime> queryTimestamps= new HashMap();
        for (Entry<Attribute, ValueQuery> query : queries.entrySet()) {
            queryTimestamps.put(query.getKey(), new ValueTime(query.getValue().getTimestamp()));
        }

        return queryTimestamps;
    }

    public void setZonesToSend() {
        zonesToSend = new LinkedList();
        System.out.println("DEBUG: timestamps to send: " + getZoneTimestampsToSend().toString());
        for (Entry<PathName, ValueTime> timestampedPath : getZoneTimestampsToSend().entrySet()) {
            ValueTime theirTimestamp = theirZoneTimestamps.get(timestampedPath.getKey());
            if (theirTimestamp == null || ValueUtils.valueLower(theirTimestamp.subtract(delta()), timestampedPath.getValue())) {
                zonesToSend.add(timestampedPath.getKey());
            }
        }
        System.out.println("DEBUG: zones to send: " + zonesToSend.toString());
    }

    public void setQueriesToSend() {
        queriesToSend = new LinkedList();
        for (Entry<Attribute, ValueTime> timestampedQuery : getQueryTimestampsToSend().entrySet()) {
            ValueTime theirTimestamp = theirQueryTimestamps.get(timestampedQuery.getKey());
            if (theirTimestamp == null || ValueUtils.valueLower(theirTimestamp, timestampedQuery.getValue())) {
                queriesToSend.add(timestampedQuery.getKey());
            }
        }
        System.out.println("DEBUG: Queries to send: " + queriesToSend.toString());
    }

    public List<ZMI> getZMIsToSend() {
        List<ZMI> zmis = new LinkedList();
        for (PathName path : zonesToSend) {
            try {
                zmis.add(hierarchy.findDescendant(path));
            } catch (ZMI.NoSuchZoneException e) {
                System.out.println("ERROR: didn't find a zone we wanted to send in getZMIsToSend");
            }
        }
        return zmis;
    }

    public List<Entry<Attribute, ValueQuery>> getQueriesToSend() {
        List<Entry<Attribute, ValueQuery>> queryList = new LinkedList();
        for (Attribute name : queriesToSend) {
            queryList.add(
                new SimpleImmutableEntry(
                    name,
                    queries.get(name)
                )
            );
        }
        return queryList;
    }

    public void collectZoneTimestamps(Map<PathName, ValueTime> timestamps, ZMI currentZMI, PathName recipientPath) {
        System.out.println("collecting timestamps, on " + currentZMI.getPathName().toString());
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
        return ValueUtils.isPrefix(zmiPath.levelUp(), recipientPath) && !ValueUtils.isPrefix(zmiPath, recipientPath);
    }

    public void sentInfo() {
        switch (state) {
            case SEND_INFO:
                state = State.WAIT_FOR_INFO;
                break;
            case SEND_INFO_AND_FINISH:
                state = State.FINISHED;
                break;
            default:
                System.out.println("ERROR: tried to set gossip state when not expected");
                state = State.ERROR;
        }
    }

    public void gotAttributes(AttributesMessage message) {
        switch (state) {
            case WAIT_FOR_INFO:
                if (!waitingForZones.remove(message.getPath())) {
                    System.out.println("DEBUG: got zone attributes we weren't expecting");
                }
                if (waitingForZones.isEmpty() && waitingForQueries.isEmpty()) {
                    System.out.println("INFO: done waiting for info");
                    state = state.FINISHED;
                }
                break;
            case WAIT_FOR_FIRST_INFO:
                offset = message.getOffset();
                setZonesToSend();
                setQueriesToSend();
                setWaitingFor();
                state = State.SEND_INFO;

                if (!waitingForZones.remove(message.getPath())) {
                    System.out.println("DEBUG: got zone attributes we weren't expecting");
                }
                if (waitingForZones.isEmpty() && waitingForQueries.isEmpty()) {
                    System.out.println("INFO: done waiting for info");
                    state = state.SEND_INFO_AND_FINISH;
                }
                break;
            default:
                System.out.println("ERROR: got attributes when not expected");
                state = State.ERROR;
        }
    }

    public void gotQuery(QueryMessage message) {
        switch (state) {
            case WAIT_FOR_FIRST_INFO:
                offset = message.getOffset();
                setZonesToSend();
                setQueriesToSend();
                setWaitingFor();
                state = State.SEND_INFO;

                if (!waitingForQueries.remove(message.getName())) {
                    System.out.println("DEBUG: got query we weren't expecting");
                }
                if (waitingForZones.isEmpty() && waitingForQueries.isEmpty()) {
                    System.out.println("INFO: done waiting for info");
                    state = state.SEND_INFO_AND_FINISH;
                }
                break;
            case WAIT_FOR_INFO:
                if (!waitingForQueries.remove(message.getName())) {
                    System.out.println("DEBUG: got query we weren't expecting");
                }
                if (waitingForZones.isEmpty() && waitingForQueries.isEmpty()) {
                    System.out.println("INFO: done waiting for info");
                    state = state.FINISHED;
                }
                break;
            default:
                System.out.println("ERROR: got query when not expected");
                state = State.ERROR;
        }
    }

    public ValueTime getTheirQueryTimestamp(Attribute name) {
        return theirQueryTimestamps.get(name);
    }
}
