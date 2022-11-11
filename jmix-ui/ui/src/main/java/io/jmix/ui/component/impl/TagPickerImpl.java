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

import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.TagPicker;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.meta.OptionsBinding;
import io.jmix.ui.component.data.options.OptionsBinder;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.widget.JmixComboBox;
import io.jmix.ui.widget.JmixPickerField;
import io.jmix.ui.widget.JmixTagPicker;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagPickerImpl<V> extends ValuesPickerImpl<V> implements TagPicker<V>, InitializingBean {

    public static final String NOACTION_STYLENAME = "no-actions";

    protected OptionsBinding<V> optionsBinding;
    protected Function<? super V, String> optionCaptionProvider;
    protected Function<? super V, String> tagCaptionProvider;

    protected FilterMode filterMode = FilterMode.CONTAINS;
    protected boolean hideSelectedOptions = true;

    protected MetaClass metaClass;

    public TagPickerImpl() {
        component = createComponent();

        attachValueChangeListener(component);
    }

    @Override
    protected JmixTagPicker<V> createComponent() {
        return new JmixTagPicker<>();
    }

    protected void initComponent(JmixPickerField<Collection<V>> component) {
        super.initComponent(component);

        getJmixTagPicker().setTagCaptionProvider(this::generateTagCaption);
        getFieldInternal().setItemCaptionGenerator(this::generateOptionCaption);
    }

    @Override
    protected void setValueInternal(@Nullable Collection<V> value, boolean userOriginated) {
        super.setValueInternal(value, userOriginated);

        hideSelectedOptionsInPresentation(value);
    }

    @Nullable
    @Override
    public Options<V> getOptions() {
        return optionsBinding != null ? optionsBinding.getSource() : null;
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
        getFieldInternal().setItems(this::filterItem, options.collect(Collectors.toList()));
    }

    protected boolean filterItem(String itemCaption, String filterText) {
        if (filterMode == FilterMode.NO) {
            return true;
        }

        if (filterMode == FilterMode.STARTS_WITH) {
            return StringUtils.startsWithIgnoreCase(itemCaption, filterText);
        }

        return StringUtils.containsIgnoreCase(itemCaption, filterText);
    }

    @Override
    public void setOptionCaptionProvider(@Nullable Function<? super V, String> optionCaptionProvider) {
        if (this.optionCaptionProvider != optionCaptionProvider) {
            this.optionCaptionProvider = optionCaptionProvider;

            getFieldInternal().setItemCaptionGenerator(this::generateOptionCaption);
        }
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionCaptionProvider() {
        return optionCaptionProvider;
    }

    @Nullable
    @Override
    public Function<? super V, String> getTagCaptionProvider() {
        return tagCaptionProvider;
    }

    @Override
    public void setTagCaptionProvider(@Nullable Function<? super V, String> tagCaptionProvider) {
        if (this.tagCaptionProvider != tagCaptionProvider) {
            this.tagCaptionProvider = tagCaptionProvider;

            getJmixTagPicker().refreshTags();
        }
    }

    @Nullable
    @Override
    public MetaClass getMetaClass() {
        ValueSource<Collection<V>> valueSource = getValueSource();
        if (valueSource instanceof EntityValueSource) {
            MetaProperty metaProperty = ((EntityValueSource) valueSource).getMetaPropertyPath().getMetaProperty();
            return metaProperty.getRange().asClass();
        } else {
            return metaClass;
        }
    }

    @Override
    public void setMetaClass(MetaClass metaClass) {
        ValueSource<Collection<V>> valueSource = getValueSource();
        if (valueSource != null) {
            throw new IllegalStateException("ValueSource is not null");
        }
        this.metaClass = metaClass;
    }

    @Override
    public int getPageLength() {
        return getFieldInternal().getPageLength();
    }

    @Override
    public void setPageLength(int pageLength) {
        getFieldInternal().setPageLength(pageLength);
    }

    @Override
    public boolean isHideSelectedOptions() {
        return hideSelectedOptions;
    }

    @Override
    public void setHideSelectedOptions(boolean hide) {
        hideSelectedOptions = hide;
    }

    @Override
    public Subscription addTagClickListener(Consumer<TagClickEvent<V>> listener) {
        if (!getEventHub().hasSubscriptions(TagClickEvent.class)) {
            getJmixTagPicker().setTagClickHandler(this::onTagClick);
        }

        getEventHub().subscribe(TagClickEvent.class, (Consumer) listener);

        return removeTagLabelClickListener(listener);
    }

    protected Subscription removeTagLabelClickListener(Consumer<TagClickEvent<V>> listener) {
        return () -> {
            getEventHub().unsubscribe(TagClickEvent.class, (Consumer) listener);

            if (!getEventHub().hasSubscriptions(TagClickEvent.class)) {
                getJmixTagPicker().setTagClickHandler(null);
            }
        };
    }

    @Nullable
    @Override
    public Function<? super V, String> getTagStyleProvider() {
        return getJmixTagPicker().getTagStyleProvider();
    }

    @Override
    public void setTagStyleProvider(@Nullable Function<? super V, String> tagStyleProvider) {
        getJmixTagPicker().setTagStyleProvider(tagStyleProvider);
    }

    @Override
    public FilterMode getFilterMode() {
        return filterMode;
    }

    @Override
    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
    }

    @Override
    public String getInputPrompt() {
        return getFieldInternal().getPlaceholder();
    }

    @Override
    public void setInputPrompt(String inputPrompt) {
        getFieldInternal().setPlaceholder(inputPrompt);
    }

    @Override
    public boolean isInlineTags() {
        return getJmixTagPicker().isInlineTags();
    }

    @Override
    public void setInlineTags(boolean inline) {
        getJmixTagPicker().setInlineTags(inline);
    }

    @Override
    public TagPosition getTagPosition() {
        return TagPosition.valueOf(getJmixTagPicker().getTagContainerPosition().name());
    }

    @Override
    public void setTagPosition(TagPosition position) {
        getJmixTagPicker().setTagContainerPosition(JmixTagPicker.TagContainerPosition.valueOf(position.name()));
    }

    @Override
    @Nullable
    public Comparator<? super V> getTagComparator() {
        return getJmixTagPicker().getTagComparator();
    }

    @Override
    public void setTagComparator(@Nullable Comparator<? super V> tagComparator) {
        getJmixTagPicker().setTagComparator(tagComparator);
    }

    @Override
    public void setFormatter(@Nullable Formatter<? super Collection<V>> formatter) {
        throw new UnsupportedOperationException("Formatter is not available for " + getClass().getCanonicalName());
    }

    @Nullable
    @Override
    public Formatter<Collection<V>> getFormatter() {
        throw new UnsupportedOperationException("Formatter is not available for " + getClass().getCanonicalName());
    }

    @Override
    public Subscription addFieldValueChangeListener(Consumer<FieldValueChangeEvent<Collection<V>>> listener) {
        if (getFieldInternal().getNewItemHandler() == null) {
            getFieldInternal().setNewItemHandler(this::onNewItemEntered);
        }

        Subscription subscription = getEventHub().subscribe(FieldValueChangeEvent.class, (Consumer) listener);

        return () -> {
            subscription.remove();

            if (!hasSubscriptions(FieldValueChangeEvent.class)) {
                getFieldInternal().setNewItemHandler(null);
            }
        };
    }

    @Override
    public void addAction(Action action, int index) {
        super.addAction(action, index);

        updateNoActionStyle();
    }

    @Override
    public void removeAction(Action action) {
        super.removeAction(action);

        updateNoActionStyle();
    }

    protected void updateNoActionStyle() {
        component.removeStyleName(NOACTION_STYLENAME);

        if (CollectionUtils.isEmpty(actions)) {
            component.addStyleName(NOACTION_STYLENAME);
        }
    }

    @Override
    protected void componentValueChanged(Collection<V> prevComponentValue, Collection<V> newComponentValue, boolean isUserOriginated) {
        super.componentValueChanged(prevComponentValue, newComponentValue, isUserOriginated);

        hideSelectedOptionsInPresentation(newComponentValue);
    }

    protected void hideSelectedOptionsInPresentation(@Nullable Collection<V> compValue) {
        if (optionsBinding == null || !hideSelectedOptions) {
            return;
        }

        Stream<V> items = optionsBinding.getSource().getOptions();

        getFieldInternal().setItems(CollectionUtils.isEmpty(compValue)
                ? items.collect(Collectors.toList())
                : items.filter(item -> !compValue.contains(item))
                .collect(Collectors.toList()));
    }

    @Nullable
    protected String generateOptionCaption(@Nullable V item) {
        if (item == null) {
            return null;
        }

        if (optionCaptionProvider != null) {
            return optionCaptionProvider.apply(item);
        }

        return generateDefaultItemCaption(item);
    }

    @Nullable
    protected String generateTagCaption(@Nullable V item) {
        if (item == null) {
            return null;
        }

        if (tagCaptionProvider != null) {
            return tagCaptionProvider.apply(item);
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

    protected JmixTagPicker<V> getJmixTagPicker() {
        return (JmixTagPicker<V>) component;
    }

    protected JmixComboBox<V> getFieldInternal() {
        //noinspection unchecked
        return (JmixComboBox<V>) getJmixTagPicker().getFieldInternal();
    }

    protected void onTagClick(V item) {
        publish(TagClickEvent.class, new TagClickEvent<>(this, item));
    }

    protected void onNewItemEntered(String itemCaption) {
        FieldValueChangeEvent<Collection<V>> event = new FieldValueChangeEvent<>(this, itemCaption, getValue());
        publish(FieldValueChangeEvent.class, event);
    }
}
