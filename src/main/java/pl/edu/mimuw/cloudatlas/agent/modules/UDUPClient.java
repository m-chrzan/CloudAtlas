package pl.edu.mimuw.cloudatlas.agent.modules;

import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;

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
                " with realbufsize " + (bufsize - 8) +
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
        byte[] sendBuf = new byte[bufsize];
        int sendBufSize = bufsize - 8;
        System.arraycopy(toByteArray(transmissionNo), 0, sendBuf, 0, 4);
        System.arraycopy(toByteArray(packetNo), 0, sendBuf, 4, 4);
        if (packetNo*sendBufSize >= buf.length) {
            System.arraycopy(buf, 0, sendBuf, 8, sendBufSize);
        } else {
            System.arraycopy(buf, (packetNo-1)*sendBufSize, sendBuf, 8, sendBufSize);
        }
        return sendBuf;
    }

    boolean checkEndOfTransmission(int packetNo, int dataBufSize) {
        int sendBufSize = bufsize - 8;
        int dataSentSoFar = (packetNo - 1) * sendBufSize;
        System.out.println("used data " + dataSentSoFar + " " + dataBufSize);
        return dataSentSoFar >= dataBufSize;
    }

    public void sendMessage(UDUPMessage msg) throws IOException {
        int packetNo = 1;
        byte[] sendBuf;
        byte[] dataBuf = this.serializer.serialize(msg);
        this.lastTransmission++;

        do {
            sendBuf = packSendBuffer(this.lastTransmission, packetNo, dataBuf);
            DatagramPacket packet = new DatagramPacket(sendBuf, bufsize, msg.getContact().getAddress(), this.serverPort);
            this.socket.send(packet);
            logSending(packet, packetNo, dataBuf);
            packetNo++;
        } while (!checkEndOfTransmission(packetNo, dataBuf.length));
    }

    void close() {
        this.socket.close();
    }

}
