package pl.edu.mimuw.cloudatlas.model;

public class ValueUtils {
    public static boolean valueNonNullOfType(Value value, Type type) {
        return value != null && !value.isNull() && value.getType().isCompatible(type);
    }
}
