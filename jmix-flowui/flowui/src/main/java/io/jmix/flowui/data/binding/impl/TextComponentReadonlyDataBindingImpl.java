/*
 * Copyright 2026 Haulmont.
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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.AccessManager;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.accesscontext.UiEntityAttributeContext;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.TextComponentReadonlyDataBinding;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.InstanceContainer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotEmptyString;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Implementation of {@link TextComponentReadonlyDataBinding}.
 * Displays formatted entity property values using {@link HasText} or a custom text setter.
 */
@NullMarked
@org.springframework.stereotype.Component("flowui_TextComponentReadonlyDataBinding")
public class TextComponentReadonlyDataBindingImpl implements TextComponentReadonlyDataBinding {

    protected MetadataTools metadataTools;
    protected AccessManager accessManager;

    public TextComponentReadonlyDataBindingImpl(MetadataTools metadataTools,
                                                AccessManager accessManager) {
        this.metadataTools = metadataTools;
        this.accessManager = accessManager;
    }

    @Override
    public <C extends Component & HasText> Registration bind(C component, ValueSource<?> valueSource) {
        return bindInternal(component, valueSource, value -> updateComponentText(component, value));
    }

    @Override
    public Registration bind(Component component, ValueSource<?> valueSource, Consumer<String> textSetter) {
        checkNotNullArgument(textSetter);

        return bindInternal(component, valueSource, value -> updateComponentText(textSetter, value));
    }

    protected Registration bindInternal(Component component, ValueSource<?> valueSource,
                                        Consumer<@Nullable Object> valueConsumer) {
        checkNotNullArgument(component);
        checkNotNullArgument(valueSource);

        valueConsumer.accept(valueSource.getValue());

        Registration registration = valueSource.addValueChangeListener(event ->
                valueConsumer.accept(valueSource.getValue()));

        if (valueSource instanceof EntityValueSource<?, ?> entityValueSource) {
            Registration instanceChangeRegistration = entityValueSource.addInstanceChangeListener(event ->
                    valueConsumer.accept(valueSource.getValue()));
            registration = Registration.combine(registration, instanceChangeRegistration);
        }

        checkPermissions(component, valueSource);

        return registration;
    }

    @Override
    public <C extends Component & HasText> Registration bind(C component, InstanceContainer<?> dataContainer,
                                                             String property) {
        return bindInternal(component, dataContainer, property, value -> updateComponentText(component, value));
    }

    @Override
    public Registration bind(Component component, InstanceContainer<?> dataContainer, String property,
                             Consumer<String> textSetter) {
        checkNotNullArgument(textSetter);

        return bindInternal(component, dataContainer, property,
                value -> updateComponentText(textSetter, value));
    }

    protected Registration bindInternal(Component component, InstanceContainer<?> dataContainer, String property,
                                        Consumer<@Nullable Object> valueConsumer) {
        checkNotNullArgument(component);
        checkNotNullArgument(dataContainer);
        checkNotEmptyString(property);

        Object item = dataContainer.getItemOrNull();
        if (item != null) {
            valueConsumer.accept(EntityValues.getValueEx(item, property));
        }

        Subscription propertyChangeSubscription = dataContainer.addItemPropertyChangeListener(
                itemPropertyChangeEvent -> {
                    if (itemPropertyChangeEvent.getItem() == dataContainer.getItemOrNull()
                            && property.equals(itemPropertyChangeEvent.getProperty())) {
                        valueConsumer.accept(itemPropertyChangeEvent.getValue());
                    }
                });

        Subscription itemChangeSubscription = dataContainer.addItemChangeListener(itemChangeEvent -> {
            Object propertyValue = itemChangeEvent.getItem() != null
                    ? EntityValues.getValueEx(itemChangeEvent.getItem(), property)
                    : null;
            valueConsumer.accept(propertyValue);
        });

        checkPermissions(component, dataContainer, property);

        return () -> {
            propertyChangeSubscription.remove();
            itemChangeSubscription.remove();
        };
    }

    protected void updateComponentText(HasText component, @Nullable Object value) {
        updateComponentText(component::setText, value);
    }

    protected void updateComponentText(Consumer<String> textSetter, @Nullable Object value) {
        textSetter.accept(metadataTools.format(value));
    }

    protected void checkPermissions(Component component, ValueSource<?> valueSource) {
        if (valueSource instanceof EntityValueSource<?, ?> entityValueSource &&
                entityValueSource.isDataModelSecurityEnabled()) {
            MetaPropertyPath metaPropertyPath = entityValueSource.getMetaPropertyPath();

            UiEntityAttributeContext attributeContext = new UiEntityAttributeContext(metaPropertyPath);
            accessManager.applyRegisteredConstraints(attributeContext);

            if (!attributeContext.canView()) {
                ComponentUtils.setVisible(component, false);
            }
        }
    }

    protected void checkPermissions(Component component, InstanceContainer<?> dataContainer, String property) {
        MetaClass metaClass = dataContainer.getEntityMetaClass();
        MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPath(metaClass, property);
        UiEntityAttributeContext attributeContext = new UiEntityAttributeContext(metaPropertyPath);
        accessManager.applyRegisteredConstraints(attributeContext);
        if (!attributeContext.canView()) {
            ComponentUtils.setVisible(component, false);
        }
    }
}
