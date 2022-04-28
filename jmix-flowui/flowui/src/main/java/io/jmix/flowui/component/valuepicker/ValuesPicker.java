package io.jmix.flowui.component.valuepicker;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.textfield.HasPrefixAndSuffix;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;

public class ValuesPicker<V> extends ValuePickerBase<ValuesPicker<V>, Collection<V>>
        implements HasPrefixAndSuffix {

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
    public Registration addCustomValueSetListener(ComponentEventListener<CustomValueSetEvent<ValuesPicker<V>, Collection<V>>> listener) {
        return super.addCustomValueSetListener(listener);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
                || CollectionUtils.isEmpty(getValue());
    }
}
