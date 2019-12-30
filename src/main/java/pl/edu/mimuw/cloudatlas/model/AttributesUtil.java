package pl.edu.mimuw.cloudatlas.model;

import java.util.Iterator;
import java.util.Map.Entry;

public class AttributesUtil {
    public static void transferAttributes(AttributesMap fromAttributes, AttributesMap toAttributes) {
        Iterator<Entry<Attribute, Value>> iterator = toAttributes.iterator();
        while (iterator.hasNext()) {
            Entry<Attribute, Value> entry = iterator.next();
            Attribute attribute = entry.getKey();
            Value newValue = fromAttributes.getOrNull(attribute);
            if (newValue == null) {
                iterator.remove();
            }
        }
        for (Entry<Attribute, Value> entry : fromAttributes) {
            toAttributes.addOrChange(entry.getKey(), entry.getValue());
        }
    }
}
