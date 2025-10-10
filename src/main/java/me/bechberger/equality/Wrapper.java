package me.bechberger.equality;

import jdk.jfr.consumer.RecordedObject;

import java.lang.reflect.Field;

/**
 * Wrapper for {@link RecordedObject} that implements {@link Object#hashCode()} and {@link Object#equals(Object)}
 * properly
 * <p>
 * Problem: There is no hashCode/equals implementation for RecordedObject that takes the actual content
 * into account. There the caching doesn't work properly.
 * <p>
 * Downside of this approach: Users need to pass <code>--add-opens jdk.jfr/jdk.jfr.consumer=ALL-UNNAMED</code>
 * to the JVM to allow access to the package private fields.
 * <p/>
 * Also: Objects with the same content but different types are considered equal.
 */
public record Wrapper(RecordedObject object) {

    private static final Field objectsField;

    static {
        try {
            objectsField = RecordedObject.class.getDeclaredField("objects");
            objectsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        try {
            return objectsField.get(object).hashCode();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        try {
            return this == obj || (obj instanceof Wrapper other && (
                    object == other.object ||
                     objectsField.get(object).equals(objectsField.get(other.object)))
            );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}