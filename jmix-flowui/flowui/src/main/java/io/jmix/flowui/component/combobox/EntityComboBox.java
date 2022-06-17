package io.jmix.flowui.component.combobox;

import com.vaadin.flow.shared.Registration;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.LookupComponent;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.EntityFieldDelegate;
import io.jmix.flowui.component.delegate.ListOptionsDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.component.valuepicker.JmixValuePickerActionSupport;
import io.jmix.flowui.data.Options;
import io.jmix.flowui.data.SupportsOptions;
import io.jmix.flowui.data.SupportsOptionsContainer;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.options.ContainerOptions;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.combobox.ComboBoxPicker;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerActionSupport;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public class EntityComboBox<V> extends ComboBoxPicker<V>
        implements EntityPickerComponent<V>, LookupComponent<V>,
        SupportsValidation<V>, SupportsOptions<V>, SupportsOptionsContainer<V>, HasRequired,
        ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected EntityFieldDelegate<EntityComboBox<V>, V, V> fieldDelegate;
    protected ListOptionsDelegate<EntityComboBox<V>, V> optionsDelegate;

    protected MetaClass metaClass;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    private void initComponent() {
        fieldDelegate = createFieldDelegate();
        optionsDelegate = createOptionsDelegate();

        setItemLabelGenerator(fieldDelegate::applyDefaultValueFormat);
    }

    @Override
    public void setValue(@Nullable V value) {
        checkValueType(value);
        super.setValue(value);
    }

    @Override
    public void setValueFromClient(@Nullable V value) {
        checkValueType(value);
        setModelValue(value, true);
    }

    protected void checkValueType(@Nullable V value) {
        if (value != null) {
            fieldDelegate.checkValueType(value);
        }
    }

    @Override
    public boolean isInvalid() {
        return fieldDelegate.isInvalid();
    }

    @Override
    public void setInvalid(boolean invalid) {
        fieldDelegate.setInvalid(invalid);
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return fieldDelegate.getRequiredMessage();
    }

    @Override
    public void setRequiredMessage(@Nullable String requiredMessage) {
        fieldDelegate.setRequiredMessage(requiredMessage);
    }

    @Override
    public Registration addValidator(Validator<? super V> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
    }

    @Nullable
    @Override
    public ValueSource<V> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<V> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    @Nullable
    @Override
    public Options<V> getOptions() {
        return optionsDelegate.getListOptions();
    }

    @Override
    public void setOptions(@Nullable Options<V> options) {
        optionsDelegate.setListOptions(options);
    }

    @Override
    public void setOptionsContainer(CollectionContainer<V> container) {
        setOptions(new ContainerOptions<>(container));
    }

    @Nullable
    @Override
    public MetaClass getMetaClass() {
        return fieldDelegate.getMetaClass();
    }

    @Override
    public void setMetaClass(@Nullable MetaClass metaClass) {
        fieldDelegate.setMetaClass(metaClass);
    }

    @Override
    public Set<V> getSelectedItems() {
        return isEmpty() ? Collections.emptySet() : Collections.singleton(getValue());
    }

    @SuppressWarnings("unchecked")
    protected EntityFieldDelegate<EntityComboBox<V>, V, V> createFieldDelegate() {
        return applicationContext.getBean(EntityFieldDelegate.class, this);
    }

    @SuppressWarnings("unchecked")
    protected ListOptionsDelegate<EntityComboBox<V>, V> createOptionsDelegate() {
        return applicationContext.getBean(ListOptionsDelegate.class, this);
    }

    @Override
    protected ValuePickerActionSupport createActionsSupport() {
        return new JmixValuePickerActionSupport(this);
    }
}
