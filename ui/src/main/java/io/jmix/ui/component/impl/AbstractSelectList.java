/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component.impl;

import com.vaadin.data.provider.ListDataProvider;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.SelectList;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.meta.OptionsBinding;
import io.jmix.ui.component.data.options.OptionsBinder;
import io.jmix.ui.widget.listselect.JmixAbstractListSelect;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base class for list select components.
 *
 * @param <V> single value type or Collection
 * @param <I> item type
 * @param <T> Vaadin component type
 */
public abstract class AbstractSelectList<V, I, T extends JmixAbstractListSelect<I>>
        extends AbstractField<T, Set<I>, V>
        implements SelectList<V, I>, InitializingBean {

    protected MetadataTools metadataTools;

    protected OptionsBinding<I> optionsBinding;

    protected Function<? super I, String> optionCaptionProvider;

    public AbstractSelectList() {
        component = createComponent();
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    protected abstract T createComponent();

    @Override
    public void afterPropertiesSet() throws Exception {
        initComponent(component);
    }

    protected void initComponent(T component) {
        component.setDataProvider(new ListDataProvider<>(Collections.emptyList()));
        component.setItemCaptionGenerator(this::generateItemCaption);
        component.setRequiredError(null);

        component.setDoubleClickHandler(this::onDoubleClick);

        attachValueChangeListener(component);
    }

    @Override
    public void setOptionCaptionProvider(@Nullable Function<? super I, String> optionCaptionProvider) {
        this.optionCaptionProvider = optionCaptionProvider;

        component.markAsDirty();
    }

    @Nullable
    @Override
    public Function<? super I, String> getOptionCaptionProvider() {
        return optionCaptionProvider;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Subscription addDoubleClickListener(Consumer<DoubleClickEvent<I>> listener) {
        return getEventHub().subscribe(DoubleClickEvent.class, (Consumer) listener);
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @Nullable
    @Override
    public Options<I> getOptions() {
        return optionsBinding != null ? optionsBinding.getSource() : null;
    }

    @Override
    public void setOptions(@Nullable Options<I> options) {
        if (this.optionsBinding != null) {
            this.optionsBinding.unbind();
            this.optionsBinding = null;
        }

        if (options != null) {
            OptionsBinder optionsBinder = applicationContext.getBean(OptionsBinder.class);
            this.optionsBinding = optionsBinder.bind(options, this, this::setItemsToPresentation);
            this.optionsBinding.activate();
        }
    }

    protected void setItemsToPresentation(Stream<I> options) {
        component.setItems(options);

        // set value to Vaadin component as it removes value after setItems
        if (getOptions() != null
                && CollectionUtils.isNotEmpty(getCollectionValue())) {
            List<I> optionItems = getOptions().getOptions().collect(Collectors.toList());

            Set<I> missedValues = getCollectionValue().stream()
                    .filter(optionItems::contains)
                    .collect(Collectors.toSet());

            component.setValue(missedValues);
        }
    }

    protected abstract Collection<I> getCollectionValue();

    @Override
    protected void componentValueChanged(Set<I> prevComponentValue, Set<I> newComponentValue, boolean isUserOriginated) {
        component.markAsDirty();

        super.componentValueChanged(prevComponentValue, newComponentValue, isUserOriginated);
    }

    @Override
    protected void valueBindingConnected(ValueSource<V> valueSource) {
        super.valueBindingConnected(valueSource);

        if (valueSource instanceof EntityValueSource) {
            DataAwareComponentsTools dataAwareComponentsTools = applicationContext.getBean(DataAwareComponentsTools.class);
            dataAwareComponentsTools.setupOptions(this, (EntityValueSource) valueSource);
        }
    }

    protected String generateDefaultItemCaption(I item) {
        if (valueBinding != null && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            return metadataTools.format(item, entityValueSource.getMetaPropertyPath().getMetaProperty());
        }

        return metadataTools.format(item);
    }

    @Nullable
    protected String generateItemCaption(@Nullable I item) {
        if (item == null) {
            return null;
        }

        if (optionCaptionProvider != null) {
            return optionCaptionProvider.apply(item);
        }

        return generateDefaultItemCaption(item);
    }

    protected void onDoubleClick(I item) {
        if (hasSubscriptions(SelectList.DoubleClickEvent.class)) {
            SelectList.DoubleClickEvent<I> event = new SelectList.DoubleClickEvent<>(this, item);
            publish(SelectList.DoubleClickEvent.class, event);
        }
    }
}
