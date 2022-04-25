package io.jmix.flowui.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;

import javax.annotation.Nullable;

public interface SupportsTypedValue<C extends Component, E extends HasValue.ValueChangeEvent<P>, V, P> extends HasValue<E, P> {

    @Nullable
    V getTypedValue();

    void setTypedValue(@Nullable V value);

    Registration addTypedValueChangeListener(ComponentEventListener<TypedValueChangeEvent<C, V>> listener);

    class TypedValueChangeEvent<C extends Component, V> extends ComponentEvent<C> {

        protected V value;
        protected V oldValue;

        public TypedValueChangeEvent(C source, @Nullable V value, @Nullable V oldValue, boolean fromClient) {
            super(source, fromClient);

            this.value = value;
            this.oldValue = oldValue;
        }

        @Nullable
        public V getValue() {
            return value;
        }

        @Nullable
        public V getOldValue() {
            return oldValue;
        }
    }
}
