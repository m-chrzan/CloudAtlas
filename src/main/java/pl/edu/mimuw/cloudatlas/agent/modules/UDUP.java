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

// TODO set server port in global config - must be the same everywhere
// TODO same with buffer size

// TODO separate server like newapiimpl
// TODO add timestamps as close to sending as possible

// TODO wysylac tylko remotegossipgirl message
// TODO update timestampow odpowiedni w tym remotegossipgirlmessage

public class UDUP extends Module implements Runnable {
    private UDUPClient client;
    private UDUPServer server;
    private final AtomicBoolean running;

    public UDUP(InetAddress serverAddr,
                int serverPort,
                int timeout,
                int bufferSize) {
        super(ModuleType.UDP);
        this.running = new AtomicBoolean(false);
        try {
            this.client = new UDUPClient(this, serverPort, bufferSize);
            this.server = new UDUPServer(this, serverAddr, serverPort, bufferSize);
            this.running.getAndSet(true);
        } catch (SocketException e) {
            e.printStackTrace();
            this.client.close();
            this.server.close();
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
        System.out.println("UDP sending message " + event.getContent().getMessageId());
        try {
            this.client.sendMessage(event);
        } catch (IOException e) {
            System.out.println("UDP send message failed");
            e.printStackTrace();
        }
    }
}
