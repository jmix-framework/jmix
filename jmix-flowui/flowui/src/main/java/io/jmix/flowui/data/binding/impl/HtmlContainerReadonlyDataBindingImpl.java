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
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.accesscontext.UiEntityAttributeContext;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.HtmlContainerReadonlyDataBinding;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import static io.jmix.core.common.util.Preconditions.checkNotEmptyString;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component("flowui_HtmlContainerDataBinding")
public class HtmlContainerReadonlyDataBindingImpl implements HtmlContainerReadonlyDataBinding {

    protected MetadataTools metadataTools;
    protected AccessManager accessManager;

    public HtmlContainerReadonlyDataBindingImpl(MetadataTools metadataTools,
                                                AccessManager accessManager) {
        this.metadataTools = metadataTools;
        this.accessManager = accessManager;
    }

    public Registration bind(HtmlContainer htmlContainer, ValueSource<?> valueSource) {
        checkNotNullArgument(htmlContainer);
        checkNotNullArgument(valueSource);

        updateHtmlContainerText(htmlContainer, valueSource.getValue());

        Registration registration = valueSource.addValueChangeListener(valueChangeEvent ->
                updateHtmlContainerText(htmlContainer, valueChangeEvent.getValue()));

        checkPermissions(htmlContainer, valueSource);

        return registration;
    }

    @Override
    public Registration bind(HtmlContainer htmlContainer, InstanceContainer<?> dataContainer, String property) {
        checkNotNullArgument(dataContainer);
        checkNotEmptyString(property);

        Object item = dataContainer.getItemOrNull();
        if (item != null) {
            Object propertyValue = EntityValues.getValueEx(item, property);
            updateHtmlContainerText(htmlContainer, propertyValue);
        }

        Subscription propertyChangeSubscription = dataContainer.addItemPropertyChangeListener(itemPropertyChangeEvent -> {
            if (property.equals(itemPropertyChangeEvent.getProperty())) {
                updateHtmlContainerText(htmlContainer, itemPropertyChangeEvent.getValue());
            }
        });

        Subscription itemChangeSubscription = dataContainer.addItemChangeListener(itemChangeEvent -> {
            Object propertyValue = itemChangeEvent.getItem() != null
                    ? EntityValues.getValueEx(itemChangeEvent.getItem(), property)
                    : null;
            updateHtmlContainerText(htmlContainer, propertyValue);
        });

        checkPermissions(htmlContainer, dataContainer, property);

        return () -> {
            propertyChangeSubscription.remove();
            itemChangeSubscription.remove();
        };
    }

    @Override
    public Registration bind(HtmlContainer htmlContainer, CollectionContainer<?> dataContainer, String property) {
        return bind(htmlContainer, (InstanceContainer<?>) dataContainer, property);
    }

    protected <T> void updateHtmlContainerText(HtmlContainer htmlContainer, @Nullable T value) {
        htmlContainer.setText(metadataTools.format(value));
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
}
