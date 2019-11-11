package pl.edu.mimuw.cloudatlas.interpreter;

import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.net.URL;

import static org.junit.Assert.*;
import org.junit.Test;

public class InterpreterTests {
    @Test
    public void fileTest1() throws Exception {
        runFileTest(1);
    }

    @Test
    public void fileTest2() throws Exception {
        runFileTest(2);
    }

    @Test
    public void fileTest3() throws Exception {
        runFileTest(3);
    }

    @Test
    public void fileTest4() throws Exception {
        runFileTest(4);
    }

    @Test
    public void fileTest5() throws Exception {
        runFileTest(5);
    }

    @Test
    public void fileTest6() throws Exception {
        runFileTest(6);
    }

    @Test
    public void fileTest7() throws Exception {
        runFileTest(7);
    }

    @Test
    public void fileTest8() throws Exception {
        runFileTest(8);
    }

    @Test
    public void fileTest9() throws Exception {
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
        Main.runTest(in, outPrint);
        String actual = outByteArray.toString();

        File expectedFile = new File(testOut.getFile());
        FileInputStream expectedIn = new FileInputStream(expectedFile);
        byte[] buffer = new byte[(int)expectedFile.length()];
        expectedIn.read(buffer);
        String expected = new String(buffer, "UTF-8");

        assertEquals(expected, actual);
    }
}
