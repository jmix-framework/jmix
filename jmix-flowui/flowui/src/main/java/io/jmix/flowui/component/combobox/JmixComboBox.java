package io.jmix.flowui.component.combobox;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.data.Options;
import io.jmix.flowui.data.SupportsListOptions;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.HasTitle;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.AbstractFieldDelegate;
import io.jmix.flowui.component.delegate.FieldDelegate;
import io.jmix.flowui.component.delegate.ListOptionsDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;

public class JmixComboBox<V> extends ComboBox<V>
        implements SupportsValueSource<V>, SupportsValidation<V>, SupportsListOptions<V>,
        HasRequired, HasTitle,
        ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected String requiredMessage;

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
    public Options<V> getListOptions() {
        return optionsDelegate.getListOptions();
    }

    @Override
    public void setListOptions(@Nullable Options<V> options) {
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
        return requiredMessage;
    }

    @Override
    public void setRequiredMessage(@Nullable String requiredMessage) {
        this.requiredMessage = requiredMessage;
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
