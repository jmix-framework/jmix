/*
 * Copyright 2024 Haulmont.
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

import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.AccessManager;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.accesscontext.UiEntityAttributeContext;
import io.jmix.flowui.accesscontext.UiEntityContext;
import io.jmix.flowui.component.formatter.CollectionFormatter;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.HtmlContainerReadonlyDataBinding;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.formatter.Formatter;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static io.jmix.core.common.util.Preconditions.checkNotEmptyString;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component("flowui_HtmlContainerDataBinding")
public class HtmlContainerReadonlyDataBindingImpl implements HtmlContainerReadonlyDataBinding {

    private static final Logger log = LoggerFactory.getLogger(HtmlContainerReadonlyDataBindingImpl.class);

    protected MetadataTools metadataTools;
    protected CollectionFormatter collectionFormatter;
    protected AccessManager accessManager;

    protected Map<HtmlContainer, Runnable> listenersRegistrations = new HashMap<>();

    public HtmlContainerReadonlyDataBindingImpl(MetadataTools metadataTools, CollectionFormatter collectionFormatter,
                                                AccessManager accessManager) {
        this.metadataTools = metadataTools;
        this.collectionFormatter = collectionFormatter;
        this.accessManager = accessManager;
    }

    public void bind(HtmlContainer htmlComponent, ValueSource<?> valueSource) {
        bind(htmlComponent, valueSource, value -> metadataTools.format(value));
    }

    @Override
    public void bind(HtmlContainer htmlContainer, ValueSource<?> valueSource, Formatter<Object> formatter) {
        checkNotNullArgument(htmlContainer);
        checkNotNullArgument(valueSource);
        checkNotNullArgument(formatter);

        unbind(htmlContainer, true);

        updateHtmlContainerText(htmlContainer, formatter, valueSource.getValue());

        Registration registration = valueSource.addValueChangeListener(valueChangeEvent ->
                updateHtmlContainerText(htmlContainer, formatter, valueChangeEvent.getValue()));

        listenersRegistrations.put(htmlContainer, registration::remove);

        checkPermissions(htmlContainer, valueSource);
    }

    @Override
    public void bind(HtmlContainer htmlComponent, InstanceContainer<?> dataContainer, String property) {
        bind(htmlComponent, dataContainer, property, value -> metadataTools.format(value), true);
    }

    @Override
    public void bind(HtmlContainer htmlComponent, InstanceContainer<?> dataContainer, String property,
                     Formatter<Object> formatter) {
        bind(htmlComponent, dataContainer, property, formatter, true);
    }

    @Override
    public void bind(HtmlContainer htmlComponent, InstanceContainer<?> dataContainer, String property,
                     boolean dataModelSecurityEnabled) {
        bind(htmlComponent, dataContainer, property, value -> metadataTools.format(value), dataModelSecurityEnabled);
    }

    @Override
    public void bind(HtmlContainer htmlContainer, InstanceContainer<?> dataContainer, String property,
                     Formatter<Object> formatter, boolean dataModelSecurityEnabled) {
        checkNotNullArgument(htmlContainer);
        checkNotNullArgument(dataContainer);
        checkNotEmptyString(property);
        checkNotNullArgument(formatter);

        unbind(htmlContainer, true);

        Object item = dataContainer.getItemOrNull();
        if (item != null) {
            Object propertyValue = EntitySystemAccess.getEntityEntry(item).getAttributeValue(property);
            updateHtmlContainerText(htmlContainer, formatter, propertyValue);
        }

        Subscription propertyChangeSubscription = dataContainer.addItemPropertyChangeListener(itemPropertyChangeEvent -> {
            if (property.equals(itemPropertyChangeEvent.getProperty())) {
                updateHtmlContainerText(htmlContainer, formatter, itemPropertyChangeEvent.getValue());
            }
        });

        Subscription itemChangeSubscription = dataContainer.addItemChangeListener(itemChangeEvent -> {
            Object propertyValue = itemChangeEvent.getItem() != null
                    ? EntitySystemAccess.getEntityEntry(itemChangeEvent.getItem()).getAttributeValue(property)
                    : null;
            updateHtmlContainerText(htmlContainer, formatter, propertyValue);
        });

        listenersRegistrations.put(htmlContainer, () -> {
            propertyChangeSubscription.remove();
            itemChangeSubscription.remove();
        });

        if (dataModelSecurityEnabled) {
            checkPermissions(htmlContainer, dataContainer, property);
        }
    }

    @Override
    public void bind(HtmlContainer htmlComponent, CollectionContainer<?> dataContainer) {
        bind(htmlComponent, dataContainer, collectionFormatter, true);
    }

    @Override
    public void bind(HtmlContainer htmlComponent, CollectionContainer<?> dataContainer, boolean dataModelSecurityEnabled) {
        bind(htmlComponent, dataContainer, collectionFormatter, dataModelSecurityEnabled);
    }

    @Override
    public void bind(HtmlContainer htmlComponent, CollectionContainer<?> dataContainer, Formatter<Collection<?>> formatter) {
        bind(htmlComponent, dataContainer, formatter, true);
    }

    @Override
    public void bind(HtmlContainer htmlContainer, CollectionContainer<?> dataContainer,
                     Formatter<Collection<?>> formatter, boolean dataModelSecurityEnabled) {
        checkNotNullArgument(htmlContainer);
        checkNotNullArgument(dataContainer);
        checkNotNullArgument(formatter);

        unbind(htmlContainer, true);

        updateHtmlContainerText(htmlContainer, formatter, dataContainer.getItems());

        Subscription subscription = dataContainer.addCollectionChangeListener(collectionChangeEvent ->
                updateHtmlContainerText(htmlContainer, formatter, dataContainer.getItems()));

        listenersRegistrations.put(htmlContainer, subscription::remove);

        if (dataModelSecurityEnabled) {
            checkPermissions(htmlContainer, dataContainer);
        }
    }

    public void unbind(HtmlContainer htmlContainer) {
        unbind(htmlContainer, false);
    }

    protected <T> void updateHtmlContainerText(HtmlContainer htmlContainer,
                                               Formatter<T> formatter,
                                               @Nullable T value) {
        htmlContainer.setText(formatter.apply(value));
    }

    protected void unbind(HtmlContainer htmlContainer, boolean warnIfBound) {
        checkNotNullArgument(htmlContainer);

        Runnable removeRegistration = listenersRegistrations.remove(htmlContainer);
        if (removeRegistration == null) {
            return;
        }

        removeRegistration.run();

        if (warnIfBound) {
            log.warn("Silent unbind. HtmlContainer with id = '{}' was already bounden with data source.",
                    htmlContainer.getId());
        }
    }

    protected void checkPermissions(HtmlContainer htmlContainer, ValueSource<?> valueSource) {
        if (valueSource instanceof EntityValueSource<?, ?> entityValueSource &&
                entityValueSource.isDataModelSecurityEnabled()) {
            MetaPropertyPath metaPropertyPath = entityValueSource.getMetaPropertyPath();

            UiEntityAttributeContext attributeContext = new UiEntityAttributeContext(metaPropertyPath);
            accessManager.applyRegisteredConstraints(attributeContext);

            if (!attributeContext.canView()) {
                ComponentUtils.setVisible(htmlContainer, false);
            }
        }
    }

    protected void checkPermissions(HtmlContainer htmlContainer, InstanceContainer<?> dataContainer, String property) {
        MetaClass metaClass = dataContainer.getEntityMetaClass();
        MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPath(metaClass, property);
        UiEntityAttributeContext attributeContext = new UiEntityAttributeContext(metaPropertyPath);
        accessManager.applyRegisteredConstraints(attributeContext);
        if (!attributeContext.canView()) {
            ComponentUtils.setVisible(htmlContainer, false);
        }
    }

    protected void checkPermissions(HtmlContainer htmlContainer, CollectionContainer<?> dataContainer) {
        MetaClass metaClass = dataContainer.getEntityMetaClass();
        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);
        if (!entityContext.isViewPermitted()) {
            ComponentUtils.setVisible(htmlContainer, false);
        }
    }
}
