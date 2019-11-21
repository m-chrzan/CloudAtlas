package pl.edu.mimuw.cloudatlas.interpreter;

import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Calendar;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
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
    public void testAvg() throws Exception {
        assertInterpreterRun(
                "SELECT avg(cpu_usage) AS cpu_usage",
                new String[] {
                    "/uw: cpu_usage: 0.5",
                    "/pjwstk: cpu_usage: 0.25",
                    "/: cpu_usage: 0.375",
                }
        );
    }

    @Test
    public void testCount() throws Exception {
        assertInterpreterRun(
                "SELECT count(cpu_usage) AS cpu_usage",
                new String[] {
                    "/uw: cpu_usage: 2",
                    "/pjwstk: cpu_usage: 2",
                    "/: cpu_usage: 2",
                }
        );
    }

    @Test
    public void testSum() throws Exception {
        assertInterpreterRun(
                "SELECT sum(cpu_usage) AS cpu_usage",
                new String[] {
                    "/uw: cpu_usage: 1.0",
                    "/pjwstk: cpu_usage: 0.5",
                    "/: cpu_usage: 1.5",
                }
        );
    }

    @Test
    public void testFirst() throws Exception {
        assertInterpreterRun(
                "SELECT first(1, unfold(php_modules)) AS php_modules",
                new String[] {
                    "/pjwstk: php_modules: [rewrite]",
                    "/: php_modules: [rewrite]",
                }
        );
    }

    @Test
    public void testLast() throws Exception {
        assertInterpreterRun(
                "SELECT last(1, unfold(php_modules)) AS php_modules",
                new String[] {
                    "/pjwstk: php_modules: [odbc]",
                    "/: php_modules: [odbc]",
                }
        );
    }

    @Test
    public void testRandom() throws Exception {
        String query = "SELECT random(1, unfold(php_modules)) AS php_modules";
        ByteArrayInputStream in = new ByteArrayInputStream(query.getBytes());
        String output = runInterpreter(in);
        String expected1 = join(new String[] {
                    "/pjwstk: php_modules: [odbc]",
                    "/: php_modules: [odbc]",
        });
        String expected2 = join(new String[] {
                    "/pjwstk: php_modules: [rewrite]",
                    "/: php_modules: [rewrite]",
        });

        assertTrue(output.equals(expected1) || output.equals(expected2));
    }

    @Test
    public void testMin() throws Exception {
        assertInterpreterRun(
                "SELECT min(cpu_usage) AS cpu_usage",
                new String[] {
                    "/uw: cpu_usage: 0.1",
                    "/pjwstk: cpu_usage: 0.1",
                    "/: cpu_usage: 0.1",
                }
        );
    }

    @Test
    public void testMax() throws Exception {
        assertInterpreterRun(
                "SELECT max(cpu_usage) AS cpu_usage",
                new String[] {
                    "/uw: cpu_usage: 0.9",
                    "/pjwstk: cpu_usage: 0.4",
                    "/: cpu_usage: 0.9",
                }
        );
    }

    @Test
    public void testLand() throws Exception {
        assertInterpreterRun(
                "SELECT max(cpu_usage) AS cpu_usage; SELECT land(cpu_usage < 0.5) AS low_cpu",
                new String[] {
                    "/uw: cpu_usage: 0.9",
                    "/uw: low_cpu: false",
                    "/pjwstk: cpu_usage: 0.4",
                    "/pjwstk: low_cpu: true",
                    "/: cpu_usage: 0.9",
                    "/: low_cpu: false",
                }
        );
    }

    @Test
    public void testLor() throws Exception {
        assertInterpreterRun(
                "SELECT max(cpu_usage) AS cpu_usage; SELECT lor(cpu_usage > 0.5) AS high_cpu",
                new String[] {
                    "/uw: cpu_usage: 0.9",
                    "/uw: high_cpu: true",
                    "/pjwstk: cpu_usage: 0.4",
                    "/pjwstk: high_cpu: false",
                    "/: cpu_usage: 0.9",
                    "/: high_cpu: true",
                }
        );
    }

    @Test
    public void testNow() throws Exception {
        ValueTime timeBefore = new ValueTime(Calendar.getInstance().getTimeInMillis());
        String query = "SELECT now() AS now";
        ByteArrayInputStream in = new ByteArrayInputStream(query.getBytes());
        String output = runInterpreter(in);
        ValueTime timeAfter = new ValueTime(Calendar.getInstance().getTimeInMillis());
        String[] lines = output.split("\n");
        assertEquals(3, lines.length);
        for (String line : lines) {
            String timestamp = line.split(":", 3)[2];
            ValueTime resultTime = new ValueTime(timestamp);
            assertFalse(((ValueBoolean) resultTime.isLowerThan(timeBefore)).getValue());
            assertFalse(((ValueBoolean) timeAfter.isLowerThan(resultTime)).getValue());
        }
    }

    private void assertInterpreterRun(String query, String[] expectedOutput) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(query.getBytes());

        String expected = join(expectedOutput);

        runTest(in, expected);
    }

    private String join(String[] strings) {
        return String.join("\n", strings) + "\n";
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

        File expectedFile = new File(testOut.getFile());
        FileInputStream expectedIn = new FileInputStream(expectedFile);
        byte[] buffer = new byte[(int)expectedFile.length()];
        expectedIn.read(buffer);
        String expected = new String(buffer, "UTF-8");

        runTest(in, expected);
    }

    private String runInterpreter(InputStream in) throws Exception {
        ByteArrayOutputStream outByteArray = new ByteArrayOutputStream();
        PrintStream outPrint = new PrintStream(outByteArray);

        ZMI root = Main.createTestHierarchy();
        Main.runTest(in, outPrint, root);

        return outByteArray.toString();
    }

    private void runTest(InputStream in, String expected) throws Exception {
        String actual = runInterpreter(in);

        assertEquals(expected, actual);
    }
}
