package io.jmix.flowui.component.valuepicker;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.textfield.HasPrefixAndSuffix;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.component.LookupComponent;
import io.jmix.flowui.component.delegate.AbstractFieldDelegate;
import io.jmix.flowui.component.delegate.EntityFieldDelegate;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public class EntityPicker<V> extends ValuePickerBase<EntityPicker<V>, V>
        implements EntityPickerComponent<V>, LookupComponent<V>, HasPrefixAndSuffix {

    @Override
    protected void initComponent() {
        super.initComponent();
        initFieldValuePropertyChangeListener();
    }


    @Override
    public void setValue(@Nullable V value) {
        getFieldDelegate().checkValueType(value);
        super.setValue(value);
    }

    @Override
    public void setValueFromClient(@Nullable V value) {
        getFieldDelegate().checkValueType(value);
        super.setValueFromClient(value);
    }

    @Nullable
    @Override
    public MetaClass getMetaClass() {
        return getFieldDelegate().getMetaClass();
    }

    @Override
    public void setMetaClass(@Nullable MetaClass metaClass) {
        getFieldDelegate().setMetaClass(metaClass);
    }

    public boolean isAllowCustomValue() {
        return super.isAllowCustomValueBoolean();
    }

    @Override
    public void setAllowCustomValue(boolean allowCustomValue) {
        super.setAllowCustomValue(allowCustomValue);
    }

    @Override
    public Registration addCustomValueSetListener(ComponentEventListener<CustomValueSetEvent<EntityPicker<V>, V>> listener) {
        return super.addCustomValueSetListener(listener);
    }

    @Override
    public Set<V> getSelectedItems() {
        return isEmpty() ? Collections.emptySet() : Collections.singleton(getValue());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractFieldDelegate<EntityPicker<V>, V, V> createFieldDelegate() {
        return applicationContext.getBean(EntityFieldDelegate.class, this);
    }

    protected EntityFieldDelegate<EntityPicker<V>, V, V> getFieldDelegate() {
        return (EntityFieldDelegate<EntityPicker<V>, V, V>) fieldDelegate;
    }
}
