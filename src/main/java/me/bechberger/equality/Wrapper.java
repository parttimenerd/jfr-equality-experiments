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
 * @param object
 */
public record Wrapper(RecordedObject object) {

    private static final Field objectContextField;
    private static final Field objectsField;

    static {
        try {
            objectContextField = RecordedObject.class.getDeclaredField("objectContext");
            objectContextField.setAccessible(true);
            objectsField = RecordedObject.class.getDeclaredField("objects");
            objectsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        try {
            return objectContextField.get(object).hashCode() ^ objectsField.get(object).hashCode();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        try {
            return this == obj || (obj instanceof Wrapper other && (
                    (objectContextField.get(object).equals(objectContextField.get(other.object)) &&
                     objectsField.get(object).equals(objectsField.get(other.object)))
                    || object.equals(other.object)
            ));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}