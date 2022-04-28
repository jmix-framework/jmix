package io.jmix.flowui.component.delegate;

import io.jmix.flowui.data.binding.impl.AbstractValueBinding;
import io.jmix.flowui.data.binding.impl.FieldValueBinding;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("flowui_DateTimePickerDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DateTimePickerDelegate<V extends Comparable<V>>
        extends AbstractDateTimeFieldDelegate<TypedDateTimePicker<V>, V, LocalDateTime> {

    public DateTimePickerDelegate(TypedDateTimePicker<V> component) {
        super(component);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractValueBinding<V> createValueBinding(ValueSource<V> valueSource) {
        return applicationContext.getBean(FieldValueBinding.class, valueSource, component);
    }
}
