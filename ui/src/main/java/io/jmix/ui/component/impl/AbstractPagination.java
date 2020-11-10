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

import com.google.common.base.Splitter;
import com.vaadin.data.HasValue;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import io.jmix.core.Messages;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.PaginationComponent;
import io.jmix.ui.component.pagination.data.PaginationDataBinder;
import io.jmix.ui.component.pagination.data.PaginationEmptyBinder;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import io.jmix.ui.widget.JmixAbstractPagination;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractPagination<T extends JmixAbstractPagination> extends AbstractComponent<T>
        implements PaginationComponent {

    protected enum State {
        FIRST_COMPLETE,     // "63 rows"
        FIRST_INCOMPLETE,   // "1-100 rows of [?] >"
        MIDDLE,             // "< 101-200 rows of [?] >"
        LAST                // "< 201-252 rows"
    }

    protected ThemeConstantsManager themeConstantsManager;
    protected Messages messages;
    protected UiProperties uiProperties;

    protected PaginationDataBinder dataBinder;
    protected Supplier<Integer> totalCountDelegate;

    protected List<Integer> itemsPerPageOptions;
    protected Integer itemsPerPageDefaultValue;

    protected Registration itemsPerPageValueChangeRegistration;

    @Autowired
    public void setThemeConstantsManager(ThemeConstantsManager themeConstantsManager) {
        this.themeConstantsManager = themeConstantsManager;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setUiProperties(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @Nullable
    @Override
    public Supplier<Integer> getTotalCountDelegate() {
        return totalCountDelegate;
    }

    @Override
    public void setTotalCountDelegate(@Nullable Supplier<Integer> delegate) {
        this.totalCountDelegate = delegate;
    }

    @Nullable
    @Override
    public PaginationDataBinder getDataBinder() {
        return dataBinder;
    }

    @Override
    public Subscription addBeforeRefreshListener(Consumer<BeforeRefreshEvent> listener) {
        return getEventHub().subscribe(BeforeRefreshEvent.class, listener);
    }

    protected BeforeRefreshEvent fireBeforeRefreshEvent() {
        BeforeRefreshEvent event = new BeforeRefreshEvent(this);

        publish(BeforeRefreshEvent.class, event);

        return event;
    }

    @Override
    public Subscription addAfterRefreshListener(Consumer<AfterRefreshEvent> listener) {
        return getEventHub().subscribe(AfterRefreshEvent.class, listener);
    }

    protected void fireAfterRefreshEvent() {
        AfterRefreshEvent event = new AfterRefreshEvent(this);

        publish(AfterRefreshEvent.class, event);
    }

    @Override
    public boolean isItemsPerPageVisible() {
        return component.isItemsPerPageVisible();
    }

    @Override
    public void setItemsPerPageVisible(boolean itemsPerPageVisible) {
        component.setItemsPerPageVisible(itemsPerPageVisible);
    }

    @Override
    public boolean isNullItemsPerPageOptionVisible() {
        return component.isNullItemsPerPageOptionVisible();
    }

    @Override
    public void setNullItemsPerPageOptionVisible(boolean nullOptionVisible) {
        component.setNullItemsPerPageOptionVisible(nullOptionVisible);
    }

    @Override
    public List<Integer> getItemsPerPageOptions() {
        if (itemsPerPageOptions == null) {
            return Collections.emptyList();
        }
        return new UnmodifiableList<>(itemsPerPageOptions);
    }

    @Override
    public void setItemsPerPageOptions(List<Integer> options) {
        this.itemsPerPageOptions = options;
    }

    @Nullable
    @Override
    public Integer getItemsPerPageDefaultValue() {
        return itemsPerPageDefaultValue;
    }

    @Override
    public void setItemsPerPageDefaultValue(@Nullable Integer defaultValue) {
        itemsPerPageDefaultValue = defaultValue;
    }

    @Override
    public void setDataBinder(PaginationDataBinder dataBinder) {
        Preconditions.checkNotNullArgument(dataBinder);

        if (this.dataBinder != null) {
            this.dataBinder.removeCollectionChangeListener();
        }

        this.dataBinder = dataBinder;
    }

    protected void removeItemsPerPageValueChangeSubscription() {
        if (itemsPerPageValueChangeRegistration != null) {
            itemsPerPageValueChangeRegistration.remove();
            itemsPerPageValueChangeRegistration = null;
        }
    }

    protected void setupItemsPerPageValueChangeListener() {
        itemsPerPageValueChangeRegistration = getItemsPerPageComboBox()
                .addValueChangeListener(this::onItemsPerPageValueChange);
    }

    protected void onItemsPerPageValueChange(HasValue.ValueChangeEvent<Integer> event) {
    }

    protected void setSilentlyItemsPerPageValue(@Nullable Integer value) {
        itemsPerPageValueChangeRegistration.remove();

        getItemsPerPageComboBox().setValue(value);

        itemsPerPageValueChangeRegistration = getItemsPerPageComboBox()
                .addValueChangeListener(this::onItemsPerPageValueChange);
    }

    protected void initItemsPerPageLayout() {
        ThemeConstants theme = themeConstantsManager.getConstants();
        getItemsPerPageComboBox().setWidth(theme.get("jmix.ui.pagination.itemsPerPage.width"));
        getItemsPerPageComboBox().setEmptySelectionAllowed(true);

        component.getItemsPerPageLabel().setValue(
                messages.getMessage("", "pagination.itemsPerPage.label.value"));
    }

    protected void initItemsPerPageOptions() {
        checkDataBound();

        List<Integer> processedOptions;
        if (CollectionUtils.isNotEmpty(itemsPerPageOptions)) {
            processedOptions = processOptions(itemsPerPageOptions, dataBinder.getEntityMetaClass());
        } else {
            processedOptions = processOptions(getItemsPerPageOptionsFromProperty(),
                    dataBinder.getEntityMetaClass());
        }

        getItemsPerPageComboBox().setItems(processedOptions);
    }

    /**
     * Setup MaxResult value to data binder and to items per page ComboBox if it's visible.
     */
    protected void initMaxResultValue() {
        checkDataBound();

        // do not init value when ItemsPerPage is disabled
        if (isEmptyOrNullDataBinder()) {
            return;
        }

        if (isItemsPerPageVisible()) {
            Integer maxResult = getDefaultOptionValue(itemsPerPageOptions, dataBinder.getEntityMetaClass());
            dataBinder.setMaxResults(maxResult);
            getItemsPerPageComboBox().setValue(maxResult);
        } else {
            int maxResult = dataBinder.getMaxResult();
            int maxFetch = getEntityMaxFetchSize(dataBinder.getEntityMetaClass());
            dataBinder.setMaxResults(Math.min(maxResult, maxFetch));
        }
    }

    protected int getTotalCount() {
        if (isEmptyOrNullDataBinder()) {
            return 0;
        }

        if (totalCountDelegate != null) {
            return totalCountDelegate.get();
        }

        return dataBinder.getCount();
    }

    /**
     * @return current items count for page
     */
    protected int getItemsCountToDisplay() {
        checkDataBound();

        if (isItemsPerPageVisible()) {
            Integer value = getItemsPerPageComboBox().getValue();
            return value != null ? value : getEntityMaxFetchSize(dataBinder.getEntityMetaClass());
        } else {
            int maxResult = dataBinder.getMaxResult();
            int maxFetch = getEntityMaxFetchSize(dataBinder.getEntityMetaClass());
            return Math.min(maxResult, maxFetch);
        }
    }

    protected void checkDataBound() {
        if (dataBinder == null) {
            throw new IllegalStateException("Pagination component is not bound with PaginationDataBinder");
        }
    }

    protected boolean isEmptyOrNullDataBinder() {
        return dataBinder == null || dataBinder instanceof PaginationEmptyBinder;
    }

    protected boolean dataBinderContainsItems() {
        checkDataBound();

        return dataBinder.size() > 0;
    }

    protected Button getFirstButton() {
        return component.getFirstButton();
    }

    protected Button getPrevButton() {
        return component.getPrevButton();
    }

    protected Button getNextButton() {
        return component.getNextButton();
    }

    protected Button getLastButton() {
        return component.getLastButton();
    }

    protected ComboBox<Integer> getItemsPerPageComboBox() {
        return component.getItemsPerPageComboBox();
    }

    protected List<Integer> getItemsPerPageOptionsFromProperty() {
        String maxResultsProperty = uiProperties.getPaginationItemsPerPageOptions();
        Iterable<String> split = Splitter.on(",").trimResults().split(maxResultsProperty);

        List<Integer> result = new ArrayList<>();
        for (String option : split) {
            try {
                result.add(Integer.parseInt(option));
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Cannot parse to Integer: " + option);
            }
        }

        return result;
    }

    protected int getEntityMaxFetchSize(MetaClass metaClass) {
        return uiProperties.getEntityMaxFetchSize(metaClass.getName());
    }

    protected int getEntityPageSize(MetaClass metaClass) {
        return uiProperties.getEntityPageSize(metaClass.getName());
    }

    protected Integer getDefaultOptionValue(List<Integer> options, MetaClass metaClass) {
        if (itemsPerPageDefaultValue != null) {
            if (options.contains(itemsPerPageDefaultValue)) {
                return itemsPerPageDefaultValue;
            } else {
                return findClosestValue(itemsPerPageDefaultValue, options);
            }
        } else {
            int defaultEntityPageSize = getEntityPageSize(metaClass);
            return findClosestValue(defaultEntityPageSize, options);
        }
    }

    protected int findClosestValue(int maxResults, List<Integer> optionsList) {
        int minimumValue = Integer.MAX_VALUE;
        int closest = maxResults;

        for (int option : optionsList) {
            int diff = Math.abs(option - maxResults);
            if (diff < minimumValue) {
                minimumValue = diff;
                closest = option;
            }
        }

        return closest;
    }

    /**
     * Sorts options. Options less than or equal 0 are ignored. Values greater than MaxFetchSize
     * are replaced by MaxFetchSize.
     *
     * @param options   items per page options
     * @param metaClass entity's MetaClass
     * @return sorted options
     */
    protected List<Integer> processOptions(List<Integer> options, MetaClass metaClass) {
        int maxFetch = getEntityMaxFetchSize(metaClass);

        List<Integer> result = new ArrayList<>();
        for (Integer option : options) {
            if (option > maxFetch) {
                option = maxFetch;
            }

            if (result.contains(option) || option <= 0) {
                continue;
            }

            result.add(option);
        }

        Collections.sort(result);

        return result;
    }
}
