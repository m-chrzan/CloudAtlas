package pl.edu.mimuw.cloudatlas.agent;

import org.junit.*;
import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.agent.modules.UDUP;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class UDUPTest {

    @Test
    public void messageBetweenUDUPs() {
        UDUP udp1 = null;
        UDUP udp2 = null;
        UDUPMessage msg1 = null;
        boolean testSuccess = true;

        try {
            System.out.println("Starting udp1");

            udp1 = new UDUP(
                    InetAddress.getByName("127.0.0.2"),
                    5999,
                    5000,
                    1000);

            System.out.println("Starting udp2");

            udp2 = new UDUP(
                    InetAddress.getByName("127.0.0.3"),
                    5999,
                    5000,
                    1000);

            UDUPMessage testContent = new UDUPMessage();
            testContent.setDestinationModule(ModuleType.TEST);

            msg1 = new UDUPMessage(
                    "udup1",
                    new ValueContact(new PathName("/udp2"), InetAddress.getByName("127.0.0.3")),
                    testContent
            );

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Thread udpThread1 = new Thread(udp1);
        udpThread1.start();
        Thread udpThread2 = new Thread(udp2);
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
/*
    @Test
    public void bigMessageBetweenUDUPs() {
        UDUP udp1 = new UDUP(
                ModuleType.UDP,
                5999,
                1000,
                5000,
                256);

        UDUP udp2 = new UDUP(
                ModuleType.UDP,
                5998,
                1000,
                5000,
                256);

        AttributesMap bigAttrib = new AttributesMap();
        for (Long i = 1L; i < 20; i++) {
            bigAttrib.add(i.toString(), new ValueInt(i));
        }

        UDUPMessage msg1 = new UDUPMessage(
                "udp1",
                ModuleType.UDP,
                "localhost",
                5998,
                new UpdateAttributesMessage("updateattrib1", 0,"/", bigAttrib),
                0,
                "conv1");

        try {
            udp1.handle(msg1);
            Thread.sleep(1000);
            UDUPMessage conv = udp2.fetchConversation("conv1");
            Assert.assertEquals(conv.getConversationId(), "conv1");
        } catch (InterruptedException | Module.InvalidMessageType e) {
            e.printStackTrace();
        } catch (UDUP.InvalidConversation noConversationError) {
            Assert.fail();
        }
    }


    @Test
    public void sendMultipleMessages() {
        @Test
        public void messageBetweenUDUPs() {
            UDUP udp1 = new UDUP(
                    ModuleType.UDP,
                    5999,
                    1000,
                    5000,
                    3,
                    256);

            UDUP udp2 = new UDUP(
                    ModuleType.UDP,
                    5998,
                    1000,
                    5000,
                    3,
                    256);

            UDUPMessage msg1 = new UDUPMessage(
                    "1",
                    ModuleType.UDP,
                    "localhost",
                    5998,
                    null,
                    0,
                    "conv1");




            try {
                udp1.handle(msg1);
                udp1.handle(msg2);
                udp1.handle(msg3);
                Thread.sleep(1000);
                UDUPMessage conv1 = udp2.fetchConversation("conv1");
                Assert.assertEquals(conv1.getConversationId(), "conv1");
                UDUPMessage conv2 = udp2.fetchConversation("conv2");
                Assert.assertEquals(conv2.getConversationId(), "conv2");
                UDUPMessage conv3 = udp2.fetchConversation("conv3");
                Assert.assertEquals(conv3.getConversationId(), "conv3");
            } catch (InterruptedException | Module.InvalidMessageType e) {
                e.printStackTrace();
            } catch (UDUP.InvalidConversation invalidConversation) {
                Assert.fail();
            }
        }
    }
*/
}
