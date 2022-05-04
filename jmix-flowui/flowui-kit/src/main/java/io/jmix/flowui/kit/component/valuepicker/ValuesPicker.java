package io.jmix.flowui.kit.component.valuepicker;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.textfield.HasPrefixAndSuffix;
import com.vaadin.flow.shared.Registration;

import java.util.Collection;
import java.util.Collections;

public class ValuesPicker<V> extends ValuePickerBase<ValuesPicker<V>, Collection<V>>
        implements HasPrefixAndSuffix {

    public boolean isAllowCustomValue() {
        return super.isAllowCustomValueBoolean();
    }

    @Override
    public void setAllowCustomValue(boolean allowCustomValue) {
        super.setAllowCustomValue(allowCustomValue);
    }

    @Override
    public Registration addCustomValueSetListener(ComponentEventListener<CustomValueSetEvent<ValuesPicker<V>, Collection<V>>> listener) {
        return super.addCustomValueSetListener(listener);
    }

    @Override
    public Collection<V> getEmptyValue() {
        return Collections.emptySet();
    }
}
