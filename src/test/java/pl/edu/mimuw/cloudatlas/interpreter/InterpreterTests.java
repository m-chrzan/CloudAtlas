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

    @Test
    public void testEpoch() throws Exception {
        assertInterpreterRun(
                "SELECT epoch() AS epoch",
                new String[] {
                    "/uw: epoch: 2000/01/01 00:00:00.000",
                    "/pjwstk: epoch: 2000/01/01 00:00:00.000",
                    "/: epoch: 2000/01/01 00:00:00.000",
                }
        );
    }

    @Test
    public void testSize() throws Exception {
        assertInterpreterRun(
                "SELECT max(size(name)) AS max_len_name",
                new String[] {
                    "/uw: max_len_name: 8",
                    "/pjwstk: max_len_name: 10",
                    "/: max_len_name: 6",
                }
        );
    }

    @Test
    public void testRound() throws Exception {
        assertInterpreterRun(
                "SELECT max(cpu_usage) AS cpu_usage; SELECT round(max(cpu_usage)) AS approximate_cpu",
                new String[] {
                    "/uw: cpu_usage: 0.9",
                    "/uw: approximate_cpu: 1.0",
                    "/pjwstk: cpu_usage: 0.4",
                    "/pjwstk: approximate_cpu: 0.0",
                    "/: cpu_usage: 0.9",
                    "/: approximate_cpu: 1.0",
                }
        );
    }

    @Test
    public void testFloor() throws Exception {
        assertInterpreterRun(
                "SELECT max(cpu_usage) AS cpu_usage; SELECT floor(0.2 + max(cpu_usage)) AS approximate_cpu",
                new String[] {
                    "/uw: cpu_usage: 0.9",
                    "/uw: approximate_cpu: 1.0",
                    "/pjwstk: cpu_usage: 0.4",
                    "/pjwstk: approximate_cpu: 0.0",
                    "/: cpu_usage: 0.9",
                    "/: approximate_cpu: 1.0",
                }
        );
    }

    @Test
    public void testCeil() throws Exception {
        assertInterpreterRun(
                "SELECT max(cpu_usage) AS cpu_usage; SELECT ceil(0.2 + max(cpu_usage)) AS approximate_cpu",
                new String[] {
                    "/uw: cpu_usage: 0.9",
                    "/uw: approximate_cpu: 2.0",
                    "/pjwstk: cpu_usage: 0.4",
                    "/pjwstk: approximate_cpu: 1.0",
                    "/: cpu_usage: 0.9",
                    "/: approximate_cpu: 2.0",
                }
        );
    }

    @Test
    public void testBoolToString() throws Exception {
        assertInterpreterRun(
                "SELECT to_string(true) + \"x\" AS truex",
                new String[] {
                    "/uw: truex: truex",
                    "/pjwstk: truex: truex",
                    "/: truex: truex",
                }
        );
    }

    @Test
    public void testStringToBool() throws Exception {
        assertInterpreterRun(
                "SELECT to_boolean(\"true\") AND true AS tru",
                new String[] {
                    "/uw: tru: true",
                    "/pjwstk: tru: true",
                    "/: tru: true",
                }
        );
    }

    @Test
    public void testIntToString() throws Exception {
        assertInterpreterRun(
                "SELECT to_string(1) + \"x\" AS onex",
                new String[] {
                    "/uw: onex: 1x",
                    "/pjwstk: onex: 1x",
                    "/: onex: 1x",
                }
        );
    }

    @Test
    public void testStringToInt() throws Exception {
        assertInterpreterRun(
                "SELECT to_integer(\"1\") + 1 AS two",
                new String[] {
                    "/uw: two: 2",
                    "/pjwstk: two: 2",
                    "/: two: 2",
                }
        );
    }

    @Test
    public void testDoublePlusIntFails() throws Exception {
        assertInterpreterRun(
                "SELECT 1 + 1.0 AS two",
                new String[] {
                }
        );
    }

    @Test
    public void testDoubleToInt() throws Exception {
        assertInterpreterRun(
                "SELECT 1 + to_integer(1.0) AS two",
                new String[] {
                    "/uw: two: 2",
                    "/pjwstk: two: 2",
                    "/: two: 2",
                }
        );
    }

    @Test
    public void testIntToDouble() throws Exception {
        assertInterpreterRun(
                "SELECT to_double(1) + 1.0 AS two",
                new String[] {
                    "/uw: two: 2.0",
                    "/pjwstk: two: 2.0",
                    "/: two: 2.0",
                }
        );
    }

    @Test
    public void testIntToDuration() throws Exception {
        assertInterpreterRun(
                "SELECT to_duration(1) AS one_ms",
                new String[] {
                    "/uw: one_ms: +0 00:00:00.001",
                    "/pjwstk: one_ms: +0 00:00:00.001",
                    "/: one_ms: +0 00:00:00.001",
                }
        );
    }

    @Test
    public void testDurationToInt() throws Exception {
        assertInterpreterRun(
                "SELECT to_integer(to_duration(1)) AS one",
                new String[] {
                    "/uw: one: 1",
                    "/pjwstk: one: 1",
                    "/: one: 1",
                }
        );
    }

    @Test
    public void testDoubleToString() throws Exception {
        assertInterpreterRun(
                "SELECT to_string(1.0) + \"x\" AS onex",
                new String[] {
                    "/uw: onex: 1.0x",
                    "/pjwstk: onex: 1.0x",
                    "/: onex: 1.0x",
                }
        );
    }

    @Test
    public void testStringToDouble() throws Exception {
        assertInterpreterRun(
                "SELECT to_double(\"1.0\") + 1.0 AS two",
                new String[] {
                    "/uw: two: 2.0",
                    "/pjwstk: two: 2.0",
                    "/: two: 2.0",
                }
        );
    }

    @Test
    public void testSetToList() throws Exception {
        assertInterpreterRun(
                "SELECT first(5, unfold(to_list(php_modules) + to_list(php_modules))) AS php_modules",
                new String[] {
                    "/pjwstk: php_modules: [rewrite, rewrite, odbc, odbc]",
                    "/: php_modules: [rewrite, rewrite, odbc, odbc, rewrite]"
                }
        );
    }

    @Test
    public void testListToSet() throws Exception {
        assertInterpreterRun(
                "SELECT to_set(first(5, unfold(to_list(php_modules) + to_list(php_modules)))) AS php_modules",
                new String[] {
                    "/pjwstk: php_modules: {odbc, rewrite}",
                    "/: php_modules: {odbc, rewrite}"
                }
        );
    }

    @Test
    public void testTimeToString() throws Exception {
        assertInterpreterRun(
                "SELECT max(to_string(timestamp)) + \"x\" AS timex",
                new String[] {
                    "/uw: timex: 2012/11/09 21:03:00.000x",
                    "/pjwstk: timex: 2012/11/09 21:13:00.000x",
                    "/: timex: 2012/11/09 20:08:13.123x"
                }
        );
    }

    @Test
    public void testStringToTime() throws Exception {
        assertInterpreterRun(
                "SELECT to_time(\"2012/11/09 21:03:00.000\") + to_duration(1) AS time",
                new String[] {
                    "/uw: time: 2012/11/09 21:03:00.001",
                    "/pjwstk: time: 2012/11/09 21:03:00.001",
                    "/: time: 2012/11/09 21:03:00.001"
                }
        );
    }

    @Test
    public void testDurationToString() throws Exception {
        assertInterpreterRun(
                "SELECT to_string(epoch() - epoch()) + \"x\" AS zerox",
                new String[] {
                    "/uw: zerox: +0 00:00:00.000x",
                    "/pjwstk: zerox: +0 00:00:00.000x",
                    "/: zerox: +0 00:00:00.000x"
                }
        );
    }

    @Test
    public void testStringToDuration() throws Exception {
        assertInterpreterRun(
                "SELECT to_duration(\"+0 00:01:12.000\") + to_duration(1) AS dur",
                new String[] {
                    "/uw: dur: +0 00:01:12.001",
                    "/pjwstk: dur: +0 00:01:12.001",
                    "/: dur: +0 00:01:12.001"
                }
        );
    }

    @Test
    public void testContactToString() throws Exception {
        assertInterpreterRun(
                "SELECT first(1, to_string(unfold(members))) AS member",
                new String[] {
                    "/uw: member: [(/uw/violet07, /10.1.1.10)]",
                    "/pjwstk: member: [(/uw/whatever01, /82.111.52.56)]",
                }
        );
    }

    @Test
    public void testListToString() throws Exception {
        assertInterpreterRun(
                "SELECT first(1, to_string(to_list(php_modules)) + \"x\") AS listx",
                new String[] {
                    "/pjwstk: listx: [[rewrite]x]"
                }
        );
    }

    @Test
    public void testSetToString() throws Exception {
        assertInterpreterRun(
                "SELECT first(1, to_string(to_set(php_modules)) + \"x\") AS setx",
                new String[] {
                    "/pjwstk: setx: [{rewrite}x]"
                }
        );
    }

    @Test
    public void testBinopOnListResultFails() throws Exception {
        assertInterpreterRun(
                "SELECT max(distinct(cardinality) + num_cores) AS x",
                new String[] {
                }
        );
    }

    @Test
    public void testAggregateResultList() throws Exception {
        assertInterpreterRun(
                "SELECT sum(cardinality) AS cardinality; SELECT sum(distinct(cardinality)) AS dc",
                new String[] {
                    "/uw: cardinality: 3",
                    "/uw: dc: 1",
                    "/pjwstk: cardinality: 2",
                    "/pjwstk: dc: 1",
                    "/: cardinality: 5",
                    "/: dc: 5"
                }
        );
    }

    @Test
    public void testEq() throws Exception {
        assertInterpreterRun(
                "SELECT 1 = 1 AS oeo, 1 = 0 AS oez",
                new String[] {
                    "/uw: oeo: true",
                    "/uw: oez: false",
                    "/pjwstk: oeo: true",
                    "/pjwstk: oez: false",
                    "/: oeo: true",
                    "/: oez: false"
                }
        );
    }

    @Test
    public void testNeq() throws Exception {
        assertInterpreterRun(
                "SELECT 1 <> 1 AS ono, 1 <> 0 AS onz",
                new String[] {
                    "/uw: ono: false",
                    "/uw: onz: true",
                    "/pjwstk: ono: false",
                    "/pjwstk: onz: true",
                    "/: ono: false",
                    "/: onz: true"
                }
        );
    }

    @Test
    public void testLt() throws Exception {
        assertInterpreterRun(
                "SELECT 0 < 1 AS zlo, 1 < 1 AS olo, 1 < 0 AS olz",
                new String[] {
                    "/uw: zlo: true",
                    "/uw: olo: false",
                    "/uw: olz: false",
                    "/pjwstk: zlo: true",
                    "/pjwstk: olo: false",
                    "/pjwstk: olz: false",
                    "/: zlo: true",
                    "/: olo: false",
                    "/: olz: false",
                }
        );
    }

    @Test
    public void testLeq() throws Exception {
        assertInterpreterRun(
                "SELECT 0 <= 1 AS zlo, 1 <= 1 AS olo, 1 <= 0 AS olz",
                new String[] {
                    "/uw: zlo: true",
                    "/uw: olo: true",
                    "/uw: olz: false",
                    "/pjwstk: zlo: true",
                    "/pjwstk: olo: true",
                    "/pjwstk: olz: false",
                    "/: zlo: true",
                    "/: olo: true",
                    "/: olz: false",
                }
        );
    }

    @Test
    public void testGeq() throws Exception {
        assertInterpreterRun(
                "SELECT 0 >= 1 AS zgo, 1 >= 1 AS ogo, 1 >= 0 AS ogz",
                new String[] {
                    "/uw: zgo: false",
                    "/uw: ogo: true",
                    "/uw: ogz: true",
                    "/pjwstk: zgo: false",
                    "/pjwstk: ogo: true",
                    "/pjwstk: ogz: true",
                    "/: zgo: false",
                    "/: ogo: true",
                    "/: ogz: true",
                }
        );
    }

    @Test
    public void testGt() throws Exception {
        assertInterpreterRun(
                "SELECT 0 > 1 AS zgo, 1 > 1 AS ogo, 1 > 0 AS ogz",
                new String[] {
                    "/uw: zgo: false",
                    "/uw: ogo: false",
                    "/uw: ogz: true",
                    "/pjwstk: zgo: false",
                    "/pjwstk: ogo: false",
                    "/pjwstk: ogz: true",
                    "/: zgo: false",
                    "/: ogo: false",
                    "/: ogz: true",
                }
        );
    }

    @Test
    public void testNullFromEmptyColumn() throws Exception {
        assertInterpreterRun(
                "SELECT max(num_cores) AS num_cores WHERE cpu_usage = 0.1",
                new String[] {
                    "/uw: num_cores: NULL",
                    "/pjwstk: num_cores: 7",
                    "/: num_cores: NULL"
                }
        );
    }

    @Test
    public void testOrderDescending() throws Exception {
        assertInterpreterRun(
                "SELECT first(1, unfold(php_modules)) AS php_modules ORDER BY cpu_usage DESC NULLS LAST",
                new String[] {
                    "/pjwstk: php_modules: [odbc]",
                    "/: php_modules: [odbc]",
                }
        );
    }

    @Test
    public void testOrderAscending() throws Exception {
        assertInterpreterRun(
                "SELECT first(1, unfold(php_modules)) AS php_modules ORDER BY cpu_usage ASC NULLS LAST",
                new String[] {
                    "/pjwstk: php_modules: [rewrite]",
                    "/: php_modules: [rewrite]"
                }
        );
    }

    @Test
    public void testOrderNullsFirst() throws Exception {
        assertInterpreterRun(
                "SELECT first(1, unfold(some_names)) AS some_names ORDER BY has_ups NULLS FIRST",
                new String[] {
                    "/uw: some_names: [tola]",
                    "/: some_names: [tola]"
                }
        );
    }

    @Test
    public void testOrderNullsLast() throws Exception {
        assertInterpreterRun(
                "SELECT last(1, unfold(some_names)) AS some_names ORDER BY has_ups NULLS LAST",
                new String[] {
                    "/uw: some_names: [tosia]",
                    "/: some_names: [tosia]"
                }
        );
    }

    private void assertInterpreterRun(String query, String[] expectedOutput) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(query.getBytes());

        String expected = join(expectedOutput);

        runTest(in, expected);
    }

    private String join(String[] strings) {
        if (strings.length == 0) {
            return "";
        }

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
