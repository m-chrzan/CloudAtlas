package pl.edu.mimuw.cloudatlas.agent.modules;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.assertj.core.data.MapEntry;
import pl.edu.mimuw.cloudatlas.agent.messages.*;
import pl.edu.mimuw.cloudatlas.model.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.text.DateFormat;
import java.util.*;

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
                ValueList vl = (ValueList) object;
                kryo.writeObject(output, ((TypeCollection) vl.getType()).getElementType());
                ArrayList<Value> al = new ArrayList<>();
                for (Value v : vl.getValue()) {
                    al.add(v);
                }
                kryo.writeObject(output, al);
            }

            @Override
            public Object read(Kryo kryo, Input input, Class type) {
                TypePrimitive t = kryo.readObject(input, TypePrimitive.class);
                ArrayList list = kryo.readObject(input, ArrayList.class);
                return new ValueList(list, t);
            }
        });

        kryo.register(ValueSet.class, new Serializer() {
            @Override
            public void write(Kryo kryo, Output output, Object object) {
                ValueSet vs = (ValueSet) object;
                kryo.writeObject(output, ((TypeCollection) vs.getType()).getElementType());
                HashSet<Value> hs = new HashSet();
                for (Value v : vs.getValue()) {
                    hs.add(v);
                }
                kryo.writeObject(output, hs);
            }

            @Override
            public Object read(Kryo kryo, Input input, Class type) {
                TypePrimitive t = kryo.readObject(input, TypePrimitive.class);
                HashSet set = kryo.readObject(input, HashSet.class);
                return new ValueSet(set, t);
            }
        });

        kryo.register(AttributesMap.class, new Serializer() {
            @Override
            public void write(Kryo kryo, Output output, Object object) {
                AttributesMap attribMap = (AttributesMap) object;
                HashMap<Attribute, Value> hashMap = new HashMap<>();

                for (Map.Entry<Attribute, Value> e : attribMap) {
                    hashMap.put(e.getKey(), e.getValue());
                }

                kryo.writeObject(output, hashMap);
            }

            @Override
            public Object read(Kryo kryo, Input input, Class type) {
                HashMap<Attribute, Value> hashMap = kryo.readObject(input, HashMap.class);
                AttributesMap attribMap = new AttributesMap();
                for (Map.Entry<Attribute, Value> e : hashMap.entrySet()) {
                    attribMap.add(e.getKey(), e.getValue());
                }
                return attribMap;
            }
        });

        // model
        kryo.register(Value.class);
        kryo.register(ValueBoolean.class);
        kryo.register(ValueContact.class);
        kryo.register(ValueDuration.class);
        kryo.register(ValueDouble.class);
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
        kryo.register(Type.PrimaryType.class);
        kryo.register(TypeCollection.class);
        kryo.register(TypePrimitive.class);

        // messages in chronological order so it's easier to keep track
        kryo.register(AgentMessage.class);
        kryo.register(AttributesMessage.class);
        kryo.register(GetStateMessage.class);
        kryo.register(HejkaMessage.class);
        kryo.register(NoCoTamMessage.class);
        kryo.register(QueryMessage.class);
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
        kryo.register(GossipGirlMessage.class);
        kryo.register(GossipGirlMessage.Type.class);
        kryo.register(RemoteGossipGirlMessage.class);

        // modules
        kryo.register(TimerScheduledTask.class);
        kryo.register(RecursiveScheduledTask.class);

        // other
        kryo.register(byte[].class);
        kryo.register(LinkedHashMap.class);
        kryo.register(HashMap.class);
        kryo.register(HashSet.class);
        kryo.register(ModuleType.class);
        kryo.register(DateFormat.class);
        kryo.register(ArrayList.class);
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
        System.out.println("SERIALIZING " + msg.getContent());
        kryo.writeObject(kryoOut, msg);
        kryoOut.flush();
        kryoOut.close();
        return out.toByteArray();
    }
}
