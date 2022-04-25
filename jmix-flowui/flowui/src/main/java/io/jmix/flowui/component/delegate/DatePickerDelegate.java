package io.jmix.flowui.component.delegate;

import io.jmix.flowui.data.binding.impl.AbstractValueBinding;
import io.jmix.flowui.data.binding.impl.FieldValueBinding;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component("flowui_DatePickerDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DatePickerDelegate<V extends Comparable<V>>
        extends AbstractDateTimeFieldDelegate<TypedDatePicker<V>, V, LocalDate> {

    public DatePickerDelegate(TypedDatePicker<V> component) {
        super(component);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractValueBinding<V> createValueBinding(ValueSource<V> valueSource) {
        return applicationContext.getBean(FieldValueBinding.class, valueSource, component);
    }
}
