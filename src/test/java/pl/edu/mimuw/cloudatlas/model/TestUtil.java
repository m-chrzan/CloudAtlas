package pl.edu.mimuw.cloudatlas.model;

public class TestUtil {
    public static <T> int iterableSize(Iterable<T> iterable) {
        int count = 0;
        for (T attribute : iterable) {
            count++;
        }

        return count;
    }

    public static ValueTime addToTime(ValueTime time, long millis) {
        return time.addValue(new ValueDuration(millis));
    }
}
