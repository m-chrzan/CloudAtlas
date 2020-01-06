package pl.edu.mimuw.cloudatlas.agent.modules;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.agent.messages.*;
import pl.edu.mimuw.cloudatlas.model.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;

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

        kryo.register(ValueList.class, new Serializer() {
            @Override
            public void write(Kryo kryo, Output output, Object object) {

            }

            @Override
            public Object read(Kryo kryo, Input input, Class type) {
                return null;
            }
        });

        kryo.register(ValueSet.class, new Serializer() {
            @Override
            public void write(Kryo kryo, Output output, Object object) {

            }

            @Override
            public Object read(Kryo kryo, Input input, Class type) {
                return null;
            }
        });

        // model
        kryo.register(Value.class);
        kryo.register(ValueBoolean.class);
        kryo.register(ValueContact.class);
        kryo.register(ValueDuration.class);
        kryo.register(ValueInt.class);
        kryo.register(ValueNull.class);
        kryo.register(ValueQuery.class);
        kryo.register(ValueSet.class);
        kryo.register(ValueString.class);
        kryo.register(ValueTime.class);
        kryo.register(ValueUtils.class);
        kryo.register(ZMI.class);

        kryo.register(Attribute.class);
        kryo.register(AttributesMap.class);
        kryo.register(AttributesUtil.class);

        kryo.register(Type.class);
        kryo.register(TypeCollection.class);
        kryo.register(TypePrimitive.class);

        // messages in chronological order so it's easier to keep track
        kryo.register(AgentMessage.class);
        kryo.register(GetStateMessage.class);
        kryo.register(QurnikMessage.class);
        kryo.register(RemikMessage.class);
        kryo.register(RemoveZMIMessage.class);
        kryo.register(RequestStateMessage.class);
        kryo.register(ResponseMessage.class);
        kryo.register(RunQueriesMessage.class);
        kryo.register(SetAttributeMessage.class);
        kryo.register(StanikMessage.Type.class);
        kryo.register(StanikMessage.class);
        kryo.register(TimerSchedulerMessage.class);
        kryo.register(UDUPMessage.class);
        kryo.register(UpdateAttributesMessage.class);
        kryo.register(UpdateQueriesMessage.class);

        // modules
        kryo.register(TimerScheduledTask.class);
        kryo.register(RecursiveScheduledTask.class);

        // other
        kryo.register(byte[].class);
        kryo.register(LinkedHashMap.class);
        kryo.register(ModuleType.class);
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
