package pl.edu.mimuw.cloudatlas.interpreter;

import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class InterpreterTests {
    @Test
    public void modifiesZmi() throws Exception {
        ZMI root = Main.createTestHierarchy();
        InputStream in = new ByteArrayInputStream("SELECT epoch() AS timestamp".getBytes("UTF-8"));
        ByteArrayOutputStream outByteArray = new ByteArrayOutputStream();
        PrintStream outPrint = new PrintStream(outByteArray);
        Main.runTest(in, outPrint, root);

        AttributesMap rootAttributes = root.getAttributes();
        assertEquals(new ValueTime("2000/01/01 00:00:00.000"), rootAttributes.get("timestamp"));

        for (ZMI son : root.getSons()) {
            AttributesMap sonAttributes = son.getAttributes();
            assertEquals(new ValueTime("2000/01/01 00:00:00.000"), sonAttributes.get("timestamp"));
        }
    }

    @Test
    public void fileTest01() throws Exception {
        runFileTest(1);
    }

    @Test
    public void fileTest02() throws Exception {
        runFileTest(2);
    }

    @Test
    public void fileTest03() throws Exception {
        runFileTest(3);
    }

    @Test
    public void fileTest04() throws Exception {
        runFileTest(4);
    }

    @Test
    public void fileTest05() throws Exception {
        runFileTest(5);
    }

    @Test
    public void fileTest06() throws Exception {
        runFileTest(6);
    }

    @Test
    public void fileTest07() throws Exception {
        runFileTest(7);
    }

    @Test
    public void fileTest08() throws Exception {
        runFileTest(8);
    }

    @Test
    public void fileTest09() throws Exception {
        runFileTest(9);
    }

    @Test
    public void fileTest10() throws Exception {
        runFileTest(10);
    }

    @Test
    public void fileTest11() throws Exception {
        runFileTest(11);
    }

    @Ignore
    @Test
    public void fileTest12() throws Exception {
        runFileTest(12);
    }

    @Test
    public void fileTest13() throws Exception {
        runFileTest(13);
    }

    @Test
    public void fileTest14() throws Exception {
        runFileTest(14);
    }

    @Test
    public void fileTest15() throws Exception {
        runFileTest(15);
    }

    @Test
    public void fileTest16() throws Exception {
        runFileTest(16);
    }

    @Test
    public void fileTest17() throws Exception {
        runFileTest(17);
    }

    @Test
    public void fileTest18() throws Exception {
        runFileTest(18);
    }

    @Test
    public void fileTest19() throws Exception {
        runFileTest(19);
    }

    private void runFileTest(int i) throws Exception {
        URL test = InterpreterTests.class.getResource(i + ".in");
        URL testOut = InterpreterTests.class.getResource(i + ".out");

        FileInputStream in = new FileInputStream(test.getFile());
        ByteArrayOutputStream outByteArray = new ByteArrayOutputStream();
        PrintStream outPrint = new PrintStream(outByteArray);

        ZMI root = Main.createTestHierarchy();
        Main.runTest(in, outPrint, root);

        String actual = outByteArray.toString();

        File expectedFile = new File(testOut.getFile());
        FileInputStream expectedIn = new FileInputStream(expectedFile);
        byte[] buffer = new byte[(int)expectedFile.length()];
        expectedIn.read(buffer);
        String expected = new String(buffer, "UTF-8");

        assertEquals(expected, actual);
    }
}
