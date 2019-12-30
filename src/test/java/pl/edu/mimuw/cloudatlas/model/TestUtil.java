package pl.edu.mimuw.cloudatlas.model;

public class TestUtil {
    public static boolean valueLower(Value a, Value b) {
        return ((ValueBoolean) a.isLowerThan(b)).getValue();
    }

    public static <T> int iterableSize(Iterable<T> iterable) {
        int count = 0;
        for (T attribute : iterable) {
            count++;
        }

        return count;
    }
}
