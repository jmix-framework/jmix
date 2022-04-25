package io.jmix.flowui.data;

import io.jmix.core.common.event.Subscription;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Data component holding a typed value.
 */
public interface ValueSource<V> extends DataUnit, HasType<V> {

    @Nullable
    V getValue();

    void setValue(@Nullable V value);

    boolean isReadOnly();

    /**
     * Registers a new value change listener.
     *
     * @param listener the listener to be added
     * @return a registration object for removing an event listener added to a source
     */
    Subscription addValueChangeListener(Consumer<ValueChangeEvent<V>> listener);

    /**
     * An event that is fired when value of source is changed.
     *
     * @param <V> value type
     */
    class ValueChangeEvent<V> extends EventObject {
        private final V prevValue;
        private final V value;

        public ValueChangeEvent(ValueSource<V> source, @Nullable V prevValue, @Nullable V value) {
            super(source);
            this.prevValue = prevValue;
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        @Override
        public ValueSource<V> getSource() {
            return (ValueSource<V>) super.getSource();
        }

        @Nullable
        public V getPrevValue() {
            return prevValue;
        }

        @Nullable
        public V getValue() {
            return value;
        }
    }
}
