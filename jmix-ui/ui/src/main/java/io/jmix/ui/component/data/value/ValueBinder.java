/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.data.value;

import com.google.common.base.Strings;
import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Field;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.meta.ValueBinding;
import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.component.validator.BeanPropertyValidator;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.validation.constraints.NotNull;
import javax.validation.metadata.BeanDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component("ui_ValueBinder")
public class ValueBinder {

    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected javax.validation.Validator validator;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected AccessManager accessManager;

    public <V> ValueBinding<V> bind(HasValue<V> component, ValueSource<V> valueSource) {
        if (valueSource instanceof ApplicationContextAware) {
            ((ApplicationContextAware) valueSource).setApplicationContext(applicationContext);
        }

        ValueBindingImpl<V> binding = new ValueBindingImpl<>(component, valueSource);

        if (component instanceof Component.Editable) {
            ((Component.Editable) component).setEditable(!valueSource.isReadOnly());
        }

        if (valueSource instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueSource;

            MetaPropertyPath metaPropertyPath = entityValueSource.getMetaPropertyPath();

            if (component instanceof Field) {
                initRequired((Field) component, metaPropertyPath);

                initBeanValidator((Field<?>) component, metaPropertyPath);
            }

            if (entityValueSource.isDataModelSecurityEnabled()) {
                UiEntityAttributeContext attributeContext = new UiEntityAttributeContext(metaPropertyPath);
                accessManager.applyRegisteredConstraints(attributeContext);

                if (component instanceof Component.Editable) {
                    MetaClass metaClass = entityValueSource.getEntityMetaClass();
                    boolean permittedIfEmbedded = true;
                    if (entityValueSource instanceof ContainerValueSource) {
                        InstanceContainer<?> container = ((ContainerValueSource<?, ?>) entityValueSource).getContainer();
                        if (container instanceof Nested && metaClass != null && metadataTools.isJpaEmbeddable(metaClass)) {
                            String embeddedProperty = ((Nested) container).getProperty();
                            MetaClass masterMetaClass = ((Nested) container).getMaster().getEntityMetaClass();

                            UiEntityAttributeContext embeddedAttributeContext = new UiEntityAttributeContext(masterMetaClass, embeddedProperty);
                            accessManager.applyRegisteredConstraints(embeddedAttributeContext);

                            permittedIfEmbedded = embeddedAttributeContext.canModify();
                        }
                        if (permittedIfEmbedded && metaPropertyPath.length() > 1) {
                            for (MetaProperty property : metaPropertyPath.getMetaProperties()) {
                                if (metadataTools.isEmbedded(property)) {
                                    UiEntityAttributeContext childAttributeContext =
                                            new UiEntityAttributeContext(property.getDomain(), property.getName());
                                    accessManager.applyRegisteredConstraints(childAttributeContext);

                                    if (!childAttributeContext.canModify()) {
                                        permittedIfEmbedded = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (!attributeContext.canModify() || !permittedIfEmbedded) {
                        ((Component.Editable) component).setEditable(false);
                    }
                }

                if (!attributeContext.canView()) {
                    component.setVisible(false);
                }
            }
        }

        binding.bind();

        return binding;
    }

    protected void initRequired(Field<?> component, MetaPropertyPath metaPropertyPath) {
        MetaProperty metaProperty = metaPropertyPath.getMetaProperty();

        boolean newRequired = metaProperty.isMandatory();
        Object notNullUiComponent = metaProperty.getAnnotations()
                .get(NotNull.class.getName() + "_notnull_ui_component");
        if (Boolean.TRUE.equals(notNullUiComponent)) {
            newRequired = true;
        }

        if (newRequired) {
            component.setRequired(true);
        }

        if (Strings.isNullOrEmpty(component.getRequiredMessage())) {
            component.setRequiredMessage(messageTools.getDefaultRequiredMessage(
                    metaPropertyPath.getMetaClass(), metaPropertyPath.toPathString())
            );
        }
    }

    protected void initBeanValidator(Field<?> component, MetaPropertyPath metaPropertyPath) {
        MetaProperty metaProperty = metaPropertyPath.getMetaProperty();

        MetaClass propertyEnclosingMetaClass = metadataTools.getPropertyEnclosingMetaClass(metaPropertyPath);
        Class enclosingJavaClass = propertyEnclosingMetaClass.getJavaClass();

        if (enclosingJavaClass != KeyValueEntity.class) {
            BeanDescriptor beanDescriptor = validator.getConstraintsForClass(enclosingJavaClass);

            if (beanDescriptor.isBeanConstrained()) {
                component.addValidator(applicationContext.getBean(BeanPropertyValidator.class, enclosingJavaClass, metaProperty.getName()));
            }
        }
    }

    protected static class ValueBindingImpl<V> implements ValueBinding<V> {
        protected ValueSource<V> source;
        protected HasValue<V> component;

        protected Subscription componentValueChangeSubscription;

        protected Subscription sourceValueChangeSubscription;
        protected Subscription sourceStateChangeSubscription;
        protected Subscription sourceInstanceChangeSubscription;

        protected boolean buffered = false;

        public ValueBindingImpl(HasValue<V> component, ValueSource<V> source) {
            this.source = source;
            this.component = component;
        }

        @Override
        public ValueSource<V> getSource() {
            return source;
        }

        @Override
        public HasValue<V> getComponent() {
            return component;
        }

        @Override
        public void unbind() {
            if (this.componentValueChangeSubscription != null) {
                this.componentValueChangeSubscription.remove();
                this.componentValueChangeSubscription = null;
            }
            if (this.sourceValueChangeSubscription != null) {
                this.sourceValueChangeSubscription.remove();
                this.sourceValueChangeSubscription = null;
            }
            if (this.sourceStateChangeSubscription != null) {
                this.sourceStateChangeSubscription.remove();
                this.sourceStateChangeSubscription = null;
            }
            if (this.sourceInstanceChangeSubscription != null) {
                this.sourceInstanceChangeSubscription.remove();
                this.sourceInstanceChangeSubscription = null;
            }

            if (component instanceof Field) {
                disableBeanValidator((Field<?>) component);
            }
        }

        @Override
        public void activate() {
            if (source.getState() == BindingState.ACTIVE) {
                component.setValue(source.getValue());
            }
        }

        @SuppressWarnings("unchecked")
        protected void disableBeanValidator(Field<?> component) {
            Collection<? extends Validator<?>> validators = component.getValidators();

            for (Validator validator : validators.toArray(new Validator[0])) {
                if (validator instanceof BeanPropertyValidator) {
                    component.removeValidator(validator);
                }
            }
        }

        public void bind() {
            this.componentValueChangeSubscription = component.addValueChangeListener(this::componentValueChanged);

            this.sourceValueChangeSubscription = source.addValueChangeListener(this::sourceValueChanged);
            this.sourceStateChangeSubscription = source.addStateChangeListener(this::valueSourceStateChanged);
            if (source instanceof EntityValueSource) {
                EntityValueSource<Object, V> entityValueSource = (EntityValueSource<Object, V>) this.source;
                this.sourceInstanceChangeSubscription = entityValueSource.addInstanceChangeListener(this::sourceInstanceChanged);

                if (component instanceof Field) {
                    resetRequiredIfAttributeFiltered((Field) component, entityValueSource, entityValueSource.getMetaPropertyPath());
                }
            }
        }

        @Override
        public void write() {
            if (isBuffered()
                    && isModified()) {
                setValueToSource(component.getValue());
            }
        }

        @Override
        public void discard() {
            if (source.getState() == BindingState.ACTIVE
                    && isBuffered()
                    && isModified()) {
                component.setValue(source.getValue());
            }
        }

        @Override
        public boolean isBuffered() {
            return buffered;
        }

        @Override
        public void setBuffered(boolean buffered) {
            this.buffered = buffered;
            if (!buffered
                    && source.getState() == BindingState.ACTIVE) {
                // reset value of the component
                component.setValue(source.getValue());
            }
        }

        @Override
        public boolean isModified() {
            return !Objects.equals(component.getValue(), source.getValue());
        }

        protected void valueSourceStateChanged(ValueSource.StateChangeEvent event) {
            if (event.getState() == BindingState.ACTIVE) {
                // read value to component
                component.setValue(source.getValue());
            }
        }

        protected void componentValueChanged(@SuppressWarnings("unused") HasValue.ValueChangeEvent event) {
            if (!isBuffered()) {
                // do not use event.getValue() because it can be already stale due to another value change listener
                // always read value from component instead
                V componentValue = component.getValue();
                setValueToSource(componentValue);
            }
        }

        protected void setValueToSource(V value) {
            if (source.getState() == BindingState.ACTIVE
                    && !source.isReadOnly()) {
                source.setValue(value);
            }
        }

        protected void sourceValueChanged(@SuppressWarnings("unused") ValueSource.ValueChangeEvent<V> event) {
            if (!isBuffered()) {
                // do not use event.getValue() because it can be already stale due to another value change listener
                // always read value from source instead
                V sourceValue = source.getValue();
                component.setValue(sourceValue);
            }
        }

        protected void sourceInstanceChanged(@SuppressWarnings("unused") EntityValueSource.InstanceChangeEvent<Object> event) {
            if (source.getState() == BindingState.ACTIVE
                    && !isBuffered()) {

                if (source instanceof EntityValueSource
                        && component instanceof Field) {
                    EntityValueSource entityValueSource = (EntityValueSource) this.source;

                    resetRequiredIfAttributeFiltered((Field) component, entityValueSource,
                            entityValueSource.getMetaPropertyPath());
                }
                // read value to component
                component.setValue(source.getValue());
            }
        }

        /**
         * Set field's "required" flag to false if the value has been filtered by Row Level Security.
         * This is necessary to allow user to submit form with filtered attribute even if attribute is required.
         */
        protected void resetRequiredIfAttributeFiltered(Field<?> field, EntityValueSource valueSource,
                                                        MetaPropertyPath metaPropertyPath) {
            if (field.isRequired()
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

                    Collection<String> erasedAttributes = EntitySystemAccess.getSecurityState(targetItem).getErasedAttributes();

                    if (value == null && erasedAttributes.contains(propertyName)) {
                        field.setRequired(false);
                    }
                }
            }
        }
    }
}
