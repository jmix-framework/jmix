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
package io.jmix.ui.component.impl;

import io.jmix.core.MetadataTools;
import io.jmix.ui.component.TwinColumn;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.meta.OptionsBinding;
import io.jmix.ui.component.data.options.OptionsBinder;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.widget.JmixTwinColSelect;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TwinColumnImpl<V> extends AbstractField<JmixTwinColSelect<V>, Set<V>, Collection<V>>
        implements TwinColumn<V>, InitializingBean {

    protected OptionsBinding<V> optionsBinding;

    protected Function<? super V, String> optionCaptionProvider;
    protected Function<? super V, String> optionStyleProvider;

    protected MetadataTools metadataTools;

    @Autowired
    protected IconResolver iconResolver;

    public TwinColumnImpl() {
        component = createComponent();
        attachValueChangeListener(component);
    }

    protected JmixTwinColSelect<V> createComponent() {
        return new JmixTwinColSelect<>();
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);
    }

    protected void initComponent(JmixTwinColSelect<V> component) {
        component.setItemCaptionGenerator(this::generateItemCaption);
    }

    @Autowired
    protected void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Override
    public void setOptions(@Nullable Options<V> options) {
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

    protected void setItemsToPresentation(Stream<V> options) {
        component.setItems(options);

        // set value to Vaadin component as it removes value after setItems
        Collection<V> optionValues = getValue();
        if (CollectionUtils.isNotEmpty(optionValues) && getOptions() != null) {
            List<V> items = getOptions().getOptions().collect(Collectors.toList());

            Set<V> values = new HashSet<>();
            for (V value : optionValues) {
                if (items.contains(value)) {
                    values.add(value);
                }
            }

            component.setValue(values);
        }
    }

    @Override
    protected Set<V> convertToPresentation(@Nullable Collection<V> modelValue) throws ConversionException {
        if (modelValue instanceof Set) {
            return (Set<V>) modelValue;
        }

        return modelValue == null ?
                new LinkedHashSet<>() : new LinkedHashSet<>(modelValue);
    }

    @Override
    protected Collection<V> convertToModel(@Nullable Set<V> componentRawValue) throws ConversionException {
        Stream<V> items;
        if (optionsBinding == null || componentRawValue == null) {
            items = Stream.empty();
        } else {
            Stream<V> options = optionsBinding.getSource().getOptions();
            if (isReorderable()) {
                items = options.filter(componentRawValue::contains);
            } else {
                Set<V> optionsSet = options.collect(Collectors.toSet());
                items = componentRawValue.stream().filter(optionsSet::contains);
            }
        }

        if (valueBinding != null) {
            Class<?> targetType = valueBinding.getSource().getType();

            if (List.class.isAssignableFrom(targetType)) {
                return items.collect(Collectors.toList());
            } else if (Set.class.isAssignableFrom(targetType)) {
                return items.collect(Collectors.toCollection(LinkedHashSet::new));
            }
        }

        return items.collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Nullable
    @Override
    public Options<V> getOptions() {
        return optionsBinding != null ? optionsBinding.getSource() : null;
    }

    @Override
    public void setOptionCaptionProvider(@Nullable Function<? super V, String> captionProvider) {
        if (this.optionCaptionProvider != captionProvider) {
            this.optionCaptionProvider = captionProvider;

            component.setItemCaptionGenerator(this::generateItemCaption);
        }
    }

    @Nullable
    protected String generateItemCaption(@Nullable V item) {
        if (item == null) {
            return null;
        }

        if (optionCaptionProvider != null) {
            return optionCaptionProvider.apply(item);
        }

        return generateDefaultItemCaption(item);
    }

    protected String generateDefaultItemCaption(V item) {
        if (valueBinding != null && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            return metadataTools.format(item, entityValueSource.getMetaPropertyPath().getMetaProperty());
        }

        return metadataTools.format(item);
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionCaptionProvider() {
        return optionCaptionProvider;
    }

    @Override
    public int getRows() {
        return component.getRows();
    }

    @Override
    public void setRows(int rows) {
        component.setRows(rows);
    }

    @Override
    public void setOptionStyleProvider(@Nullable Function<? super V, String> optionStyleProvider) {
        if (this.optionStyleProvider != optionStyleProvider) {
            this.optionStyleProvider = optionStyleProvider;

            component.setOptionStyleProvider(this::generateItemStylename);
        }
    }

    @Nullable
    protected String generateItemStylename(V item) {
        if (optionStyleProvider == null) {
            return null;
        }

        return this.optionStyleProvider.apply(item);
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionStyleProvider() {
        return optionStyleProvider;
    }

    @Override
    public void setAddAllBtnEnabled(boolean enabled) {
        component.setAddAllBtnEnabled(enabled);
    }

    @Override
    public boolean isAddAllBtnEnabled() {
        return component.isAddAllBtnEnabled();
    }

    @StudioProperty(defaultValue = "true")
    @Override
    public void setReorderable(boolean reorderable) {
        component.setReorderable(reorderable);
    }

    @Override
    public boolean isReorderable() {
        return component.isReorderable();
    }

    @Override
    public void setLeftColumnCaption(@Nullable String leftColumnCaption) {
        component.setLeftColumnCaption(leftColumnCaption);
    }

    @Nullable
    @Override
    public String getLeftColumnCaption() {
        return component.getLeftColumnCaption();
    }

    @Override
    public void setRightColumnCaption(@Nullable String rightColumnCaption) {
        component.setRightColumnCaption(rightColumnCaption);
    }

    @Nullable
    @Override
    public String getRightColumnCaption() {
        return component.getRightColumnCaption();
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
                || CollectionUtils.isEmpty(getValue());
    }
}
