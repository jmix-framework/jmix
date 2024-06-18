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
import io.jmix.core.MetadataTools;
import io.jmix.core.common.datastruct.Pair;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.HtmlContainerReadonlyDataBinding;
import io.jmix.flowui.kit.component.formatter.Formatter;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("flowui_HtmlContainerDataBinding")
public class HtmlContainerReadonlyDataBindingImpl implements HtmlContainerReadonlyDataBinding {

    protected MetadataTools metadataTools;

    public HtmlContainerReadonlyDataBindingImpl(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    protected List<Pair<HtmlContainer, HtmlContainerListenerRegistrationsCleaner>> listenersRegistrations = new LinkedList<>();

    public void bind(HtmlContainer htmlComponent, ValueSource<?> valueSource) {
        bind(htmlComponent, valueSource, new HtmlContainerTextFormatter());
    }

    @Override
    public void bind(HtmlContainer htmlContainer, ValueSource<?> valueSource, Formatter<Object> formatter) {
        Preconditions.checkNotNullArgument(htmlContainer);
        Preconditions.checkNotNullArgument(valueSource);
        Preconditions.checkNotNullArgument(formatter);

        updateHtmlContainerText(htmlContainer, formatter, valueSource.getValue());

        Registration registration = valueSource.addValueChangeListener(valueChangeEvent ->
            updateHtmlContainerText(htmlContainer, formatter, valueChangeEvent.getValue()));

        HtmlContainerListenerRegistrationsCleaner remove = registration::remove;
        listenersRegistrations.add(new Pair<>(htmlContainer, remove));
    }

    @Override
    public void bind(HtmlContainer htmlComponent, InstanceContainer<?> dataContainer, String property) {
        bind(htmlComponent, dataContainer, property, new HtmlContainerTextFormatter());
    }

    @Override
    public void bind(HtmlContainer htmlContainer, InstanceContainer<?> dataContainer, String property, Formatter<Object> formatter) {
        Preconditions.checkNotNullArgument(htmlContainer);
        Preconditions.checkNotNullArgument(dataContainer);
        Preconditions.checkNotEmptyString(property);
        Preconditions.checkNotNullArgument(formatter);

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
        HtmlContainerListenerRegistrationsCleaner propertyChangeCleaner = propertyChangeSubscription::remove;
        listenersRegistrations.add(new Pair<>(htmlContainer, propertyChangeCleaner));

        Subscription itemChangeSubscription = dataContainer.addItemChangeListener(itemChangeEvent -> {
            Object propertyValue = itemChangeEvent.getItem() != null
                    ? EntitySystemAccess.getEntityEntry(itemChangeEvent.getItem()).getAttributeValue(property)
                    : null;
            updateHtmlContainerText(htmlContainer, formatter, propertyValue);
        });
        HtmlContainerListenerRegistrationsCleaner itemChangeCleaner = itemChangeSubscription::remove;
        listenersRegistrations.add(new Pair<>(htmlContainer, itemChangeCleaner));
    }

    @Override
    public void bind(HtmlContainer htmlComponent, CollectionContainer<?> dataContainer) {
        bind(htmlComponent, dataContainer, new HtmlContainerTextFormatter());
    }

    @Override
    public void bind(HtmlContainer htmlContainer, CollectionContainer<?> dataContainer, Formatter<Object> formatter) {
        Preconditions.checkNotNullArgument(htmlContainer);
        Preconditions.checkNotNullArgument(dataContainer);
        Preconditions.checkNotNullArgument(formatter);

        updateHtmlContainerText(htmlContainer, formatter, dataContainer.getItems());

        Subscription subscription = dataContainer.addCollectionChangeListener(collectionChangeEvent ->
                updateHtmlContainerText(htmlContainer, formatter, dataContainer.getItems()));

        HtmlContainerListenerRegistrationsCleaner htmlContainerListenerRegistrationsCleaner = subscription::remove;
        listenersRegistrations.add(new Pair<>(htmlContainer, htmlContainerListenerRegistrationsCleaner));
    }

    public void unbind(HtmlContainer htmlContainer) {
        Preconditions.checkNotNullArgument(htmlContainer);

        Iterator<Pair<HtmlContainer, HtmlContainerListenerRegistrationsCleaner>> iterator = listenersRegistrations.iterator();
        while (iterator.hasNext()) {
            Pair<HtmlContainer, HtmlContainerListenerRegistrationsCleaner> htmlContainerAndCleaner = iterator.next();
            if (htmlContainer.equals(htmlContainerAndCleaner.getFirst())) {
                htmlContainerAndCleaner.getSecond().remove();
                iterator.remove();
            }
        }
    }

    protected void updateHtmlContainerText(HtmlContainer htmlContainer,
                                           Formatter<Object> formatter,
                                           @Nullable Object value) {
        htmlContainer.setText(formatter.apply(value));
    }

    @FunctionalInterface
    protected interface HtmlContainerListenerRegistrationsCleaner {
        void remove();
    }

    class HtmlContainerTextFormatter implements Formatter<Object> {

        @Override
        public String apply(@Nullable Object value) {
            if (value instanceof Collection<?>) {
                return formatCollection((Collection<?>) value);
            }
            return formatObject(value);
        }

        public String formatObject(@Nullable Object property) {
            return metadataTools.format(property);
        }

        public String formatCollection(@Nullable Collection<?> collection) {
            if (collection == null) {
                return StringUtils.EMPTY;
            }
            
            return collection.stream()
                    .map(this::formatObject)
                    .collect(Collectors.joining(", "));
        }
    }
}
