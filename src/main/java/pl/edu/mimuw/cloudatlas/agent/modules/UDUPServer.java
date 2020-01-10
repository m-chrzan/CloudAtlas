package pl.edu.mimuw.cloudatlas.agent.modules;

import com.google.common.primitives.Bytes;
import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;
import pl.edu.mimuw.cloudatlas.model.ValueUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

public class UDUPServer implements Runnable {
    private UDUP udp;
    private UDUPSerializer serializer;
    private DatagramSocket socket;
    private InetAddress address;
    private HashMap<String, ArrayList<byte[]>> partialPackets;
    private int bufSize;
    private final AtomicBoolean running;

    public UDUPServer(InetAddress addr, int port, int bufSize) throws SocketException {
        this.socket = new DatagramSocket(port, addr);
        this.address = addr;
        this.bufSize = bufSize;
        this.partialPackets = new HashMap<>();
        this.serializer = new UDUPSerializer();
        this.running = new AtomicBoolean(false);
    }

    public void setUDUP(UDUP udup) {
        this.udp = udup;
    }

    public void run() {
        System.out.println("UDP server running");
        this.running.getAndSet(true);
        while(this.running.get()) {
            try {
                this.acceptMessage();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                this.running.getAndSet(false);
                this.close();
            }
        }
    }

    public void acceptMessage() throws IOException, InterruptedException {
        DatagramPacket packet = receivePacket();
        int transmissionNo = readTransmissionNo(packet.getData());
        String transmissionID = packTransmissionID(transmissionNo, packet.getAddress());
        int packetNo = readPacketNo(packet.getData());
        byte[] packetData = trimPacketBuffer(packet.getData());
        UDUPMessage msg;

        if (packetNo == 1 && packet.getLength() < this.bufSize) {
            msg = this.serializer.deserialize(packetData);
            System.out.println("UDP received message " + msg.getContent().getMessageId());
        } else {
            System.out.println("UDP received partial message with transmission id " + transmissionID + " packet no " + packetNo);
            msg = this.addPartialMessageAndCheckSerialization(transmissionID, packetNo, packetData);
        }

        if (msg != null) {
            msg.getContent().setReceivedTimestamp(ValueUtils.currentTime());
            msg.getContent().setSenderAddress(packet.getAddress());
            sendMessageFurther(msg);
        }
    }

    private DatagramPacket receivePacket() throws IOException {
        byte[] buf = new byte[bufSize];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        this.socket.receive(packet);
        System.out.println("UDP " + this.address + " received packet from " + packet.getAddress());
        return packet;
    }

    private void sendMessageFurther(UDUPMessage msg) throws InterruptedException {
        if (msg.getContent().getDestinationModule() == ModuleType.TEST) {
            System.out.println("UDP server: test message received");
        } else if (msg.getContent().getDestinationModule() != ModuleType.UDP) {
            this.udp.sendMessage(msg.getContent());
        }
    }

    private int readTransmissionNo(byte[] packetData) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(packetData, 0, 4);
        byte[] byteBuf = new byte[4];
        in.read(byteBuf);
        return ByteBuffer.wrap(byteBuf).getInt();
    }

    private int readPacketNo(byte[] packetData) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(packetData,4, 4);
        byte[] byteBuf = new byte[4];
        in.read(byteBuf);
        return ByteBuffer.wrap(byteBuf).getInt();
    }

    private byte[] trimPacketBuffer(byte[] packetData) {
        int newPacketDataSize = packetData.length - 8;
        byte[] newPacketData = new byte[newPacketDataSize];
        System.arraycopy(packetData, 8, newPacketData, 0, newPacketDataSize);
        return newPacketData;
    }

    private String packTransmissionID(int transmissionNo, InetAddress contactAddr) {
        return contactAddr.getHostAddress() + ":" + transmissionNo;
    }

    private byte[] concatPacketData(String transmissionID, int newPacketNo, byte[] newPacketData) {
        ArrayList<byte[]> previousPacketData = this.partialPackets.get(transmissionID);
        byte[] fullData = new byte[0];

        previousPacketData.add(newPacketNo - 1, newPacketData);
        this.partialPackets.put(transmissionID, previousPacketData);

        if (previousPacketData.contains(null)) {
            throw new NoSuchElementException("Packet is not full");
        } else {
            for (byte[] prevData : previousPacketData) {
                fullData = Bytes.concat(fullData, prevData);
            }
        }

        return fullData;
    }

    public UDUPMessage addPartialMessageAndCheckSerialization(String transmissionID, int newPacketNo, byte[] packetData) {
        UDUPMessage msg = null;

        if (this.partialPackets.containsKey(transmissionID)) {
            try {
                byte[] allPacketData = concatPacketData(transmissionID, newPacketNo, packetData);
                msg = this.serializer.deserialize(allPacketData);
                this.partialPackets.remove(transmissionID);
                System.out.println("Kryo put together whole transmission for msg " + msg.getContent().getMessageId());
            } catch (Error | Exception e) {
                System.out.println("Kryo didn't deserialize partial message, waiting to receive the rest");
            }
        } else {
            ArrayList<byte[]> newTransmission = new ArrayList<byte[]>();
            newTransmission.add(newPacketNo-1, packetData);
            this.partialPackets.put(transmissionID, newTransmission);
        }

        return msg;
    }

    public void close() {
        this.socket.close();
    }

}
