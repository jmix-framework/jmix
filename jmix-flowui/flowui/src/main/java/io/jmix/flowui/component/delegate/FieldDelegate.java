package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.AbstractField;
import io.jmix.flowui.data.binding.impl.AbstractValueBinding;
import io.jmix.flowui.data.binding.impl.FieldValueBinding;
import io.jmix.flowui.data.ValueSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @param <C> component type
 * @param <T> value source value type
 * @param <V> component value type
 */
@Component("flowui_FieldDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FieldDelegate<C extends AbstractField<?, V>, T, V> extends AbstractFieldDelegate<C, T, V> {

    public FieldDelegate(C component) {
        super(component);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractValueBinding<T> createValueBinding(ValueSource<T> valueSource) {
        return applicationContext.getBean(FieldValueBinding.class, valueSource, component);
    }
}
