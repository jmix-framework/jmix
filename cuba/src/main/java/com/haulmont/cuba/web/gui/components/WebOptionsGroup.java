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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.OptionsGroup;
import com.vaadin.v7.data.util.IndexedContainer;
import io.jmix.core.MetadataTools;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.meta.OptionsBinding;
import io.jmix.ui.component.data.options.OptionsBinder;
import io.jmix.ui.widget.JmixOptionGroup;
import io.jmix.ui.widget.client.optiongroup.OptionGroupOrientation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

@Deprecated
public class WebOptionsGroup<V, I> extends WebAbstractField<JmixOptionGroup, V> implements OptionsGroup<V, I> {

    protected MetadataTools metadataTools;

    protected OptionsBinding<I> optionsBinding;

    protected Function<? super I, String> optionCaptionProvider;

    @SuppressWarnings("unchecked")
    public WebOptionsGroup() {
        component = createComponent();
        component.setContainerDataSource(new IndexedContainer());
        component.setItemCaptionGenerator(o -> generateItemCaption((I) o));
        component.setRequiredError(null);

        attachListener(component);
    }

    protected JmixOptionGroup createComponent() {
        return new JmixOptionGroup();
    }

    @Override
    public V getValue() {
        return convertToModel(component.getValue());
    }

    @Override
    protected boolean fieldValueEquals(V value, V oldValue) {
        if (!isMultiSelect()) {
            return super.fieldValueEquals(value, oldValue);
        }

        //noinspection unchecked
        return equalCollections((Collection<V>) value, (Collection<V>) oldValue);
    }

    protected boolean equalCollections(Collection<V> a, Collection<V> b) {
        if (a == null && b == null) {
            return true;
        }

        if (a == null || b == null) {
            return false;
        }

        return CollectionUtils.isEqualCollection(a, b);
    }

    protected String generateDefaultItemCaption(I item) {
        if (valueBinding != null && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            return metadataTools.format(item, entityValueSource.getMetaPropertyPath().getMetaProperty());
        }

        return metadataTools.format(item);
    }

    protected String generateItemCaption(I item) {
        if (item == null) {
            return null;
        }

        if (optionCaptionProvider != null) {
            return optionCaptionProvider.apply(item);
        }

        return generateDefaultItemCaption(item);
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Override
    public boolean isMultiSelect() {
        return component.isMultiSelect();
    }

    @Override
    public void setMultiSelect(boolean multiselect) {
        component.setMultiSelect(multiselect);
    }

    @Override
    public Orientation getOrientation() {
        switch (component.getOrientation()) {
            case HORIZONTAL:
                return Orientation.HORIZONTAL;
            case VERTICAL:
                return Orientation.VERTICAL;
            default:
                throw new RuntimeException("Unsupproted orientation of OptionGroup");
        }
    }

    @Override
    public void setOrientation(Orientation orientation) {
        checkNotNull(orientation, "Orientation must not be null");

        if (orientation == Orientation.HORIZONTAL) {
            component.setOrientation(OptionGroupOrientation.HORIZONTAL);
        } else {
            component.setOrientation(OptionGroupOrientation.VERTICAL);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected V convertToModel(Object componentRawValue) {
        if (isMultiSelect()) {
            Set collectionValue = (Set) componentRawValue;

            List<I> itemIds = getCurrentItems();

            //noinspection RedundantCast
            Stream<I> selectedItemsStream = collectionValue.stream()
                    .filter(item -> itemIds.isEmpty()
                            || itemIds.contains((I) item));

            if (valueBinding != null) {
                Class<V> targetType = valueBinding.getSource().getType();

                if (List.class.isAssignableFrom(targetType)) {
                    return (V) selectedItemsStream.collect(Collectors.toList());
                }

                if (Set.class.isAssignableFrom(targetType)) {
                    return (V) selectedItemsStream.collect(Collectors.toCollection(LinkedHashSet::new));
                }
            }

            return (V) selectedItemsStream.collect(Collectors.toCollection(LinkedHashSet::new));
        }

        return super.convertToModel(componentRawValue);
    }

    @SuppressWarnings("unchecked")
    protected List<I> getCurrentItems() {
        IndexedContainer container = (IndexedContainer) component.getContainerDataSource();

        return (List<I>) container.getItemIds();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object convertToPresentation(V modelValue) {
        if (isMultiSelect()) {
            if (modelValue instanceof List) {
                return new LinkedHashSet<I>((Collection<? extends I>) modelValue);
            }
        }

        return super.convertToPresentation(modelValue);
    }

    @Override
    public void setLookupSelectHandler(Consumer selectHandler) {
        // do nothing
    }

    @Override
    public Collection getLookupSelectedItems() {
        Object value = getValue();
        return (value instanceof Collection)
                ? (Collection) value
                : Collections.singleton(value);
    }

    @Override
    public Options<I> getOptions() {
        return optionsBinding != null ? optionsBinding.getSource() : null;
    }

    @Override
    public void setOptions(Options<I> options) {
        if (this.optionsBinding != null) {
            this.optionsBinding.unbind();
            this.optionsBinding = null;
        }

        if (options != null) {
            OptionsBinder optionsBinder = applicationContext.getBean(OptionsBinder.class);
            this.optionsBinding = optionsBinder.bind(options, this, this::setOptionsToComponent);
            this.optionsBinding.activate();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void valueBindingConnected(ValueSource<V> valueSource) {
        super.valueBindingConnected(valueSource);

        if (valueSource instanceof EntityValueSource) {
            DataAwareComponentsTools dataAwareComponentsTools = applicationContext.getBean(DataAwareComponentsTools.class);
            dataAwareComponentsTools.setupOptions(this, (EntityValueSource) valueSource);
        }
    }

    @Override
    protected void setValueToPresentation(Object value) {
        component.setValueToComponent(value);
    }

    protected void setOptionsToComponent(Stream<I> options) {
        List<I> itemIds = options.collect(Collectors.toList());
        component.setContainerDataSource(new IndexedContainer(itemIds));
    }

    @Override
    public void setOptionCaptionProvider(Function<? super I, String> optionCaptionProvider) {
        this.optionCaptionProvider = optionCaptionProvider;
    }

    @Override
    public Function<? super I, String> getOptionCaptionProvider() {
        return optionCaptionProvider;
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


    @Override
    public boolean isEmpty() {
        return super.isEmpty()
                || (getValue() instanceof Collection
                && ((Collection) getValue()).isEmpty());
    }

    @Override
    protected boolean isEmpty(Object value) {
        return super.isEmpty(value)
                || (value instanceof Collection
                && ((Collection) value).isEmpty());
    }

    @Override
    public void addValidator(Consumer<? super V> validator) {
        addValidator(validator::accept);
    }

    @Override
    public void removeValidator(Consumer<V> validator) {
        removeValidator(validator::accept);
    }
}
