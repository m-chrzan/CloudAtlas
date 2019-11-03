package pl.edu.mimuw.cloudatlas.model;

import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import org.junit.Test;
import static org.junit.Assert.*;

public class ZMITest {
    @Test
    public void testSimpleSerialization1() {
        ZMI zmi = new ZMI();
        AttributesMap attributes = zmi.getAttributes();
        attributes.add("foo", new ValueInt(42l));
        serializationTest(zmi);
    }

    @Test
    public void testSimpleSerialization2() {
        ZMI zmi = new ZMI();
        AttributesMap attributes = zmi.getAttributes();
        attributes.add("foo", new ValueInt(42l));
        attributes.add("bar", new ValueDuration("+1 11:43:45.342"));
        serializationTest(zmi);
    }

    @Test
    public void testHierarchySerialization() {
        ZMI root = new ZMI();

        AttributesMap rootAttributes = root.getAttributes();
        rootAttributes.add("foo", new ValueInt(42l));
        rootAttributes.add("bar", new ValueDuration("+1 11:43:45.342"));

        ZMI son1 = new ZMI(root);
        root.addSon(son1);

        AttributesMap son1Attributes = son1.getAttributes();
        son1Attributes.add("foo", new ValueInt(43l));
        son1Attributes.add("bar", new ValueDuration("+1 12:43:47.342"));

        ZMI son2 = new ZMI(root);
        root.addSon(son2);

        AttributesMap son2Attributes = son2.getAttributes();
        son2Attributes.add("foo", new ValueInt(47l));
        son2Attributes.add("bar", new ValueDuration("+1 15:45:43.342"));

        ZMI grandson1 = new ZMI(son1);
        son1.addSon(grandson1);

        AttributesMap grandson1Attributes = grandson1.getAttributes();
        grandson1Attributes.add("foo", new ValueInt(52l));
        grandson1Attributes.add("bar", new ValueDuration("-2 15:45:43.342"));

        serializationTest(root);
        serializationTest(son1);
        serializationTest(grandson1);
    }

    private void serializationTest(ZMI zmi) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        zmi.serialize(out);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ZMI zmi2 = ZMI.deserialize(in);

        assertEquals(zmi.toString(), zmi2.toString());
    }
}
