package pl.edu.mimuw.cloudatlas.agent.modules;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StanikMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Serializes classes to and from byte arrays for UDP use
 */
public class UDUPSerializer {
    private Kryo kryo;

    UDUPSerializer() {
        kryo = new Kryo();
        kryo.setReferences(true);
        kryo.setRegistrationRequired(true);
        registerClasses();
    }

    private void registerClasses() {

        kryo.register(Inet4Address.class, new Serializer() {

            @Override
            public void write(Kryo kryo, Output output, Object object) {
                InetAddress ia = (InetAddress) object;
                kryo.writeObject(output, ia.getAddress());
            }

            @Override
            public Object read(Kryo kryo, Input input, Class type) {
                try {
                    byte[] buf = kryo.readObject(input, byte[].class);
                    InetAddress addr = Inet4Address.getByAddress(buf);
                    return addr;
                } catch (UnknownHostException e) {
                    System.out.println("Custom InetAddress read failed");
                    e.printStackTrace();
                    return null;
                }
            }
        });

        kryo.register(PathName.class, new Serializer() {

            @Override
            public void write(Kryo kryo, Output output, Object object) {
                PathName pn = (PathName) object;
                kryo.writeObject(output, pn.getName());
            }

            @Override
            public Object read(Kryo kryo, Input input, Class type) {
                String addr = input.readString();
                return new PathName(addr);
            }
        });

        kryo.register(byte[].class);
        kryo.register(ValueContact.class);
        kryo.register(ModuleType.class);

        kryo.register(AgentMessage.class);
        kryo.register(GetStateMessage.class);
        kryo.register(UDUPMessage.class);
        kryo.register(StanikMessage.Type.class);
        kryo.register(StanikMessage.class);
    }

    public UDUPMessage deserialize(byte[] packetData) {
        ByteArrayInputStream in = new ByteArrayInputStream(packetData);
        Input kryoInput = new Input(in);
        UDUPMessage msg = kryo.readObject(kryoInput, UDUPMessage.class);
        return msg;
    }

    public byte[] serialize(UDUPMessage msg) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Output kryoOut = new Output(out);
        kryo.writeObject(kryoOut, msg);
        kryoOut.flush();
        kryoOut.close();
        return out.toByteArray();
    }
}
