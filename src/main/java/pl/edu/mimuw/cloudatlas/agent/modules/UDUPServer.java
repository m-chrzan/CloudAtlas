package pl.edu.mimuw.cloudatlas.agent.modules;

import com.google.common.primitives.Bytes;
import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class UDUPServer {
    UDUP udp;
    private DatagramSocket socket;
    private InetAddress address;
    private byte[] buf;
    private HashMap<InetAddress, byte[]> partialPackets;

    public UDUPServer(UDUP udp, InetAddress addr, int port, int bufSize) throws SocketException, UnknownHostException {
        this.udp = udp;
        this.socket = new DatagramSocket(port, addr);
        this.address = addr;
        this.buf = new byte[bufSize];
        this.partialPackets = new HashMap<>();
    }

    public void acceptMessage() throws IOException, InterruptedException {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        this.socket.receive(packet);
        System.out.println("UDP received packet: " + packet.getData());

        if (packet.getOffset() == 0) {
            UDUPMessage msg = this.udp.deserialize(new ByteArrayInputStream(packet.getData()));
            // TODO check if not full packet anyway
            // TODO check for errors if it's not the end od transmission

            this.udp.addConversation(msg);
            System.out.println("UDP received message " + msg.getContent().getMessageId());

            if (msg.getDestinationModule() != ModuleType.UDP) {
                this.udp.sendMessage(msg.getContent());
            }
        } else {
            this.addPartialMessageAndCheckSerialization(packet.getAddress(), packet.getData());
        }
    }

    public void addPartialMessageAndCheckSerialization(InetAddress senderAddress, byte[] packetData) {
        if (this.partialPackets.containsKey(senderAddress)) {
            byte[] previousPacketData = this.partialPackets.get(senderAddress);
            byte[] allPacketData = Bytes.concat(previousPacketData, packetData);
            try {
                UDUPMessage msg = this.udp.deserialize(new ByteArrayInputStream(allPacketData));
                this.udp.sendMessage(msg.getContent());
                this.partialPackets.remove(senderAddress);
            } catch (Error | Exception e) {
                System.out.println("Kryo didn't deserialize partial message, waiting to receive the rest");
                this.partialPackets.put(senderAddress, allPacketData);
            }
        } else {
            this.partialPackets.put(senderAddress, packetData);
        }
    }

    public void close() {
        this.socket.close();
    }

}
