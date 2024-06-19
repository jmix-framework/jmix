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
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.flowui.component.formatter.CollectionFormatter;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.HtmlContainerReadonlyDataBinding;
import io.jmix.flowui.kit.component.formatter.Formatter;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static io.jmix.core.common.util.Preconditions.checkNotEmptyString;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component("flowui_HtmlContainerDataBinding")
public class HtmlContainerReadonlyDataBindingImpl implements HtmlContainerReadonlyDataBinding {

    private static final Logger log = LoggerFactory.getLogger(HtmlContainerReadonlyDataBindingImpl.class);

    protected MetadataTools metadataTools;
    protected List<Pair<HtmlContainer, DataBindingListenerCleaner>> listenersRegistrations = new LinkedList<>();


    public HtmlContainerReadonlyDataBindingImpl(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
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

        DataBindingListenerCleaner remove = registration::remove;
        listenersRegistrations.add(new Pair<>(htmlContainer, remove));
    }

    @Override
    public void bind(HtmlContainer htmlComponent, InstanceContainer<?> dataContainer, String property) {
        bind(htmlComponent, dataContainer, property, value -> metadataTools.format(value));
    }

    @Override
    public void bind(HtmlContainer htmlContainer, InstanceContainer<?> dataContainer, String property,
                     Formatter<Object> formatter) {
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

        Subscription propertyChangeSubscription = dataContainer.addItemPropertyChangeListener(
                itemPropertyChangeEvent -> {
                    if (property.equals(itemPropertyChangeEvent.getProperty())) {
                    updateHtmlContainerText(htmlContainer, formatter, itemPropertyChangeEvent.getValue());
                }
        });
        DataBindingListenerCleaner propertyChangeCleaner = propertyChangeSubscription::remove;
        listenersRegistrations.add(new Pair<>(htmlContainer, propertyChangeCleaner));

        Subscription itemChangeSubscription = dataContainer.addItemChangeListener(itemChangeEvent -> {
            Object propertyValue = itemChangeEvent.getItem() != null
                    ? EntitySystemAccess.getEntityEntry(itemChangeEvent.getItem()).getAttributeValue(property)
                    : null;
            updateHtmlContainerText(htmlContainer, formatter, propertyValue);
        });
        DataBindingListenerCleaner itemChangeCleaner = itemChangeSubscription::remove;
        listenersRegistrations.add(new Pair<>(htmlContainer, itemChangeCleaner));
    }

    @Override
    public void bind(HtmlContainer htmlComponent, CollectionContainer<?> dataContainer) {
        bind(htmlComponent, dataContainer, new CollectionFormatter(metadataTools));
    }

    @Override
    public void bind(HtmlContainer htmlContainer, CollectionContainer<?> dataContainer,
                     Formatter<Collection<?>> formatter) {
        checkNotNullArgument(htmlContainer);
        checkNotNullArgument(dataContainer);
        checkNotNullArgument(formatter);

        unbind(htmlContainer, true);

        updateHtmlContainerText(htmlContainer, formatter, dataContainer.getItems());

        Subscription subscription = dataContainer.addCollectionChangeListener(collectionChangeEvent ->
                updateHtmlContainerText(htmlContainer, formatter, dataContainer.getItems()));

        DataBindingListenerCleaner dataBindingListenerCleaner = subscription::remove;
        listenersRegistrations.add(new Pair<>(htmlContainer, dataBindingListenerCleaner));
    }

    public void unbind(HtmlContainer htmlContainer) {
        unbind(htmlContainer, false);
    }

    protected void updateHtmlContainerText(HtmlContainer htmlContainer,
                                           Formatter<Object> formatter,
                                           @Nullable Object value) {
        htmlContainer.setText(formatter.apply(value));
    }

    protected void updateHtmlContainerText(HtmlContainer htmlContainer,
                                           Formatter<Collection<?>> formatter,
                                           @Nullable Collection<?> value) {
        htmlContainer.setText(formatter.apply(value));
    }

    protected void unbind(HtmlContainer htmlContainer, boolean warnIfBound) {
        checkNotNullArgument(htmlContainer);

        Iterator<Pair<HtmlContainer, DataBindingListenerCleaner>> iterator = listenersRegistrations.iterator();
        while (iterator.hasNext()) {
            Pair<HtmlContainer, DataBindingListenerCleaner> htmlContainerAndCleaner = iterator.next();
            if (htmlContainer.equals(htmlContainerAndCleaner.getFirst())) {
                htmlContainerAndCleaner.getSecond().remove();
                iterator.remove();

                if (warnIfBound) {
                    log.warn("Silent unbind. HtmlContainer with id = '{}' was already bounden with data source.",
                            htmlContainer.getId());
                }
            }
        }
    }

    @FunctionalInterface
    protected interface DataBindingListenerCleaner {
        void remove();
    }
}
