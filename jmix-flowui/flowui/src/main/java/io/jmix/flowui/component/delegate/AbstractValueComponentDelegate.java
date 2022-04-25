package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.data.binding.ValueBinding;
import io.jmix.flowui.data.binding.impl.AbstractValueBinding;
import io.jmix.flowui.data.ValueSource;

import javax.annotation.Nullable;

public abstract class AbstractValueComponentDelegate<C extends Component & HasValue<?, V>, T, V>
        extends AbstractComponentDelegate<C> {

    protected ValueBinding<T> valueBinding;

    public AbstractValueComponentDelegate(C component) {
        super(component);
    }

    @Nullable
    public ValueSource<T> getValueSource() {
        return valueBinding != null ? valueBinding.getValueSource() : null;
    }

    public void setValueSource(@Nullable ValueSource<T> valueSource) {
        if (valueBinding != null) {
            valueBinding.unbind();
            valueBinding = null;
        }

        if (valueSource != null) {
            valueBinding = createValueBinding(valueSource);
            valueBinding.bind();

            valueBinding.activate();
        }
    }

    protected abstract AbstractValueBinding<T> createValueBinding(ValueSource<T> valueSource);
}
