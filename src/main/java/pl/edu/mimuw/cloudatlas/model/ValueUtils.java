package pl.edu.mimuw.cloudatlas.model;

import java.util.List;
import java.util.Iterator;

public class ValueUtils {
    public static boolean valueNonNullOfType(Value value, Type type) {
        return value != null && !value.isNull() && value.getType().isCompatible(type);
    }

    public static boolean valueLower(Value a, Value b) {
        return ((ValueBoolean) a.isLowerThan(b)).getValue();
    }

    public static ValueTime currentTime() {
        return new ValueTime(System.currentTimeMillis());
    }

    public static ValueTime addToTime(ValueTime time, long millis) {
        return time.addValue(new ValueDuration(millis));
    }

    public static boolean isPrefix(PathName prefix, PathName path) {
        List<String> prefixComponents = prefix.getComponents();
        List<String> pathComponents = path.getComponents();

        if (prefixComponents.size() > pathComponents.size()) {
            return false;
        }

        Iterator<String> prefixIterator = prefixComponents.iterator();
        Iterator<String> pathIterator = pathComponents.iterator();

        while (prefixIterator.hasNext()) {
            if (!prefixIterator.next().equals(pathIterator.next())) {
                return false;
            }
        }
        return true;
    }
}
