package io.jmix.flowui.component.valuepicker;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.textfield.HasPrefixAndSuffix;
import com.vaadin.flow.shared.Registration;


public class ValuePicker<V> extends ValuePickerBase<ValuePicker<V>, V> implements HasPrefixAndSuffix {

    @Override
    protected void initComponent() {
        super.initComponent();
        initFieldValuePropertyChangeListener();
    }

    public boolean isAllowCustomValue() {
        return super.isAllowCustomValueBoolean();
    }

    @Override
    public void setAllowCustomValue(boolean allowCustomValue) {
        super.setAllowCustomValue(allowCustomValue);
    }

    @Override
    public Registration addCustomValueSetListener(ComponentEventListener<CustomValueSetEvent<ValuePicker<V>, V>> listener) {
        return super.addCustomValueSetListener(listener);
    }
}
