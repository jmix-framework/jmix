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

import com.vaadin.server.Resource;
import com.vaadin.ui.IconGenerator;
import com.vaadin.ui.StyleGenerator;
import io.jmix.core.MetadataTools;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.meta.OptionsBinding;
import io.jmix.ui.component.data.options.OptionsBinder;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.widget.JmixComboBox;
import io.jmix.ui.widget.ShortcutListenerDelegate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vaadin.event.ShortcutAction.KeyCode;
import static com.vaadin.event.ShortcutAction.ModifierKey;

public class WebComboBox<V> extends WebV8AbstractField<JmixComboBox<V>, V, V>
        implements ComboBox<V>, InitializingBean {

    public static final StyleGenerator NULL_STYLE_GENERATOR = item -> null;
    public static final IconGenerator NULL_ITEM_ICON_GENERATOR = item -> null;

    protected ComboBox.FilterMode filterMode = FilterMode.CONTAINS;

    protected boolean nullOptionVisible = true;

    protected Consumer<String> newOptionHandler;

    protected Function<? super V, String> optionIconProvider;
    protected Function<? super V, io.jmix.ui.component.Resource> optionImageProvider;
    protected Function<? super V, String> optionCaptionProvider;
    protected Function<? super V, String> optionStyleProvider;

    protected FilterPredicate filterPredicate;

    protected MetadataTools metadataTools;
    protected IconResolver iconResolver;

    protected OptionsBinding<V> optionsBinding;

    public WebComboBox() {
        this.component = createComponent();

        attachValueChangeListener(component);
    }

    protected JmixComboBox<V> createComponent() {
        return new JmixComboBox<>();
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setIconResolver(IconResolver iconResolver) {
        this.iconResolver = iconResolver;
    }

    protected void handleClearShortcut(@SuppressWarnings("unused") Object sender, @SuppressWarnings("unused") Object target) {
        if (!isRequired()
                && isEnabledRecursive()
                && isEditableWithParent()) {
            setValue(null);
        }
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);

        setPageLength(applicationContext.getBean(UiProperties.class).getLookupFieldPageLength());
    }

    protected void initComponent(JmixComboBox<V> component) {
        component.setItemCaptionGenerator(this::generateItemCaption);

        component.addShortcutListener(new ShortcutListenerDelegate("clearShortcut",
                KeyCode.DELETE, new int[]{ModifierKey.SHIFT})
                        .withHandler(this::handleClearShortcut));
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

    protected boolean filterItemTest(String itemCaption, String filterText) {
        if (filterPredicate != null) {
            return filterPredicate.test(itemCaption, filterText);
        }

        if (filterMode == FilterMode.NO) {
            return true;
        }

        if (filterMode == FilterMode.STARTS_WITH) {
            return StringUtils.startsWithIgnoreCase(itemCaption, filterText);
        }

        return StringUtils.containsIgnoreCase(itemCaption, filterText);
    }

    @Override
    protected void valueBindingConnected(ValueSource<V> valueSource) {
        super.valueBindingConnected(valueSource);

        if (valueSource instanceof EntityValueSource) {
            DataAwareComponentsTools dataAwareComponentsTools = applicationContext.getBean(DataAwareComponentsTools.class);
            dataAwareComponentsTools.setupOptions(this, (EntityValueSource) valueSource);
        }
    }

    @Nullable
    @Override
    public V getValue() {
        return internalValue;
    }

    @Override
    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
    }

    @Override
    public FilterMode getFilterMode() {
        return filterMode;
    }

    @Override
    public void setRequired(boolean required) {
        super.setRequired(required);

        component.setEmptySelectionAllowed(!required && nullOptionVisible);
    }

    @Override
    public String getNullSelectionCaption() {
        return component.getEmptySelectionCaption();
    }

    @Override
    public void setNullSelectionCaption(String nullOption) {
        component.setEmptySelectionCaption(nullOption);

        setInputPrompt(null);
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
            OptionsBinder optionsBinder = (OptionsBinder) applicationContext.getBean(OptionsBinder.NAME);
            this.optionsBinding = optionsBinder.bind(options, this, this::setItemsToPresentation);
            this.optionsBinding.activate();
        }
    }

    protected void setItemsToPresentation(Stream<V> options) {
        component.setItems(this::filterItemTest, options.collect(Collectors.toList()));
    }

    @Override
    public void setOptionCaptionProvider(@Nullable Function<? super V, String> optionCaptionProvider) {
        if (this.optionCaptionProvider != optionCaptionProvider) {
            this.optionCaptionProvider = optionCaptionProvider;

            // reset item captions
            component.setItemCaptionGenerator(this::generateItemCaption);
        }
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionCaptionProvider() {
        return optionCaptionProvider;
    }

    protected void onNewItemEntered(String newItemCaption) {
        if (newOptionHandler == null) {
            throw new IllegalStateException("New item handler cannot be NULL");
        }
        newOptionHandler.accept(newItemCaption);
    }

    @Override
    public boolean isTextInputAllowed() {
        return component.isTextInputAllowed();
    }

    @Override
    public void setTextInputAllowed(boolean textInputAllowed) {
        component.setTextInputAllowed(textInputAllowed);
    }

    @Override
    public boolean isAutomaticPopupOnFocus() {
        return false;
    }

    @Override
    public void setAutomaticPopupOnFocus(boolean popup) {}

    @Nullable
    @Override
    public Consumer<String> getNewOptionHandler() {
        return newOptionHandler;
    }

    @Override
    public void setNewOptionHandler(@Nullable Consumer<String> newOptionHandler) {
        this.newOptionHandler = newOptionHandler;

        if (newOptionHandler != null
                && component.getNewItemHandler() == null) {
            component.setNewItemHandler(this::onNewItemEntered);
        }

        if (newOptionHandler == null
                && component.getNewItemHandler() != null) {
            component.setNewItemHandler(null);
        }
    }

    @Override
    public int getPageLength() {
        return component.getPageLength();
    }

    @Override
    public void setPageLength(int pageLength) {
        component.setPageLength(pageLength);
    }

    @Override
    public void setNullOptionVisible(boolean nullOptionVisible) {
        this.nullOptionVisible = nullOptionVisible;

        component.setEmptySelectionAllowed(!isRequired() && nullOptionVisible);
    }

    @Override
    public boolean isNullOptionVisible() {
        return nullOptionVisible;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setOptionIconProvider(@Nullable Function<? super V, String> optionIconProvider) {
        if (this.optionIconProvider != optionIconProvider) {
            this.optionIconProvider = optionIconProvider;

            if (optionIconProvider != null) {
                component.setItemIconGenerator(this::generateOptionIcon);
            } else {
                component.setItemIconGenerator(NULL_ITEM_ICON_GENERATOR);
            }
        }
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionIconProvider() {
        return optionIconProvider;
    }

    @Nullable
    protected Resource generateOptionIcon(V item) {
        String resourceId;
        try {
            resourceId = optionIconProvider.apply(item);
        } catch (Exception e) {
            LoggerFactory.getLogger(WebComboBox.class)
                    .warn("Error invoking optionIconProvider apply method", e);
            return null;
        }

        return iconResolver.getIconResource(resourceId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setOptionImageProvider(@Nullable Function<? super V, io.jmix.ui.component.Resource> optionImageProvider) {
        if (this.optionImageProvider != optionImageProvider) {
            this.optionImageProvider = optionImageProvider;

            if (optionImageProvider != null) {
                component.setItemIconGenerator(this::generateOptionImage);
            } else {
                component.setItemIconGenerator(NULL_ITEM_ICON_GENERATOR);
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
            LoggerFactory.getLogger(WebComboBox.class)
                    .warn("Error invoking optionImageProvider apply method", e);
            return null;
        }

        return resource != null && ((WebResource) resource).hasSource()
                ? ((WebResource) resource).getResource()
                : null;
    }

    @Nullable
    @Override
    public String getInputPrompt() {
        return component.getPlaceholder();
    }

    @Override
    public void setInputPrompt(@Nullable String inputPrompt) {
        if (StringUtils.isNotBlank(inputPrompt)) {
            setNullSelectionCaption(generateItemCaption(null));
        }
        component.setPlaceholder(inputPrompt);
    }

    @Override
    public void setLookupSelectHandler(Consumer selectHandler) {
        // do nothing
    }

    @Override
    public Collection getLookupSelectedItems() {
        return Collections.singleton(getValue());
    }

    @Override
    public void commit() {
        super.commit();
    }

    @Override
    public void discard() {
        super.discard();
    }

    @Override
    public boolean isBuffered() {
        return super.isBuffered();
    }

    @Override
    public void setBuffered(boolean buffered) {
        super.setBuffered(buffered);
    }

    @Override
    public boolean isModified() {
        return super.isModified();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setOptionStyleProvider(@Nullable Function<? super V, String> optionStyleProvider) {
        if (this.optionStyleProvider != optionStyleProvider) {
            this.optionStyleProvider = optionStyleProvider;

            if (optionStyleProvider != null) {
                component.setStyleGenerator(this::generateItemStylename);
            } else {
                component.setStyleGenerator(NULL_STYLE_GENERATOR);
            }
        }
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionStyleProvider() {
        return optionStyleProvider;
    }

    @Override
    public void setFilterPredicate(@Nullable FilterPredicate filterPredicate) {
        this.filterPredicate = filterPredicate;
    }

    @Nullable
    @Override
    public FilterPredicate getFilterPredicate() {
        return filterPredicate;
    }

    @Nullable
    @Override
    public String getPopupWidth() {
        return component.getPopupWidth();
    }

    @Override
    public void setPopupWidth(@Nullable String width) {
        component.setPopupWidth(width);
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
}
