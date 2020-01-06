package pl.edu.mimuw.cloudatlas.agent.modules;

import com.google.common.primitives.Bytes;
import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class UDUPServer {
    private UDUP udp;
    private UDUPSerializer serializer;
    private DatagramSocket socket;
    private InetAddress address;
    private HashMap<InetAddress, byte[]> partialPackets;
    private int bufSize;

    public UDUPServer(UDUP udp, InetAddress addr, int port, int bufSize) throws SocketException {
        this.udp = udp;
        this.socket = new DatagramSocket(port, addr);
        this.address = addr;
        this.bufSize = bufSize;
        this.partialPackets = new HashMap<>();
        this.serializer = new UDUPSerializer();
    }

    public void acceptMessage() throws IOException, InterruptedException {
        byte[] buf = new byte[bufSize];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        this.socket.receive(packet);
        System.out.println("UDP " + this.address + " received packet from " + packet.getAddress());

        if (packet.getOffset() == 0) {
            UDUPMessage msg = this.serializer.deserialize(packet.getData());
            System.out.println("UDP received message " + msg.getContent().getMessageId());

            if (packet.getLength() == this.bufSize) {
                this.addPartialMessageAndCheckSerialization(packet.getAddress(), packet.getData());
            } else  {
                if (msg.getContent().getDestinationModule() == ModuleType.TEST) {
                    System.out.println("UDP server: test message received");
                } else if (msg.getContent().getDestinationModule() != ModuleType.UDP) {
                    this.udp.sendMessage(msg.getContent());
                }
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
                UDUPMessage msg = this.serializer.deserialize(allPacketData);
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
