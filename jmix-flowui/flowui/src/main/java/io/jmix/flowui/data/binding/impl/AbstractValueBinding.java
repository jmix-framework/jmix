package io.jmix.flowui.data.binding.impl;

import com.google.common.base.Strings;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.data.binding.SuspendableBinding;
import io.jmix.flowui.data.binding.ValueBinding;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.DataUnit.StateChangeEvent;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.validation.bean.BeanPropertyValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.BeanDescriptor;

public abstract class AbstractValueBinding<V> implements ValueBinding<V>, SuspendableBinding {

    protected ApplicationContext applicationContext;
    protected MessageTools messageTools;
    protected MetadataTools metadataTools;
    protected Validator validator;

    protected ValueSource<V> valueSource;
    protected HasValue<?, V> component;

    protected Registration componentValueChangeRegistration;
    protected Registration valueSourceValueChangeSubscription;
    protected Registration valueSourceStateChangeSubscription;

    protected boolean suspended = false;

    public AbstractValueBinding(ValueSource<V> valueSource, HasValue<?, V> component) {
        Preconditions.checkNotNullArgument(valueSource);
        Preconditions.checkNotNullArgument(component);

        this.valueSource = valueSource;
        this.component = component;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public ValueSource<V> getValueSource() {
        return valueSource;
    }

    @Override
    public HasValue<?, V> getComponent() {
        return component;
    }

    @Override
    public void bind() {
        if (valueSource instanceof ApplicationContextAware) {
            ((ApplicationContextAware) valueSource).setApplicationContext(applicationContext);
        }

        componentValueChangeRegistration = addComponentValueChangeListener(this::onComponentValueChange);

        valueSourceValueChangeSubscription = valueSource.addValueChangeListener(this::onValueSourceValueChange);
        valueSourceStateChangeSubscription = valueSource.addStateChangeListener(this::valueSourceStateChanged);

        component.setReadOnly(valueSource.isReadOnly());

        if (valueSource instanceof EntityValueSource) {
            EntityValueSource<?, ?> entityValueSource = (EntityValueSource<?, ?>) valueSource;
            MetaPropertyPath metaPropertyPath = entityValueSource.getMetaPropertyPath();

            initRequired(component, metaPropertyPath);

            if (component instanceof SupportsValidation) {
                //noinspection unchecked
                initBeanValidator((SupportsValidation<V>) component, metaPropertyPath);
            }
        }
    }

    protected void valueSourceStateChanged(StateChangeEvent event) {
        if (event.getState() == BindingState.ACTIVE) {
            // read value to component
            setComponentValue(valueSource.getValue());
        }
    }

    protected void onValueSourceValueChange(ValueSource.ValueChangeEvent<V> event) {
        // do not use event.getValue() because it can be already stale due to another value change listener
        // always read value from source instead
        V sourceValue = valueSource.getValue();
        setComponentValue(sourceValue);
    }

    protected void onComponentValueChange() {
        // do not use event.getValue() because it can be already stale due to another value change listener
        // always read value from component instead
        V componentValue = getComponentValue();
        setValueToSource(componentValue);
    }

    @Override
    public void unbind() {
        if (componentValueChangeRegistration != null) {
            componentValueChangeRegistration.remove();
            componentValueChangeRegistration = null;
        }
        if (valueSourceValueChangeSubscription != null) {
            valueSourceValueChangeSubscription.remove();
            valueSourceValueChangeSubscription = null;
        }
        if (valueSourceStateChangeSubscription != null) {
            valueSourceStateChangeSubscription.remove();
            valueSourceStateChangeSubscription = null;
        }
    }

    @Override
    public void activate() {
        if (valueSource.getState() == BindingState.ACTIVE) {
            setComponentValue(valueSource.getValue());
        }
    }

    @Override
    public void suspend() {
        suspended = true;
    }

    @Override
    public void resume() {
        suspended = false;
    }

    @Override
    public boolean suspended() {
        return suspended;
    }

    protected void setValueToSource(@Nullable V value) {
        if (suspended()) {
            return;
        }

        if (valueSource.getState() == BindingState.ACTIVE
                && !valueSource.isReadOnly()) {
            valueSource.setValue(value);
        }
    }

    protected void initRequired(HasValue<?, V> component, MetaPropertyPath metaPropertyPath) {
        MetaProperty metaProperty = metaPropertyPath.getMetaProperty();

        boolean newRequired = metaProperty.isMandatory();
        Object notNullUiComponent = metaProperty.getAnnotations()
                .get(NotNull.class.getName() + "_notnull_ui_component");
        if (Boolean.TRUE.equals(notNullUiComponent)) {
            newRequired = true;
        }

        if (newRequired) {
            component.setRequiredIndicatorVisible(true);
        }

        if (component instanceof HasRequired) {
            String errorMessage = ((HasRequired) component).getRequiredMessage();

            if (Strings.isNullOrEmpty(errorMessage)) {
                String defaultRequiredMessage = messageTools.getDefaultRequiredMessage(
                        metaPropertyPath.getMetaClass(),
                        metaPropertyPath.toPathString());
                ((HasRequired) component).setRequiredMessage(defaultRequiredMessage);
            }
        }
    }

    protected void initBeanValidator(SupportsValidation<V> component, MetaPropertyPath mpp) {
        MetaProperty metaProperty = mpp.getMetaProperty();

        MetaClass propertyEnclosingMetaClass = metadataTools.getPropertyEnclosingMetaClass(mpp);
        Class<?> enclosingJavaClass = propertyEnclosingMetaClass.getJavaClass();

        if (enclosingJavaClass != KeyValueEntity.class) {
            BeanDescriptor beanDescriptor = validator.getConstraintsForClass(enclosingJavaClass);

            if (beanDescriptor.isBeanConstrained()) {
                //noinspection unchecked
                component.addValidator(applicationContext.getBean(BeanPropertyValidator.class, enclosingJavaClass, metaProperty.getName()));
            }
        }
    }

    @Nullable
    protected abstract V getComponentValue();

    protected abstract void setComponentValue(@Nullable V value);

    @SuppressWarnings("unchecked")
    protected Registration addComponentValueChangeListener(Runnable listener) {
        if (component instanceof SupportsTypedValue) {
            return ((SupportsTypedValue<?, ?, V, ?>) component).addTypedValueChangeListener(event -> listener.run());
        } else {
            return component.addValueChangeListener(event -> listener.run());
        }
    }
}
