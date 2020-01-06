package pl.edu.mimuw.cloudatlas.agent.modules;

import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;

import java.io.IOException;
import java.net.*;

public class UDUPClient {
    private UDUP udp;
    private UDUPSerializer serializer;
    private int serverPort;
    private DatagramSocket socket;
    private int bufsize;

    UDUPClient(UDUP udp, int serverPort, int bufferSize) throws SocketException {
        this.udp = udp;
        this.serverPort = serverPort;
        this.socket = new DatagramSocket();
        this.bufsize = bufferSize;
        this.serializer = new UDUPSerializer();
    }

    public void sendMessage(UDUPMessage msg) throws IOException {
        int offset = 0;
        int outputSize;

        byte[] buf = this.serializer.serialize(msg);
        outputSize = buf.length;

        do {
            outputSize =- bufsize;
            offset += bufsize;
            DatagramPacket packet = new DatagramPacket(buf, buf.length, msg.getContact().getAddress(), this.serverPort);
            System.out.println("UDP sends message: ");
            for (byte b : buf) {
                System.out.print(b);
            }
            System.out.println("to " + packet.getAddress());
            this.socket.send(packet);
        } while (outputSize > bufsize);
    }

    void close() {
        this.socket.close();
    }

}
