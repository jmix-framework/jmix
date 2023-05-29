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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.Frame.NotificationType;
import com.haulmont.cuba.gui.components.PickerField;
import com.haulmont.cuba.gui.components.SearchPickerField;
import com.vaadin.server.Resource;
import io.jmix.core.Entity;
import io.jmix.core.Messages;
import io.jmix.core.QueryUtils;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.SecuredActionsHolder;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.meta.EntityOptions;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.meta.OptionsBinding;
import io.jmix.ui.component.data.options.OptionsBinder;
import io.jmix.ui.component.impl.ComboBoxImpl;
import io.jmix.ui.component.impl.WebResource;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.widget.JmixPickerField;
import io.jmix.ui.widget.JmixSearchSelectPickerField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.ui.component.impl.ComboBoxImpl.NULL_ITEM_ICON_GENERATOR;
import static io.jmix.ui.component.impl.ComboBoxImpl.NULL_STYLE_GENERATOR;

@Deprecated
public class WebSearchPickerField<V extends Entity> extends WebPickerField<V>
        implements SearchPickerField<V>, SecuredActionsHolder {

    protected V nullOption;
    protected boolean nullOptionVisible = true;

    // just stub
    protected FilterMode filterMode = FilterMode.CONTAINS;
    // just stub
    protected FilterPredicate filterPredicate;

    protected Function<? super V, String> optionCaptionProvider;
    protected Function<? super V, String> optionIconProvider;
    protected Function<? super V, io.jmix.ui.component.Resource> optionImageProvider;
    protected Function<? super V, String> optionStyleProvider;

    protected OptionsBinding<V> optionsBinding;

    protected int minSearchStringLength = 0;
    protected Mode mode = Mode.CASE_SENSITIVE;
    protected boolean escapeValueForLike = false;

    protected IconResolver iconResolver;

    protected NotificationType defaultNotificationType = NotificationType.TRAY;

    protected SearchNotifications searchNotifications = createSearchNotifications();

    public WebSearchPickerField() {
    }

    @Override
    protected JmixPickerField<V> createComponent() {
        return new JmixSearchSelectPickerField<>();
    }

    @Inject
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
        Messages messages = applicationContext.getBean(Messages.class);
        setInputPrompt(messages.getMessage("searchPickerField.inputPrompt"));

        getComponent().setItemCaptionGenerator(this::generateItemCaption);
        getComponent().setFilterHandler(this::executeSearch);
    }

    protected String generateItemStylename(V item) {
        if (optionStyleProvider == null) {
            return null;
        }

        return this.optionStyleProvider.apply(item);
    }

    protected void executeSearch(final String newFilter) {
        if (optionsBinding == null || optionsBinding.getSource() == null) {
            return;
        }

        String filterForDs = newFilter;
        if (mode == Mode.LOWER_CASE) {
            filterForDs = StringUtils.lowerCase(newFilter);
        } else if (mode == Mode.UPPER_CASE) {
            filterForDs = StringUtils.upperCase(newFilter);
        }

        if (escapeValueForLike && StringUtils.isNotEmpty(filterForDs)) {
            filterForDs = QueryUtils.escapeForLike(filterForDs);
        }

        /*
        TODO: legacy-ui
        CollectionDatasource optionsDatasource = ((DatasourceOptions) optionsBinding.getSource()).getDatasource();

        if (!isRequired() && StringUtils.isEmpty(filterForDs)) {
            setValue(null);
            if (optionsDatasource.getState() == Datasource.State.VALID) {
                optionsDatasource.clear();
            }
            return;
        }

        if (StringUtils.length(filterForDs) >= minSearchStringLength) {
            optionsDatasource.refresh(Collections.singletonMap(SEARCH_STRING_PARAM, filterForDs));

            if (optionsDatasource.getState() == Datasource.State.VALID) {
                if (optionsDatasource.size() == 0) {
                    if (searchNotifications != null) {
                        searchNotifications.notFoundSuggestions(newFilter);
                    }
                } else if (optionsDatasource.size() == 1) {
                    setValue((V) optionsDatasource.getItems().iterator().next());
                }
            }
        } else {
            if (optionsDatasource.getState() == Datasource.State.VALID) {
                optionsDatasource.clear();
            }

            if (searchNotifications != null && StringUtils.length(newFilter) > 0) {
                searchNotifications.needMinSearchStringLength(newFilter, minSearchStringLength);
            }
        }*/
    }

    protected SearchNotifications createSearchNotifications() {
        return new SearchNotifications() {
            @Override
            public void notFoundSuggestions(String filterString) {
                Messages messages = applicationContext.getBean(Messages.class);

                String message = messages.formatMessage("", "searchSelect.notFound", filterString);
                // TODO: legacy-ui
                // App.getInstance().getWindowManager().showNotification(message, defaultNotificationType);
            }

            @Override
            public void needMinSearchStringLength(String filterString, int minSearchStringLength) {
                Messages messages = applicationContext.getBean(Messages.class);

                String message = messages.formatMessage("", "searchSelect.minimumLengthOfFilter", minSearchStringLength);
                // TODO: legacy-ui
                // App.getInstance().getWindowManager().showNotification(message, defaultNotificationType);
            }
        };
    }

    @Override
    public JmixSearchSelectPickerField<V> getComponent() {
        return (JmixSearchSelectPickerField<V>) super.getComponent();
    }

    @Override
    public Subscription addFieldValueChangeListener(Consumer<PickerField.FieldValueChangeEvent<V>> listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFieldEditable(boolean editable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMinSearchStringLength() {
        return minSearchStringLength;
    }

    @Override
    public void setMinSearchStringLength(int searchStringLength) {
        this.minSearchStringLength = searchStringLength;
    }

    @Override
    public SearchNotifications getSearchNotifications() {
        return searchNotifications;
    }

    @Override
    public void setSearchNotifications(SearchNotifications searchNotifications) {
        this.searchNotifications = searchNotifications;
    }

    @Override
    public NotificationType getDefaultNotificationType() {
        return defaultNotificationType;
    }

    @Override
    public void setDefaultNotificationType(NotificationType defaultNotificationType) {
        this.defaultNotificationType = defaultNotificationType;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public boolean isEscapeValueForLike() {
        return escapeValueForLike;
    }

    @Override
    public void setEscapeValueForLike(boolean escapeValueForLike) {
        this.escapeValueForLike = escapeValueForLike;
    }

    @Override
    public V getNullOption() {
        return nullOption;
    }

    @Override
    public void setNullOption(V nullOption) {
        this.nullOption = nullOption;
        setNullSelectionCaption(generateItemCaption(nullOption));
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

    protected String generateDefaultItemCaption(V item) {
        if (valueBinding != null && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            return metadataTools.format(item, entityValueSource.getMetaPropertyPath().getMetaProperty());
        }

        return metadataTools.format(item);
    }

    protected String generateItemCaption(V item) {
        if (item == null) {
            return "";
        }

        if (optionCaptionProvider != null) {
            return optionCaptionProvider.apply(item);
        }

        return generateDefaultItemCaption(item);
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionCaptionProvider() {
        return optionCaptionProvider;
    }

    // just stub
    @Override
    public FilterMode getFilterMode() {
        return filterMode;
    }

    // just stub
    @Override
    public void setFilterMode(FilterMode mode) {
        this.filterMode = mode;
    }

    @Override
    public boolean isNewOptionAllowed() {
        return false;
    }

    @Override
    public void setNewOptionAllowed(boolean newOptionAllowed) {
        if (newOptionAllowed) {
            throw new UnsupportedOperationException("New options are not allowed for SearchPickerField");
        }
    }

    @Override
    public boolean isTextInputAllowed() {
        return false;
    }

    @Override
    public void setTextInputAllowed(boolean textInputAllowed) {
        throw new UnsupportedOperationException("Text input is not allowed for SearchPickerField");
    }

    @Override
    public void setAutomaticPopupOnFocus(boolean automaticPopupOnFocus) {
    }

    @Override
    public boolean isAutomaticPopupOnFocus() {
        return false;
    }

    @Override
    public Consumer<String> getNewOptionHandler() {
        return null;
    }

    @Override
    public void setNewOptionHandler(Consumer<String> newOptionHandler) {
        if (newOptionHandler != null) {
            throw new UnsupportedOperationException("New options are not allowed for SearchPickerField");
        }
    }

    @Nullable
    @Override
    public Consumer<EnterPressEvent> getEnterPressHandler() {
        return null;
    }

    @Override
    public void setEnterPressHandler(@Nullable Consumer<EnterPressEvent> handler) {
        if (handler != null) {
            throw new UnsupportedOperationException("EnterPress handler is not allowed for SearchPickerField");
        }
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
    public void setNullOptionVisible(boolean nullOptionVisible) {
        this.nullOptionVisible = nullOptionVisible;

        getComponent().setEmptySelectionAllowed(!isRequired() && nullOptionVisible);
    }

    @Override
    public boolean isNullOptionVisible() {
        return nullOptionVisible;
    }

    @Override
    public void setOptionIconProvider(Function<? super V, String> optionIconProvider) {
        if (this.optionIconProvider != optionIconProvider) {
            this.optionIconProvider = optionIconProvider;

            getComponent().setItemIconGenerator(this::generateOptionIcon);
        }
    }

    @Override
    public void setOptionIconProvider(Class<V> optionClass, Function<? super V, String> optionIconProvider) {
        setOptionIconProvider(optionIconProvider);
    }

    @Override
    public Function<? super V, String> getOptionIconProvider() {
        return optionIconProvider;
    }

    protected Resource generateOptionIcon(V item) {
        if (optionIconProvider == null) {
            return null;
        }

        String resourceId;
        try {
            resourceId = optionIconProvider.apply(item);
        } catch (Exception e) {
            LoggerFactory.getLogger(WebSearchPickerField.class)
                    .warn("Error invoking OptionIconProvider getItemIcon method", e);
            return null;
        }

        return iconResolver.getIconResource(resourceId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setOptionImageProvider(Function<? super V, io.jmix.ui.component.Resource> optionImageProvider) {
        if (this.optionImageProvider != optionImageProvider) {
            this.optionImageProvider = optionImageProvider;

            if (optionImageProvider != null) {
                getComponent().setItemIconGenerator(this::generateOptionImage);
            } else {
                getComponent().setItemIconGenerator(NULL_ITEM_ICON_GENERATOR);
            }
        }
    }

    @Override
    public Function<? super V, io.jmix.ui.component.Resource> getOptionImageProvider() {
        return optionImageProvider;
    }

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
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOptionsCaptionFilter(@Nullable Predicate<OptionsCaptionFilteringContext> filter) {
        throw new UnsupportedOperationException();
    }

    // just stub
    @Override
    public void setFilterPredicate(FilterPredicate filterPredicate) {
        this.filterPredicate = filterPredicate;
    }

    // just stub
    @Override
    public FilterPredicate getFilterPredicate() {
        return filterPredicate;
    }

    @Override
    public String getPopupWidth() {
        return getComponent().getPopupWidth();
    }

    @Override
    public void setPopupWidth(String width) {
        getComponent().setPopupWidth(width);
    }

    @Override
    public String getInputPrompt() {
        return getComponent().getPlaceholder();
    }

    @Override
    public void setInputPrompt(String inputPrompt) {
        if (StringUtils.isNotBlank(inputPrompt)) {
            setNullOption(null);
        }
        getComponent().setPlaceholder(inputPrompt);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setOptionStyleProvider(Function<? super V, String> optionStyleProvider) {
        if (this.optionStyleProvider != optionStyleProvider) {
            this.optionStyleProvider = optionStyleProvider;

            if (optionStyleProvider != null) {
                getComponent().setOptionsStyleProvider(this::generateItemStylename);
            } else {
                getComponent().setOptionsStyleProvider(NULL_STYLE_GENERATOR);
            }
        }
    }

    @Override
    public Function<? super V, String> getOptionStyleProvider() {
        return optionStyleProvider;
    }

    @Override
    public Options<V> getOptions() {
        return optionsBinding != null ? optionsBinding.getSource() : null;
    }

    @Override
    public void setOptions(Options<V> options) {
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

    protected void setItemsToPresentation(Stream<V> options) {
        getComponent().setItems(this::filterItemTest, options.collect(Collectors.toList()));
    }

    protected boolean filterItemTest(String itemCaption, String filterText) {
        if (filterMode == FilterMode.NO) {
            return true;
        }

        if (filterMode == FilterMode.STARTS_WITH) {
            return StringUtils.startsWithIgnoreCase(itemCaption, filterText);
        }

        return StringUtils.containsIgnoreCase(itemCaption, filterText);
    }

    @Override
    public void setOptionCaptionProvider(Function<? super V, String> optionCaptionProvider) {
        if (this.optionCaptionProvider != optionCaptionProvider) {
            this.optionCaptionProvider = optionCaptionProvider;

            // reset item captions
            getComponent().setItemCaptionGenerator(this::generateItemCaption);
        }
    }

    @Override
    protected void checkValueType(V value) {
        // do not check
    }
}
