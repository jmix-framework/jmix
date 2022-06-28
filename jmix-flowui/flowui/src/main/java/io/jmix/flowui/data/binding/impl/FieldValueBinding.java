package io.jmix.flowui.data.binding.impl;

import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.SupportsTypedValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("flowui_FieldValueBinding")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FieldValueBinding<V> extends AbstractValueBinding<V> {

    public FieldValueBinding(ValueSource<V> valueSource, HasValue<?, V> component) {
        super(valueSource, component);
    }

    @Nullable
    @Override
    protected V getComponentValue() {
        return component instanceof SupportsTypedValue
                ? ((SupportsTypedValue<?, ?, V, ?>) component).getTypedValue()
                : component.getValue();
    }

    @Override
    protected void setComponentValue(@Nullable V value) {
        if (component instanceof SupportsTypedValue) {
            ((SupportsTypedValue<?, ?, V, ?>) component).setTypedValue(value);
        } else {
            component.setValue(value);
        }
    }
}
