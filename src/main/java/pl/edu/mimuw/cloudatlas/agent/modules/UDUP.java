package pl.edu.mimuw.cloudatlas.agent.modules;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.primitives.Bytes;
import pl.edu.mimuw.cloudatlas.agent.EventBus;
import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Communication over UDP
 *
 * Client-server exchange pattern:
 *  server: init message
 *  client: ack message
 *
 *  retry count
 *  retry timeout after which retry happens
 **
 *  udp sends initiator module success/failure information
 *
 *  we have udps on different addresses with the same ports
 *  due to ValueContact design
 */

public class UDUP extends Module implements Runnable {
    public class InvalidConversation extends Exception {
        public InvalidConversation(String message) { super(message); }
    }

    public class InvalidContact extends Exception {
        public InvalidContact(String message) { super(message); }
    }

    private UDUPClient client;
    private UDUPServer server;
    private HashMap<String, UDUPMessage> currentConversations; // TODO find blocking one
    private final AtomicBoolean running;

    public UDUP(ModuleType moduleType,
                InetAddress serverAddr,
                int serverPort,
                int retryTimeout,
                int retriesCount,
                int bufferSize) {
        super(moduleType);
        this.currentConversations = new HashMap<>();
        this.running = new AtomicBoolean(true);
        try {
            this.client = new UDUPClient(this, serverPort, retryTimeout, retriesCount, bufferSize);
            this.server = new UDUPServer(this, serverAddr, serverPort, bufferSize);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
            this.client.close();
            this.server.close();
            this.running.getAndSet(false);
        }
    }

    public void run() {
        System.out.println("UDP server running");
        while(this.running.get()) {
            try {
                this.server.acceptMessage();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                this.running.getAndSet(false);
                this.server.close();
            }
        }
    }

    public void handleTyped(UDUPMessage event) throws InterruptedException {
//        System.out.println("UDP sending message " + event.getContent().getMessageId());
        this.client.sendMessage(event);
    }

    // also used for updating
    public void addConversation(UDUPMessage msg) {
        this.currentConversations.put(msg.getConversationId(), msg);
    }

    public UDUPMessage fetchConversation(String conversationId) throws InvalidConversation {
        UDUPMessage ret = this.currentConversations.get(conversationId);
        if (ret == null) {
            throw new InvalidConversation("Conversation does not exist");
        } else {
            return ret;
        }
    }

    // TODO add conversation removal
    public void removeConversation(String conversationId) {
        this.currentConversations.remove(conversationId);
    }

    public UDUPMessage deserialize(ByteArrayInputStream in) {
        Kryo kryo = new Kryo();
        Input kryoInput = new Input(in);
        UDUPMessage msg = kryo.readObject(kryoInput, UDUPMessage.class);
        return msg;
    }

    public void serialize(ByteArrayOutputStream out, UDUPMessage msg) {
        Kryo kryo = new Kryo();
        Output kryoOut = new Output(out);
        kryo.writeObject(kryoOut, msg);
        kryoOut.flush();
    }

}
