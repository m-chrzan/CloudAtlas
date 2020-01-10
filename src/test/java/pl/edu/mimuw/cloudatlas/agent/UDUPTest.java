package pl.edu.mimuw.cloudatlas.agent;

import org.junit.*;
import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateAttributesMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.agent.modules.UDUP;
import pl.edu.mimuw.cloudatlas.agent.modules.UDUPServer;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueInt;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

// TODO add serialization tests that target custom serializers (type collections!)

public class UDUPTest {

    @Test
    public void messageBetweenUDUPs() {
        UDUP udp1 = null;
        UDUP udp2 = null;
        UDUPServer server1 = null;
        UDUPServer server2 = null;
        UDUPMessage msg1 = null;
        boolean testSuccess = true;
        int timeout = 5000;

        try {
            System.out.println("Starting udp1");

            server1 = new UDUPServer(InetAddress.getByName("127.0.0.2"), 5996, 1000);
            udp1 = new UDUP(
                    5997,
                    timeout,
                    1000,
                    server1);

            System.out.println("Starting udp2");

            server2 = new UDUPServer(InetAddress.getByName("127.0.0.3"), 5996, 1000);
            udp2 = new UDUP(
                    5997,
                    timeout,
                    1000,
                    server2);

            UDUPMessage testContent = new UDUPMessage();
            testContent.setDestinationModule(ModuleType.TEST);
            testContent.setMessageId("singleMsgTest");

            msg1 = new UDUPMessage(
                    "udup1",
                    new ValueContact(new PathName("/udp2"), InetAddress.getByName("127.0.0.3")),
                    testContent
            );

        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }

        Thread udpThread1 = new Thread(server1);
        udpThread1.start();
        Thread udpThread2 = new Thread(server2);
        udpThread2.start();

        try {
            Thread.sleep(5000);
            System.out.println("Sending message");
            if (udp1 == null | udp2 == null) {
                Assert.fail("UDPs not initialized");
            } else {
                udp1.handle(msg1);
                Thread.sleep(10000);
            }
        } catch (InterruptedException | Module.InvalidMessageType e) {
            e.printStackTrace();
            testSuccess = false;
        }

        udpThread1.interrupt();
        udpThread2.interrupt();

        if (testSuccess) {
            Assert.assertTrue(true);
        } else {
            Assert.fail();
        }
    }

    @Test
    public void bigMessageBetweenUDUPs() {
        UDUP udp1 = null;
        UDUP udp2 = null;
        UDUPServer server1 = null;
        UDUPServer server2 = null;
        UDUPMessage msg1 = null;
        boolean testSuccess = true;
        int timeout = 5000;

        try {
            System.out.println("Starting udp1");

            server1 = new UDUPServer(InetAddress.getByName("127.0.0.2"), 5991, 1000);
            udp1 = new UDUP(
                    5997,
                    timeout,
                    30,
                    server1);

            System.out.println("Starting udp2");

            server2 = new UDUPServer(InetAddress.getByName("127.0.0.3"), 5991, 1000);
            udp2 = new UDUP(
                    5997,
                    timeout,
                    30,
                    server2);

            UDUPMessage testContent = new UDUPMessage();
            testContent.setDestinationModule(ModuleType.TEST);
            testContent.setMessageId("bigMsgTest");

            msg1 = new UDUPMessage(
                    "udup1",
                    new ValueContact(new PathName("/udp2"), InetAddress.getByName("127.0.0.3")),
                    testContent
            );

        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }

        Thread udpThread1 = new Thread(server1);
        udpThread1.start();
        Thread udpThread2 = new Thread(server2);
        udpThread2.start();

        try {
            udp1.handle(msg1);
            Thread.sleep(timeout + 1000);
        } catch (InterruptedException | Module.InvalidMessageType e) {
            e.printStackTrace();
            testSuccess = false;
        }

        udpThread1.interrupt();
        udpThread2.interrupt();

        if (testSuccess) {
            Assert.assertTrue(true);
        } else {
            Assert.fail();
        }
    }


    @Test
    public void sendMultipleMessages() {
        UDUP udp1 = null;
        UDUP udp2 = null;
        UDUPServer server1 = null;
        UDUPServer server2 = null;
        UDUPMessage msg1 = null;
        UDUPMessage msg2 = null;
        UDUPMessage msg3 = null;
        boolean testSuccess = true;
        int timeout = 5000;

        try {
            System.out.println("Starting udp1");

            server1 = new UDUPServer(InetAddress.getByName("127.0.0.2"), 5997, 1000);
            udp1 = new UDUP(
                    5997,
                    timeout,
                    1000,
                    server1);

            System.out.println("Starting udp2");

            server2 = new UDUPServer(InetAddress.getByName("127.0.0.3"), 5997, 1000);
            udp2 = new UDUP(
                    5997,
                    timeout,
                    1000,
                    server2);

            UDUPMessage testContent = new UDUPMessage();
            testContent.setDestinationModule(ModuleType.TEST);
            testContent.setMessageId("multipleMsgTest");

            msg1 = new UDUPMessage(
                    "udup1",
                    new ValueContact(new PathName("/udp2"), InetAddress.getByName("127.0.0.3")),
                    testContent
            );

            msg2 = new UDUPMessage(
                    "udup2",
                    new ValueContact(new PathName("/udp2"), InetAddress.getByName("127.0.0.3")),
                    testContent
            );

            msg3 = new UDUPMessage(
                    "udup3",
                    new ValueContact(new PathName("/udp2"), InetAddress.getByName("127.0.0.3")),
                    testContent
            );

        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }

        Thread udpThread1 = new Thread(server1);
        udpThread1.start();
        Thread udpThread2 = new Thread(server2);
        udpThread2.start();

        try {
            udp1.handle(msg1);
            udp1.handle(msg2);
            udp1.handle(msg3);
            Thread.sleep(timeout + 2000);
        } catch (InterruptedException | Module.InvalidMessageType e) {
            e.printStackTrace();
        }
    }
}
