package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.agent.messages.AttributesMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GossipGirlMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.HejkaMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.InitiateGossipMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.NoCoTamMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.ResponseMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
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
            case NO_CO_TAM:
                handleNoCoTam((NoCoTamMessage) message);
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

    private void setState(StateMessage message) throws InterruptedException {
        GossipGirlState state = gossipStates.get(message.getRequestId());
        if (state != null) {
            state.setState(message.getZMI(), message.getQueries());
            if (state.state == GossipGirlState.State.SEND_HEJKA) {
                HejkaMessage hejka = new HejkaMessage(
                        "",
                        0,
                        state.gossipId,
                        getZoneTimestamps(message.getZMI()),
                        getQueryTimestamps(message.getQueries())
                );
                UDUPMessage udupMessage = new UDUPMessage("", 0, state.theirContact, hejka);
                sendMessage(udupMessage);
                state.sentHejka();
            }
        } else {
            System.out.println("ERROR: GossipGirl got state for a nonexistent gossip");
        }
    }

    private void handleNoCoTam(NoCoTamMessage message) throws InterruptedException {
        GossipGirlState state = gossipStates.get(message.getReceiverGossipId());
        if (state != null) {
            state.handleNoCoTam(message);
            for (ZMI zmi : state.getZMIsToSend()) {
                AttributesMessage attributesMessage = new AttributesMessage("", 0, zmi.getPathName(), zmi.getAttributes(), state.theirGossipId);
                UDUPMessage udupMessage = new UDUPMessage("", 0, state.theirContact, attributesMessage);
                sendMessage(udupMessage);
            }
            // TODO: send queries
            state.sentInfo();
        } else {
            System.out.println("ERROR: GossipGirl got state for a nonexistent gossip");
        }
    }

    public Map<PathName, ValueTime> getZoneTimestamps(ZMI root) {
        return new HashMap();
    }

    public Map<Attribute, ValueTime> getQueryTimestamps(Map<Attribute, Entry<ValueQuery, ValueTime>> queries) {
        return new HashMap();
    }

}
