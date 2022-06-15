package io.jmix.flowui.component.combobox;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.data.Options;
import io.jmix.flowui.data.SupportsOptions;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.AbstractFieldDelegate;
import io.jmix.flowui.component.delegate.FieldDelegate;
import io.jmix.flowui.component.delegate.ListOptionsDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.HasTitle;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;

public class JmixComboBox<V> extends ComboBox<V>
        implements SupportsValueSource<V>, SupportsValidation<V>, SupportsOptions<V>,
        HasRequired, HasTitle,
        ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected AbstractFieldDelegate<? extends JmixComboBox<V>, V, V> fieldDelegate;
    protected ListOptionsDelegate<JmixComboBox<V>, V> optionsDelegate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    protected void initComponent() {
        fieldDelegate = createFieldDelegate();
        optionsDelegate = createOptionsDelegate();

        setItemLabelGenerator(fieldDelegate::applyDefaultValueFormat);
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
    public Options<V> getOptions() {
        return optionsDelegate.getListOptions();
    }

    @Override
    public void setOptions(@Nullable Options<V> options) {
        optionsDelegate.setListOptions(options);
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
    public String getRequiredMessage() {
        return fieldDelegate.getRequiredMessage();
    }

    @Override
    public void setRequiredMessage(@Nullable String requiredMessage) {
        fieldDelegate.setRequiredMessage(requiredMessage);
    }

    @Override
    public boolean isInvalid() {
        return fieldDelegate.isInvalid();
    }

    @Override
    public void setInvalid(boolean invalid) {
        fieldDelegate.setInvalid(invalid);
    }

    @SuppressWarnings("unchecked")
    protected AbstractFieldDelegate<? extends JmixComboBox<V>, V, V> createFieldDelegate() {
        return applicationContext.getBean(FieldDelegate.class, this);
    }

    @SuppressWarnings("unchecked")
    protected ListOptionsDelegate<JmixComboBox<V>, V> createOptionsDelegate() {
        return applicationContext.getBean(ListOptionsDelegate.class, this);
    }
}
