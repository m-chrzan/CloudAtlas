package pl.edu.mimuw.cloudatlas.agent.modules;

import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
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

// TODO add timestamps as close to sending as possible

// TODO wysylac tylko remotegossipgirl message
// TODO update timestampow odpowiedni w tym remotegossipgirlmessage

public class UDUP extends Module {
    private UDUPClient client;
    private UDUPServer server;

    public UDUP(int serverPort,
                int timeout,
                int bufferSize,
                UDUPServer server) {
        super(ModuleType.UDP);
        try {
            this.client = new UDUPClient(this, serverPort, bufferSize);
            this.server = server;
            this.server.setUDUP(this);
        } catch (SocketException e) {
            e.printStackTrace();
            this.client.close();
            this.server.close();
        }
    }

    public UDUPServer getServer() {
        return this.server;
    }

    public void handleTyped(UDUPMessage event) throws InterruptedException {
        System.out.println("UDP sending message " + event.getContent().getMessageId());
        try {
            this.client.sendMessage(event);
        } catch (IOException e) {
            System.out.println("UDP send message failed");
            e.printStackTrace();
        }
    }
}
