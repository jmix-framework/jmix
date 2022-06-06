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

import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Resource;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.component.SecuredActionsHolder;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.meta.EntityOptions;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.meta.OptionsBinding;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.component.data.options.OptionsBinder;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.widget.JmixComboBoxPickerField;
import io.jmix.ui.widget.JmixPickerField;
import io.jmix.ui.widget.ShortcutListenerDelegate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.ui.component.impl.ComboBoxImpl.NULL_ITEM_ICON_GENERATOR;
import static io.jmix.ui.component.impl.ComboBoxImpl.NULL_STYLE_GENERATOR;

public class EntityComboBoxImpl<V> extends EntityPickerImpl<V>
        implements EntityComboBox<V>, SecuredActionsHolder {

    protected boolean nullOptionVisible = true;

    protected FilterMode filterMode = FilterMode.CONTAINS;
    protected Predicate<OptionsCaptionFilteringContext> optionsCaptionFilter;

    protected Consumer<EnterPressEvent> enterPressHandler;

    protected Function<? super V, String> optionCaptionProvider;
    protected Function<? super V, String> optionIconProvider;
    protected Function<? super V, io.jmix.ui.component.Resource> optionImageProvider;
    protected Function<? super V, String> optionStyleProvider;

    protected OptionsBinding<V> optionsBinding;

    protected IconResolver iconResolver;

    public EntityComboBoxImpl() {
    }

    @Override
    protected JmixPickerField<V> createComponent() {
        return new JmixComboBoxPickerField<>();
    }

    @Override
    public JmixComboBoxPickerField<V> getComponent() {
        return (JmixComboBoxPickerField<V>) super.getComponent();
    }

    @Autowired
    public void setIconResolver(IconResolver iconResolver) {
        this.iconResolver = iconResolver;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        setPageLength(getUiComponentProperties().getComboBoxPageLength());
    }

    @Override
    protected void initComponent(JmixPickerField<V> component) {
        JmixComboBoxPickerField<V> impl = (JmixComboBoxPickerField<V>) component;
        impl.setItemCaptionGenerator(this::generateItemCaption);
        impl.getFieldInternal().setCustomValueEquals(this::fieldValueEquals);

        component.addShortcutListener(new ShortcutListenerDelegate("clearShortcut",
                ShortcutAction.KeyCode.DELETE, new int[]{ShortcutAction.ModifierKey.SHIFT})
                .withHandler(this::handleClearShortcut));
    }

    protected void handleClearShortcut(@SuppressWarnings("unused") Object sender, @SuppressWarnings("unused") Object target) {
        if (!isRequired()
                && isEnabledRecursive()
                && isEditableWithParent()) {
            setValue(null);
        }
    }

    protected String generateDefaultItemCaption(V item) {
        if (valueBinding != null && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            return metadataTools.format(item, entityValueSource.getMetaPropertyPath().getMetaProperty());
        }

        return metadataTools.format(item);
    }

    protected String generateItemCaption(@Nullable V item) {
        if (item == null) {
            return "";
        }

        if (optionCaptionProvider != null) {
            return optionCaptionProvider.apply(item);
        }

        return generateDefaultItemCaption(item);
    }

    @Nullable
    protected String generateItemStylename(V item) {
        if (optionStyleProvider == null) {
            return null;
        }

        return this.optionStyleProvider.apply(item);
    }

    @Override
    public String getNullSelectionCaption() {
        return getComponent().getEmptySelectionCaption();
    }

    @Override
    public void setNullSelectionCaption(String nullOption) {
        getComponent().setEmptySelectionCaption(nullOption);

        setInputPrompt(null);
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
    public Subscription addFieldValueChangeListener(Consumer<FieldValueChangeEvent<V>> listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFieldEditable() {
        return false;
    }

    @Override
    public void setFieldEditable(boolean editable) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Formatter<V> getFormatter() {
        return null;
    }

    @Override
    public void setFormatter(@Nullable Formatter<? super V> formatter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTextInputAllowed() {
        return getComponent().isTextInputAllowed();
    }

    @Override
    public void setTextInputAllowed(boolean textInputAllowed) {
        getComponent().setTextInputAllowed(textInputAllowed);
    }

    @Override
    public void setAutomaticPopupOnFocus(boolean automaticPopupOnFocus) {
    }

    @Override
    public boolean isAutomaticPopupOnFocus() {
        return false;
    }

    @Nullable
    @Override
    public Consumer<EnterPressEvent> getEnterPressHandler() {
        return enterPressHandler;
    }

    @Override
    public void setEnterPressHandler(@Nullable Consumer<EnterPressEvent> handler) {
        this.enterPressHandler = handler;

        if (handler != null
                && getComponent().getNewItemHandler() == null) {
            getComponent().setNewItemHandler(this::onEnterPressed);
        }

        if (handler == null
                && getComponent().getNewItemHandler() != null) {
            getComponent().setNewItemHandler(null);
        }
    }

    protected void onEnterPressed(String text) {
        if (enterPressHandler == null) {
            throw new IllegalStateException("EnterPress handler cannot be NULL");
        }
        enterPressHandler.accept(new EnterPressEvent(this, text));
    }

    @Override
    public int getPageLength() {
        return getComponent().getPageLength();
    }

    @Override
    public void setPageLength(int pageLength) {
        getComponent().setPageLength(pageLength);
    }

    @Override
    public boolean isNullOptionVisible() {
        return nullOptionVisible;
    }

    @Override
    public void setNullOptionVisible(boolean nullOptionVisible) {
        this.nullOptionVisible = nullOptionVisible;

        getComponent().setEmptySelectionAllowed(!isRequired() && nullOptionVisible);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setOptionIconProvider(@Nullable Function<? super V, String> optionIconProvider) {
        if (this.optionIconProvider != optionIconProvider) {
            this.optionIconProvider = optionIconProvider;

            if (optionIconProvider != null) {
                getComponent().setItemIconGenerator(this::generateOptionIcon);
            } else {
                getComponent().setItemIconGenerator(NULL_ITEM_ICON_GENERATOR);
            }
        }
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionIconProvider() {
        return optionIconProvider;
    }

    protected Resource generateOptionIcon(@Nullable V item) {
        String resourceId;
        try {
            resourceId = optionIconProvider.apply(item);
        } catch (Exception e) {
            LoggerFactory.getLogger(EntityComboBoxImpl.class)
                    .warn("Error invoking optionIconProvider apply method", e);
            return null;
        }

        return iconResolver.getIconResource(resourceId);
    }

    @Nullable
    @Override
    public Function<? super V, String> getFieldIconProvider() {
        return null;
    }

    @Override
    public void setFieldIconProvider(@Nullable Function<? super V, String> iconProvider) {
        throw new UnsupportedOperationException("Use options icon provider instead");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setOptionImageProvider(@Nullable Function<? super V, io.jmix.ui.component.Resource> optionImageProvider) {
        if (this.optionImageProvider != optionImageProvider) {
            this.optionImageProvider = optionImageProvider;

            if (optionImageProvider != null) {
                getComponent().setItemIconGenerator(this::generateOptionImage);
            } else {
                getComponent().setItemIconGenerator(NULL_ITEM_ICON_GENERATOR);
            }
        }
    }

    @Nullable
    @Override
    public Function<? super V, io.jmix.ui.component.Resource> getOptionImageProvider() {
        return optionImageProvider;
    }

    @Nullable
    protected Resource generateOptionImage(V item) {
        io.jmix.ui.component.Resource resource;
        try {
            resource = optionImageProvider.apply(item);
        } catch (Exception e) {
            LoggerFactory.getLogger(ComboBoxImpl.class)
                    .warn("Error invoking OptionImageProvider apply method", e);
            return null;
        }

        return resource != null && ((WebResource) resource).hasSource()
                ? ((WebResource) resource).getResource()
                : null;
    }

    @Nullable
    @Override
    public Predicate<OptionsCaptionFilteringContext> getOptionsCaptionFilter() {
        return optionsCaptionFilter;
    }

    @Override
    public void setOptionsCaptionFilter(@Nullable Predicate<OptionsCaptionFilteringContext> filter) {
        this.optionsCaptionFilter = filter;
    }

    @Nullable
    @Override
    public String getPopupWidth() {
        return getComponent().getPopupWidth();
    }

    @Override
    public void setPopupWidth(@Nullable String width) {
        getComponent().setPopupWidth(width);
    }

    @Override
    public void setInputPrompt(@Nullable String inputPrompt) {
        if (StringUtils.isNotBlank(inputPrompt)) {
            setNullSelectionCaption(generateItemCaption(null));
        }
        super.setInputPrompt(inputPrompt);
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

            if (getMetaClass() == null
                    && options instanceof EntityOptions) {
                setMetaClass(((EntityOptions<V>) options).getEntityMetaClass());
            }
        }
    }

    @Override
    public void setOptionsContainer(CollectionContainer<V> container) {
        setOptions(new ContainerOptions<>(container));
    }

    protected void setItemsToPresentation(Stream<V> options) {
        getComponent().setItems(this::filterItemTest, options.collect(Collectors.toList()));
    }

    protected boolean filterItemTest(String itemCaption, String filterText) {
        if (optionsCaptionFilter != null) {
            OptionsCaptionFilteringContext context = new OptionsCaptionFilteringContext(itemCaption, filterText);
            return optionsCaptionFilter.test(context);
        }

        if (filterMode == FilterMode.NO) {
            return true;
        }

        if (filterMode == FilterMode.STARTS_WITH) {
            return StringUtils.startsWithIgnoreCase(itemCaption, filterText);
        }

        return StringUtils.containsIgnoreCase(itemCaption, filterText);
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionCaptionProvider() {
        return optionCaptionProvider;
    }

    @Override
    public void setOptionCaptionProvider(@Nullable Function<? super V, String> optionCaptionProvider) {
        if (this.optionCaptionProvider != optionCaptionProvider) {
            this.optionCaptionProvider = optionCaptionProvider;

            // reset item captions
            getComponent().setItemCaptionGenerator(this::generateItemCaption);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setOptionStyleProvider(@Nullable Function<? super V, String> optionStyleProvider) {
        if (this.optionStyleProvider != optionStyleProvider) {
            this.optionStyleProvider = optionStyleProvider;

            if (optionStyleProvider != null) {
                getComponent().setStyleGenerator(this::generateItemStylename);
            } else {
                getComponent().setStyleGenerator(NULL_STYLE_GENERATOR);
            }
        }
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionStyleProvider() {
        return optionStyleProvider;
    }

    @Override
    protected void checkValueType(@Nullable V value) {
        // do not check
    }

    @Override
    protected void componentValueChanged(V prevComponentValue, V newComponentValue, boolean isUserOriginated) {
        super.componentValueChanged(prevComponentValue, newComponentValue, isUserOriginated);

        refreshActionsState();
    }
}
