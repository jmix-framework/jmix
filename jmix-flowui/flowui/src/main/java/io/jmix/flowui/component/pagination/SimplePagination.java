/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.pagination;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.FlowuiComponentProperties;
import io.jmix.flowui.FlowuiProperties;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.PaginationComponent;
import io.jmix.flowui.data.pagination.PaginationDataLoader;
import io.jmix.flowui.kit.component.pagination.JmixSimplePagination;
import io.jmix.flowui.model.CollectionChangeType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class SimplePagination extends JmixSimplePagination implements PaginationComponent<SimplePagination>,
        ApplicationContextAware, InitializingBean {

    protected enum State {
        FIRST_COMPLETE,     // "63 rows"
        FIRST_INCOMPLETE,   // "1-100 rows of [?] >"
        MIDDLE,             // "< 101-200 rows of [?] >"
        LAST                // "< 201-252 rows"
    }

    protected ApplicationContext applicationContext;
    protected Messages messages;
    protected UiComponents uiComponents;
    protected CurrentAuthentication currentAuthentication;
    protected FlowuiProperties flowuiProperties;

    protected ItemsPerPage itemsPerPage;
    protected PaginationDataLoader loader;

    protected Registration firstButtonClickRegistration;
    protected Registration previousButtonClickRegistration;
    protected Registration nextButtonClickRegistration;
    protected Registration lastButtonClickRegistration;
    protected Registration totalCountLabelClickRegistration;
    protected Registration itemsPerPageValueChangeRegistration;

    protected Function<LoadContext, Integer> totalCountDelegate;

    protected boolean samePage;
    protected boolean lastPage = false;
    protected boolean refreshing;
    protected State state;
    protected State lastState;
    protected int size;
    protected int start;

    protected Datatype<?> numberDatatype;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
        messages = applicationContext.getBean(Messages.class);
        currentAuthentication = applicationContext.getBean(CurrentAuthentication.class);
        flowuiProperties = applicationContext.getBean(FlowuiProperties.class);
    }

    protected void initComponent() {
        getContent(); // init component content

        itemsPerPage = createItemsPerPage();
        initItemsPerPage(itemsPerPage);

        numberDatatype = applicationContext.getBean(DatatypeRegistry.class).get(Integer.class);

        disableUi();
    }

    protected void disableUi() {
        firstButton.setEnabled(false);
        previousButton.setEnabled(false);
        nextButton.setEnabled(false);
        lastButton.setEnabled(false);

        getTotalCountLabel().setEnabled(false);
        getTotalCountLabel().setText("");
        getRowsStatusLabel().setText("");

        itemsPerPage.setEnabled(false);
    }

    protected ItemsPerPage createItemsPerPage() {
        return uiComponents.create(ItemsPerPage.class);
    }

    protected void initItemsPerPage(ItemsPerPage itemsPerPage) {
        itemsPerPage.setPaginationLoader(loader);
    }

    /**
     * @return items per page default value or {@code null} if not set
     */
    @Nullable
    public Integer getItemsPerPageDefaultValue() {
        return itemsPerPage.getItemsPerPageDefaultValue();
    }

    /**
     * Sets default value for the select component.
     *
     * @param defaultValue value to set
     */
    public void setItemsPerPageDefaultValue(@Nullable Integer defaultValue) {
        itemsPerPage.setItemsPerPageDefaultValue(defaultValue);
    }

    /**
     * @return items that is used in select component
     */
    public Collection<Integer> getItemsPerPageItems() {
        return itemsPerPage.getItemsPerPageItems();
    }

    /**
     * Sets items which should be used in the select component. Items less than or equal to 0 are ignored,
     * options greater than entity's max fetch size will be replaced by it.
     *
     * @param itemsPerPageItems items to set
     */
    public void setItemsPerPageItems(List<Integer> itemsPerPageItems) {
        itemsPerPage.setItemsPerPageItems(itemsPerPageItems);
    }

    /**
     * @return {@code true} if unlimited (null) item should be visible in the select component
     */
    public boolean isItemsPerPageUnlimitedItemVisible() {
        return itemsPerPage.isItemsPerPageUnlimitedItemVisible();
    }

    /**
     * Sets visibility of unlimited (null) option value in the items per page select component. If unlimited (null) option
     * is selected component will try to load data with {@link FlowuiProperties#getEntityMaxFetchSize(String)}
     * limitation. The default value is true.
     *
     * @param unlimitedItemVisible whether unlimited option should be visible
     */
    public void setItemsPerPageUnlimitedItemVisible(boolean unlimitedItemVisible) {
        itemsPerPage.setItemsPerPageUnlimitedItemVisible(unlimitedItemVisible);
    }

    /**
     * @return {@code true} if items per page select component is visible
     */
    public boolean isItemsPerPageVisible() {
        return getJmixRowsPerPage() != null;
    }

    /**
     * Sets visibility of items per page select component. This component contains options to limit the number
     * of items for one page. If custom options are not set component will use
     * {@link FlowuiComponentProperties#getPaginationItemsPerPageItems()}. The default value is {@code false}.
     */
    public void setItemsPerPageVisible(boolean itemsPerPageVisible) {
        if (isItemsPerPageVisible() != itemsPerPageVisible) {
            setItemsPerPage(itemsPerPageVisible ? itemsPerPage : null);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addBeforeRefreshListener(ComponentEventListener<BeforeRefreshEvent<SimplePagination>> listener) {
        return addListener(BeforeRefreshEvent.class, (ComponentEventListener) listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addAfterRefreshListener(ComponentEventListener<AfterRefreshEvent<SimplePagination>> listener) {
        return addListener(AfterRefreshEvent.class, (ComponentEventListener) listener);
    }

    @Nullable
    @Override
    public Function<LoadContext, Integer> getTotalCountDelegate() {
        return totalCountDelegate;
    }

    @Override
    public void setTotalCountDelegate(@Nullable Function<LoadContext, Integer> totalCountDelegate) {
        this.totalCountDelegate = totalCountDelegate;

        if (loader != null) {
            loader.setTotalCountDelegate(totalCountDelegate);
        }
    }

    @Override
    public PaginationDataLoader getPaginationLoader() {
        return loader;
    }

    @Override
    public void setPaginationLoader(@Nullable PaginationDataLoader loader) {
        if (this.loader != null) {
            this.loader.removeCollectionChangeListener();
        }
        this.loader = loader;

        removeListeners();

        if (loader == null) {
            disableUi();
            return;
        }

        loader.setTotalCountDelegate(totalCountDelegate);
        loader.setCollectionChangeListener(this::onRefreshItems);

        initItemsPerPage(itemsPerPage);

        initListeners();

        if (loaderContainsItems() && isItemsPerPageVisible()) {
            // if items has already loaded we should reload them
            // with maxResult from Select or update shown items
            loader.refresh();
            return;
        }

        // update state
        onCollectionChanged();
    }

    protected void onRefreshItems(CollectionChangeType changeType) {
        samePage = CollectionChangeType.REFRESH != changeType;
        onCollectionChanged();
    }

    protected void removeListeners() {
        if (firstButtonClickRegistration != null) {
            firstButtonClickRegistration.remove();
            firstButtonClickRegistration = null;
        }
        if (previousButtonClickRegistration != null) {
            previousButtonClickRegistration.remove();
            previousButtonClickRegistration = null;
        }
        if (nextButtonClickRegistration != null) {
            nextButtonClickRegistration.remove();
            nextButtonClickRegistration = null;
        }
        if (lastButtonClickRegistration != null) {
            lastButtonClickRegistration.remove();
            lastButtonClickRegistration = null;
        }
        if (totalCountLabelClickRegistration != null) {
            totalCountLabelClickRegistration.remove();
            totalCountLabelClickRegistration = null;
        }
        if (itemsPerPageValueChangeRegistration != null) {
            itemsPerPageValueChangeRegistration.remove();
            itemsPerPageValueChangeRegistration = null;
        }
    }

    protected void initListeners() {
        removeListeners();
        firstButtonClickRegistration = firstButton.addClickListener(this::onFirstClick);
        previousButtonClickRegistration = previousButton.addClickListener(this::onPreviousClick);
        nextButtonClickRegistration = nextButton.addClickListener(this::onNextClick);
        lastButtonClickRegistration = lastButton.addClickListener(this::onLastClick);
        totalCountLabelClickRegistration = getTotalCountLabel().addClickListener(this::onTotalCountLabelClick);
        itemsPerPageValueChangeRegistration =
                itemsPerPage.addItemsPerPageValueChangeListener(this::onItemsPerPageValueChange);
    }

    protected void onTotalCountLabelClick(ClickEvent<Span> event) {
        setTotalCountLabelText(getTotalCount());
    }

    protected void onFirstClick(ClickEvent<Button> event) {
        int firstResult = loader.getFirstResult();
        loader.setFirstResult(0);

        if (refreshData()) {
            fireAfterRefreshEvent();
        } else {
            loader.setFirstResult(firstResult);
        }
    }

    protected void onPreviousClick(ClickEvent<Button> event) {
        int firstResult = loader.getFirstResult();
        int newStart = loader.getFirstResult() - loader.getMaxResults();
        loader.setFirstResult(Math.max(newStart, 0));

        if (refreshData()) {
            fireAfterRefreshEvent();
        } else {
            loader.setFirstResult(firstResult);
        }
    }

    protected void onNextClick(ClickEvent<Button> event) {
        int firstResult = loader.getFirstResult();
        loader.setFirstResult(loader.getFirstResult() + loader.getMaxResults());
        if (refreshData()) {
            if (state == State.LAST && size == 0) {
                loader.setFirstResult(firstResult);
                lastPage = true;
                refreshData();
                lastPage = false;
            }
            fireAfterRefreshEvent();
        } else {
            loader.setFirstResult(firstResult);
        }
    }

    protected void onLastClick(ClickEvent<Button> event) {
        int count = getTotalCount();
        int itemsToDisplay = count % loader.getMaxResults();
        if (itemsToDisplay == 0) itemsToDisplay = loader.getMaxResults();

        int firstResult = loader.getFirstResult();
        loader.setFirstResult(count - itemsToDisplay);

        lastPage = true;
        if (refreshData()) {
            fireAfterRefreshEvent();
        } else {
            loader.setFirstResult(firstResult);
        }
        lastPage = false;
    }

    protected void onItemsPerPageValueChange(HasValue.ValueChangeEvent<Integer> event) {
        BeforeRefreshEvent<SimplePagination> refreshEvent = fireBeforeRefreshEvent();
        if (refreshEvent.isRefreshPrevented()) {
            setSilentlyItemsPerPageValue(event.getOldValue());
            return;
        }

        Integer maxResult = event.getValue();
        if (maxResult == null) {
            maxResult = getEntityMaxFetchSize(loader.getEntityMetaClass());
        }

        loader.setFirstResult(0);
        loader.setMaxResults(maxResult);
        loader.refresh();

        fireAfterRefreshEvent();
    }

    protected boolean refreshData() {
        BeforeRefreshEvent<SimplePagination> refreshEvent = fireBeforeRefreshEvent();
        if (refreshEvent.isRefreshPrevented()) {
            return false;
        }

        refreshing = true;
        try {
            loader.refresh();
        } finally {
            refreshing = false;
        }

        return true;
    }

    protected void onCollectionChanged() {
        if (loader == null) {
            return;
        }

        size = loader.size();
        start = 0;

        boolean updateTotalCountValue = false;
        if (samePage) {
            state = lastState == null ? State.FIRST_COMPLETE : lastState;
            start = loader.getFirstResult();
            samePage = false;
            updateTotalCountValue = State.LAST.equals(state);
        } else if ((size == 0 || size < loader.getMaxResults()) && loader.getFirstResult() == 0) {
            state = State.FIRST_COMPLETE;
            lastState = state;
        } else if (size == loader.getMaxResults() && loader.getFirstResult() == 0) {
            state = State.FIRST_INCOMPLETE;
            lastState = state;
        } else if (size == loader.getMaxResults() && loader.getFirstResult() > 0 && !lastPage) {
            state = State.MIDDLE;
            start = loader.getFirstResult();
            lastState = state;
        } else if (size <= loader.getMaxResults() && loader.getFirstResult() > 0) {
            state = State.LAST;
            start = loader.getFirstResult();
            lastState = state;
        } else {
            state = State.FIRST_COMPLETE;
            lastState = state;
        }

        updateNavigationButtonsAvailability();
        updateItemsPerPageAvailability();

        getRowsStatusLabel().setText(
                messages.formatMessage("", getLabelMessageKey(), calculateRowsStatusLabelValue()));

        // update visible total count
        if (getTotalCountLabel().isVisible() && !refreshing || updateTotalCountValue) {
            updateTotalCountLabel();
        }

        if (loader.getMaxResults() != itemsPerPage.getItemsPerPageValue()) {
            if (isItemsPerPageVisible()) {
                Integer maxResult = canSetUnlimitedValue(loader.getMaxResults()) ? null : loader.getMaxResults();
                setSilentlyItemsPerPageValue(maxResult);
            }
        }
    }

    protected void updateItemsPerPageAvailability() {
        itemsPerPage.setEnabled(loader != null && loaderContainsItems());
    }

    protected void updateTotalCountLabel() {
        if (autoLoad) {
//            loadItemsCount(); // todo background task
        } else {
            getTotalCountLabel().setText(messages.getMessage("pagination.msg3"));
            getTotalCountLabel().addClassName(ROWS_STATUS_LINK_CLASS_NAME);
            getTotalCountLabel().setEnabled(true);
        }
    }

    protected void updateNavigationButtonsAvailability() {
        switch (state) {
            case FIRST_COMPLETE:
                firstButton.setEnabled(false);
                previousButton.setEnabled(false);
                getTotalCountLabel().setVisible(false);
                nextButton.setEnabled(false);
                lastButton.setEnabled(false);
                break;
            case FIRST_INCOMPLETE:
                firstButton.setEnabled(false);
                previousButton.setEnabled(false);
                getTotalCountLabel().setVisible(true);
                nextButton.setEnabled(true);
                lastButton.setEnabled(true);
                break;
            case MIDDLE:
                firstButton.setEnabled(true);
                previousButton.setEnabled(true);
                getTotalCountLabel().setVisible(true);
                nextButton.setEnabled(true);
                lastButton.setEnabled(true);
                break;
            case LAST:
                firstButton.setEnabled(true);
                previousButton.setEnabled(true);
                getTotalCountLabel().setVisible(false);
                nextButton.setEnabled(false);
                lastButton.setEnabled(false);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected String getLabelMessageKey() {
        String msgKey;
        switch (state) {
            case FIRST_COMPLETE:
                if (size == 1) {
                    msgKey = "pagination.msg2Singular1";
                } else if (size % 100 > 10 && size % 100 < 20) {
                    msgKey = "pagination.msg2Plural1";
                } else {
                    switch (size % 10) {
                        case 1:
                            msgKey = "pagination.msg2Singular";
                            break;
                        case 2:
                        case 3:
                        case 4:
                            msgKey = "pagination.msg2Plural2";
                            break;
                        default:
                            msgKey = "pagination.msg2Plural1";
                    }
                }
                break;
            case FIRST_INCOMPLETE:
            case MIDDLE:
                msgKey = "pagination.msg1";
                break;
            case LAST:
                msgKey = "pagination.msg2Plural2";
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return msgKey;
    }

    protected String calculateRowsStatusLabelValue() {
        switch (state) {
            case FIRST_COMPLETE:
                return numberDatatype.format(size);
            case FIRST_INCOMPLETE:
            case MIDDLE:
            case LAST:
                if (size == 0) {
                    return String.valueOf(size);
                } else {
                    String from = numberDatatype.format(start + 1, currentAuthentication.getLocale());
                    String to = numberDatatype.format(start + size, currentAuthentication.getLocale());
                    return from + "-" + to;
                }
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected void setTotalCountLabelText(int totalCount) {
        getTotalCountLabel().setText(numberDatatype.format(totalCount, currentAuthentication.getLocale()));
        getTotalCountLabel().removeClassName(ROWS_STATUS_LINK_CLASS_NAME);
        getTotalCountLabel().setEnabled(false);
    }

    protected int getTotalCount() {
        return loader == null ? 0 : loader.getCount();
    }

    protected boolean loaderContainsItems() {
        return loader.size() > 0;
    }

    protected boolean canSetUnlimitedValue(@Nullable Integer value) {
        int maxFetch = getEntityMaxFetchSize(loader.getEntityMetaClass());

        return value == null && isItemsPerPageUnlimitedItemVisible()
                || Objects.equals(value, maxFetch) && isItemsPerPageUnlimitedItemVisible();
    }

    protected int getEntityMaxFetchSize(MetaClass metaClass) {
        return flowuiProperties.getEntityMaxFetchSize(metaClass.getName());
    }

    protected void setSilentlyItemsPerPageValue(@Nullable Integer value) {
        itemsPerPageValueChangeRegistration.remove();

        itemsPerPage.setItemsPerPageValue(value);

        itemsPerPageValueChangeRegistration = itemsPerPage
                .addItemsPerPageValueChangeListener(this::onItemsPerPageValueChange);
    }

    protected BeforeRefreshEvent<SimplePagination> fireBeforeRefreshEvent() {
        BeforeRefreshEvent<SimplePagination> event = new BeforeRefreshEvent<>(this);

        fireEvent(event);

        return event;
    }

    protected void fireAfterRefreshEvent() {
        AfterRefreshEvent<SimplePagination> event = new AfterRefreshEvent<>(this);

        fireEvent(event);
    }
}
