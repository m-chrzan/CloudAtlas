package pl.edu.mimuw.cloudatlas.agent.modules;

import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ValueUtils;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class UDUPClient {
    private UDUP udp;
    private UDUPSerializer serializer;
    private int serverPort;
    private DatagramSocket socket;
    private int bufsize;
    private int lastTransmission;

    UDUPClient(UDUP udp, int serverPort, int bufferSize) throws SocketException {
        this.udp = udp;
        this.serverPort = serverPort;
        this.socket = new DatagramSocket();
        this.bufsize = bufferSize;
        this.serializer = new UDUPSerializer();
        this.lastTransmission = 0;
    }

    private void logSending(DatagramPacket packet, int packetNo, byte[] buf) {
        System.out.print("UDP sends packet no " + packetNo +
                " with real bufsize " + (bufsize - 8) +
                " out of data buffer with size " + buf.length +
                " to " + packet.getAddress() + ": ");
        for (byte b : packet.getData()) {
            System.out.print(b);
        }
        System.out.println();
    }

    byte[] toByteArray(int val) {
        return ByteBuffer.allocate(4).putInt(val).array();
    }

    private byte[] packSendBuffer(int transmissionNo, int packetNo, byte[] buf) {
        byte[] sendBuf;
        int sendBufSize = bufsize - 8;

        if (packetNo*sendBufSize >= buf.length) {
            int copyLength = buf.length - (packetNo-1)*sendBufSize;
            sendBuf = new byte[copyLength + 8];
            System.arraycopy(buf, (packetNo-1)*sendBufSize, sendBuf, 8, copyLength);
        } else {
            sendBuf = new byte[bufsize];
            System.arraycopy(buf, (packetNo-1)*sendBufSize, sendBuf, 8, sendBufSize);
        }

        System.arraycopy(toByteArray(transmissionNo), 0, sendBuf, 0, 4);
        System.arraycopy(toByteArray(packetNo), 0, sendBuf, 4, 4);

        return sendBuf;
    }

    boolean checkEndOfTransmission(int packetNo, int dataBufSize) {
        int sendBufSize = bufsize - 8;
        int dataSentSoFar = (packetNo - 1) * sendBufSize;
        return dataSentSoFar >= dataBufSize;
    }

    public void sendMessage(UDUPMessage msg) throws IOException {
        int packetNo = 1;
        byte[] sendBuf;
        byte[] dataBuf;
        this.lastTransmission++;

        msg.getContent().setSentTimestamp(ValueUtils.currentTime());
        dataBuf = this.serializer.serialize(msg);

        do {
            sendBuf = packSendBuffer(this.lastTransmission, packetNo, dataBuf);
            DatagramPacket packet = new DatagramPacket(sendBuf, 0, sendBuf.length, msg.getContact().getAddress(), this.serverPort);
            this.socket.send(packet);
            logSending(packet, packetNo, dataBuf);
            packetNo++;
        } while (!checkEndOfTransmission(packetNo, dataBuf.length));
    }

    void close() {
        this.socket.close();
    }

}
