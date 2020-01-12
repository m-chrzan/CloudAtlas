package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.agent.messages.AttributesMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.CleanOldGossipsMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GossipGirlMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.HejkaMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.InitiateGossipMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.NoCoTamMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.QueryMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.ResponseMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateAttributesMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateQueriesMessage;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class GossipGirl extends Module {
    private long nextGossipId = 0;

    private Map<Long, GossipGirlState> gossipStates;
    public GossipGirl() {
        super(ModuleType.GOSSIP);
        gossipStates = new HashMap();
    }

    public void handleTyped(GossipGirlMessage message) throws InterruptedException, InvalidMessageType {
        switch(message.getType()) {
            case INITIATE:
                initiateGossip((InitiateGossipMessage) message);
                break;
            case HEJKA:
                receiveGossip((HejkaMessage) message);
                break;
            case NO_CO_TAM:
                handleNoCoTam((NoCoTamMessage) message);
                break;
            case ATTRIBUTES:
                handleAttributes((AttributesMessage) message);
                break;
            case QUERY:
                handleQuery((QueryMessage) message);
                break;
            case CLEAN:
                cleanOldGossips((CleanOldGossipsMessage) message);
                break;
            default:
                throw new InvalidMessageType("This type of message cannot be handled by GossipGirl");
        }
    }

    public void handleTyped(ResponseMessage message) throws InterruptedException, InvalidMessageType {
        switch(message.getType()) {
            case STATE:
                setState((StateMessage) message);
                break;
            default:
                throw new InvalidMessageType("This type of message cannot be handled by GossipGirl");
        }
    }

    private void initiateGossip(InitiateGossipMessage message) throws InterruptedException {
        Long gossipId = nextGossipId;
        nextGossipId++;
        gossipStates.put(gossipId, new GossipGirlState(gossipId, message.getOurPath(), message.getTheirContact(), true));

        GetStateMessage getState = new GetStateMessage("", 0, ModuleType.GOSSIP, gossipId);
        sendMessage(getState);
    }

    private void receiveGossip(HejkaMessage message) throws InterruptedException {
        Long gossipId = nextGossipId;
        nextGossipId++;
        gossipStates.put(gossipId, new GossipGirlState(
                    gossipId,
                    message.getReceiverPath(),
                    new ValueContact(message.getSenderPath(), message.getSenderAddress()),
                    false
                )
        );

        gossipStates.get(gossipId).handleHejka(message);

        GetStateMessage getState = new GetStateMessage("", 0, ModuleType.GOSSIP, gossipId);
        sendMessage(getState);
    }

    private void setState(StateMessage message) throws InterruptedException {
        GossipGirlState state = gossipStates.get(message.getRequestId());
        if (state != null) {
            state.setLastAction();
            state.setState(message.getZMI(), message.getQueries());
            if (state.state == GossipGirlState.State.SEND_HEJKA) {
                HejkaMessage hejka = new HejkaMessage(
                        "",
                        0,
                        state.gossipId,
                        state.ourPath,
                        state.theirContact.getName(),
                        state.getZoneTimestampsToSend(),
                        state.getQueryTimestampsToSend()
                );
                UDUPMessage udupMessage = new UDUPMessage("", 0, state.theirContact, hejka);
                sendMessage(udupMessage);
                state.sentHejka();
            } else if (state.state == GossipGirlState.State.SEND_NO_CO_TAM) {
                NoCoTamMessage noCoTam = new NoCoTamMessage(
                        "",
                        0,
                        state.gossipId,
                        state.theirGossipId,
                        state.getZoneTimestampsToSend(),
                        state.getQueryTimestampsToSend(),
                        state.hejkaSendTimestamp,
                        state.hejkaReceiveTimestamp
                );
                UDUPMessage udupMessage = new UDUPMessage("", 0, state.theirContact, noCoTam);
                sendMessage(udupMessage);
                state.sentNoCoTam();
            }
        } else {
            System.out.println("ERROR: GossipGirl got state for a nonexistent gossip");
        }
    }

    private void handleNoCoTam(NoCoTamMessage message) throws InterruptedException {
        GossipGirlState state = gossipStates.get(message.getReceiverGossipId());
        if (state != null) {
            state.setLastAction();
            state.handleNoCoTam(message);
            sendInfo(state);
        } else {
            System.out.println("ERROR: GossipGirl got state for a nonexistent gossip");
        }
    }

    private void sendInfo(GossipGirlState state) throws InterruptedException {
        for (ZMI zmi : state.getZMIsToSend()) {
            AttributesMessage attributesMessage = new AttributesMessage("", 0, zmi.getPathName(), zmi.getAttributes(), state.theirGossipId);
            UDUPMessage udupMessage = new UDUPMessage("", 0, state.theirContact, attributesMessage);
            sendMessage(udupMessage);
        }

        for (Entry<Attribute, ValueQuery> query : state.getQueriesToSend()) {
            QueryMessage queryMessage = new QueryMessage("", 0, query.getKey(), query.getValue(), state.theirGossipId);
            UDUPMessage udupMessage = new UDUPMessage("", 0, state.theirContact, queryMessage);
            sendMessage(udupMessage);
        }
        state.sentInfo();
    }

    private void handleAttributes(AttributesMessage message) throws InterruptedException {
        GossipGirlState state = gossipStates.get(message.getReceiverGossipId());
        if (state != null) {
            state.setLastAction();
            state.gotAttributes(message);
            if (state.state == GossipGirlState.State.SEND_INFO) {
                sendInfo(state);
            }
            UpdateAttributesMessage updateMessage = new UpdateAttributesMessage("", 0, message.getPath().toString(), message.getAttributes());
            sendMessage(updateMessage);
            if (state.state == GossipGirlState.State.FINISHED) {
                gossipStates.remove(message.getReceiverGossipId());
            }
        } else {
            System.out.println("ERROR: GossipGirl got attributes for a nonexistent gossip");
        }
    }

    private void handleQuery(QueryMessage message) throws InterruptedException {
        GossipGirlState state = gossipStates.get(message.getReceiverGossipId());
        if (state != null) {
            state.setLastAction();
            state.gotQuery(message.getName());
            Map<Attribute, Entry<ValueQuery, ValueTime>> queries = new HashMap();
            queries.put(
                message.getName(),
                new SimpleImmutableEntry(message.getQuery(), state.getTheirQueryTimestamp(message.getName()))
            );
            UpdateQueriesMessage updateMessage = new UpdateQueriesMessage("", 0, queries);
            sendMessage(updateMessage);
            if (state.state == GossipGirlState.State.FINISHED) {
                gossipStates.remove(message.getReceiverGossipId());
            }
        } else {
            System.out.println("ERROR: GossipGirl got query for a nonexistent gossip");
        }
    }

    private void cleanOldGossips(CleanOldGossipsMessage message) {
        Iterator<Entry<Long, GossipGirlState>> iterator = gossipStates.entrySet().iterator();
        while (iterator.hasNext()) {
            GossipGirlState state = iterator.next().getValue();
            if (state.lastAction.isLowerThan(message.getAgeThreshold()).getValue()) {
                System.out.println("INFO: GossipGirl removing old gossip " + Long.toString(state.gossipId));
                iterator.remove();
            }
        }
    }
}
