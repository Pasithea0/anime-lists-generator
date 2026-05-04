package net.fribbtastic.coding.animelistsgenerator.models;

import lombok.Getter;

/**
 * @author Frederic Eßer
 */
@Getter
public class SourceId {

    private final Object value;

    public SourceId(Integer value) {
        this.value = value;
    }

    public SourceId(String value) {
        this.value = value;
    }

    public String asString() {
        return String.valueOf(this.value);
    }

    public Integer asInteger() {
        return (value instanceof Integer) ? (Integer) value : null;
    }

    @Override
    public String toString() {
        return this.asString();
    }
}
