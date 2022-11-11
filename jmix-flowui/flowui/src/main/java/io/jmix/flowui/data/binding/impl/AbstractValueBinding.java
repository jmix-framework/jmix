/*
 * Copyright 2022 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.flowui.data.binding.impl;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.flowui.accesscontext.FlowuiEntityAttributeContext;
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
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.BeanDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public abstract class AbstractValueBinding<V> implements ValueBinding<V>, SuspendableBinding {

    protected ApplicationContext applicationContext;
    protected MessageTools messageTools;
    protected MetadataTools metadataTools;
    protected Validator validator;
    protected AccessManager accessManager;

    protected ValueSource<V> valueSource;
    protected HasValue<?, V> component;

    protected Registration componentValueChangeRegistration;

    protected Registration valueSourceValueChangeRegistration;
    protected Registration valueSourceStateChangeRegistration;
    protected Registration valueSourceInstanceChangeRegistration;

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

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
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

        valueSourceValueChangeRegistration = valueSource.addValueChangeListener(this::onValueSourceValueChange);
        valueSourceStateChangeRegistration = valueSource.addStateChangeListener(this::onValueSourceStateChanged);

        component.setReadOnly(valueSource.isReadOnly());

        if (valueSource instanceof EntityValueSource) {
            EntityValueSource<?, V> entityValueSource = (EntityValueSource<?, V>) valueSource;
            MetaPropertyPath metaPropertyPath = entityValueSource.getMetaPropertyPath();

            this.valueSourceInstanceChangeRegistration =
                    entityValueSource.addInstanceChangeListener(this::onValueSourceInstanceChanged);

            initRequired(component, metaPropertyPath);

            if (component instanceof SupportsValidation) {
                initBeanValidator((SupportsValidation<V>) component, metaPropertyPath);
            }

            if (((EntityValueSource<?, V>) valueSource).isDataModelSecurityEnabled()) {
                FlowuiEntityAttributeContext attributeContext = new FlowuiEntityAttributeContext(metaPropertyPath);
                accessManager.applyRegisteredConstraints(attributeContext);

                MetaClass metaClass = entityValueSource.getEntityMetaClass();
                boolean permittedIfEmbedded = true;
                if (entityValueSource instanceof ContainerValueSource) {
                    InstanceContainer<?> container = ((ContainerValueSource<?, ?>) entityValueSource).getContainer();
                    if (container instanceof Nested && metaClass != null && metadataTools.isJpaEmbeddable(metaClass)) {
                        String embeddedProperty = ((Nested) container).getProperty();
                        MetaClass masterMetaClass = ((Nested) container).getMaster().getEntityMetaClass();

                        FlowuiEntityAttributeContext embeddedAttributeContext =
                                new FlowuiEntityAttributeContext(masterMetaClass, embeddedProperty);
                        accessManager.applyRegisteredConstraints(embeddedAttributeContext);

                        permittedIfEmbedded = embeddedAttributeContext.canModify();
                    }
                    if (permittedIfEmbedded && metaPropertyPath.length() > 1) {
                        for (MetaProperty property : metaPropertyPath.getMetaProperties()) {
                            if (!metadataTools.isEmbedded(property)) {
                                continue;
                            }

                            FlowuiEntityAttributeContext childAttributeContext =
                                    new FlowuiEntityAttributeContext(property.getDomain(), property.getName());
                            accessManager.applyRegisteredConstraints(childAttributeContext);

                            if (!childAttributeContext.canModify()) {
                                permittedIfEmbedded = false;
                                break;
                            }
                        }
                    }
                }

                if (!attributeContext.canModify() || !permittedIfEmbedded) {
                    component.setReadOnly(true);
                }

                if (!attributeContext.canView()) {
                    ((Component) component).setVisible(false);
                }

                resetRequiredIfAttributeFiltered(component, entityValueSource, metaPropertyPath);
            }
        }
    }

    protected void onValueSourceInstanceChanged(EntityValueSource.InstanceChangeEvent<?> event) {
        if (valueSource.getState() == BindingState.ACTIVE) {

            if (valueSource instanceof EntityValueSource) {
                EntityValueSource<?, V> entityValueSource = (EntityValueSource<?, V>) valueSource;

                resetRequiredIfAttributeFiltered(component, entityValueSource,
                        entityValueSource.getMetaPropertyPath());
            }

            // read value to component
            setComponentValue(valueSource.getValue());
        }
    }

    protected void onValueSourceStateChanged(StateChangeEvent event) {
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
        if (valueSourceValueChangeRegistration != null) {
            valueSourceValueChangeRegistration.remove();
            valueSourceValueChangeRegistration = null;
        }
        if (valueSourceStateChangeRegistration != null) {
            valueSourceStateChangeRegistration.remove();
            valueSourceStateChangeRegistration = null;
        }
        if (valueSourceInstanceChangeRegistration != null) {
            valueSourceInstanceChangeRegistration.remove();
            valueSourceInstanceChangeRegistration = null;
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

    /**
     * Set field's "required" flag to false if the value has been filtered by
     * Row Level Security. This is necessary to allow user to submit form with
     * filtered attribute even if attribute is required.
     */
    protected void resetRequiredIfAttributeFiltered(HasValue<?, V> field,
                                                    EntityValueSource<?, V> valueSource,
                                                    MetaPropertyPath metaPropertyPath) {
        if (field.isRequiredIndicatorVisible()
                && valueSource.getState() == BindingState.ACTIVE
                && valueSource.getItem() != null
                && metaPropertyPath.getMetaProperty().getRange().isClass()) {

            Object rootItem = valueSource.getItem();
            Object targetItem = rootItem;

            MetaProperty[] propertiesChain = metaPropertyPath.getMetaProperties();
            if (propertiesChain.length > 1) {
                String basePropertyItem = Arrays.stream(propertiesChain)
                        .limit(propertiesChain.length - 1)
                        .map(MetadataObject::getName)
                        .collect(Collectors.joining("."));

                targetItem = EntityValues.getValueEx(rootItem, basePropertyItem);
            }

            if (targetItem != null) {
                String propertyName = metaPropertyPath.getMetaProperty().getName();
                Object value = EntityValues.getValue(targetItem, propertyName);

                Collection<String> erasedAttributes =
                        EntitySystemAccess.getSecurityState(targetItem).getErasedAttributes();

                if (value == null && erasedAttributes.contains(propertyName)) {
                    field.setRequiredIndicatorVisible(false);
                }
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
