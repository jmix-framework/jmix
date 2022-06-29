package io.jmix.flowui.component.combobox;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.AbstractFieldDelegate;
import io.jmix.flowui.component.delegate.DataViewDelegate;
import io.jmix.flowui.component.delegate.FieldDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.SupportsDataProvider;
import io.jmix.flowui.data.SupportsItemsEnum;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.HasTitle;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;

public class JmixComboBox<V> extends ComboBox<V>
        implements SupportsValueSource<V>, SupportsValidation<V>, SupportsDataProvider<V>,
        SupportsItemsEnum<V>, HasRequired, HasTitle,
        ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected AbstractFieldDelegate<? extends JmixComboBox<V>, V, V> fieldDelegate;
    protected DataViewDelegate<JmixComboBox<V>, V> dataViewDelegate;

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
        dataViewDelegate = createDataViewDelegate();

        fieldDelegate.addValueBindingChangeListener(event ->
                dataViewDelegate.valueBindingChanged(event));

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

    @Override
    public <C> void setDataProvider(DataProvider<V, C> dataProvider, SerializableFunction<String, C> filterConverter) {
        // Method is called from a constructor so bean can be null
        if (dataViewDelegate != null) {
            dataViewDelegate.bind(dataProvider);
        }
        super.setDataProvider(dataProvider, filterConverter);
    }

    @Override
    public void setItems(Class<V> itemsEnum) {
        dataViewDelegate.setItems(itemsEnum);
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
    protected DataViewDelegate<JmixComboBox<V>, V> createDataViewDelegate() {
        return applicationContext.getBean(DataViewDelegate.class, this);
    }
}
