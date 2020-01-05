package pl.edu.mimuw.cloudatlas.agent.modules;

import pl.edu.mimuw.cloudatlas.agent.messages.TimerSchedulerMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;

public class UDUPClient {
    private UDUP udp;
    private int serverPort;
    private int timeout;
    private int retriesCount;
    private DatagramSocket socket;
    private int bufsize;

    UDUPClient(UDUP udp, int serverPort, int timeout, int retriesCount, int bufferSize) throws SocketException, UnknownHostException {
        this.udp = udp;
        this.serverPort = serverPort;
        this.timeout = timeout;
        this.retriesCount = retriesCount;
        this.socket = new DatagramSocket();
        this.bufsize = bufferSize;
    }

    // TODO make sure that retry count in message is updated correctly
    public void sendMessage(UDUPMessage msg) throws InterruptedException {
        String messageId = msg.getMessageId();

        if (msg.getRetry() >= this.retriesCount) {
            this.udp.removeConversation(msg.getConversationId());
        } else {
            this.udp.addConversation(msg);
            try {
                sendUDP(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }

            msg.setRetry(msg.getRetry() + 1);

            // TODO add sending message to timer with retry
            /*
            this.udp.executor.passMessage(new TimerSchedulerMessage(
                    "",
                    0,
                    "",
                    this.timeout,
                    System.currentTimeMillis() / 1000L,
                    new TimerScheduledTask() {
                        @Override
                        public void run() {
                            try {
                                this.sendMessage(msg);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }));

             */
        }
    }

    private void sendUDP(UDUPMessage msg) throws IOException {
        int offset = 0;
        int outputSize;
        byte[] buf = new byte[bufsize];
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        this.udp.serialize(output, msg);
        outputSize = output.size();

        do {
            output.write(buf, offset, bufsize);
            System.out.println("UDP sends message: " + buf);
            outputSize =- bufsize;
            offset += bufsize;
            DatagramPacket packet = new DatagramPacket(buf, buf.length, msg.getContact().getAddress(), this.serverPort);
            this.socket.send(packet);
        } while (outputSize > bufsize);
    }

    void close() {
        this.socket.close();
    }

}
