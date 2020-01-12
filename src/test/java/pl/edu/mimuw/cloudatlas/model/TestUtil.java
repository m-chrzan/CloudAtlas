package pl.edu.mimuw.cloudatlas.model;

public class TestUtil {
    public static <T> int iterableSize(Iterable<T> iterable) {
        int count = 0;
        for (T attribute : iterable) {
            count++;
        }

        return count;
    }
}
