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

import com.vaadin.data.HasValue;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Button;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.*;
import io.jmix.ui.component.pagination.data.PaginationDataBinder;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.CollectionChangeType;
import io.jmix.ui.widget.JmixPagination;
import io.jmix.ui.widget.JmixPagination.JmixPage;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;

import java.util.function.Consumer;

public class PaginationImpl extends AbstractPagination<JmixPagination> implements Pagination,
        InitializingBean, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(PaginationImpl.class);

    protected static final int FIRST_PAGE = 1;

    protected Registration firstButtonClickRegistration;
    protected Registration prevButtonClickRegistration;
    protected Registration nextButtonClickRegistration;
    protected Registration lastButtonClickRegistration;

    protected boolean refreshing = false;

    public PaginationImpl() {
        component = createComponent();
    }

    protected JmixPagination createComponent() {
        return new JmixPagination();
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    @SuppressWarnings("ConstantConditions")
    protected void initComponent() {
        String iconName = getIconName(JmixIcon.ANGLE_DOUBLE_LEFT);
        getFirstButton().setIcon(getIconResource(iconName));

        iconName = getIconName(JmixIcon.ANGLE_LEFT);
        getPrevButton().setIcon(getIconResource(iconName));

        iconName = getIconName(JmixIcon.ANGLE_RIGHT);
        getNextButton().setIcon(getIconResource(iconName));

        iconName = getIconName(JmixIcon.ANGLE_DOUBLE_RIGHT);
        getLastButton().setIcon(getIconResource(iconName));

        initItemsPerPageLayout();

        updateState();

        updateItemsPerPageAvailability();
    }

    @Override
    public void setDataBinder(PaginationDataBinder dataBinder) {
        super.setDataBinder(dataBinder);

        component.setDataBinder(dataBinder);
        dataBinder.setCollectionChangeListener(this::onCollectionChange);

        removeListeners();

        initItemsPerPageOptions();
        initMaxResultValue();

        initListeners();

        if (dataBinderContainsItems()) {
            dataBinder.refresh();
        }
    }

    protected void removeListeners() {
        if (firstButtonClickRegistration != null) {
            firstButtonClickRegistration.remove();
            firstButtonClickRegistration = null;
        }
        if (prevButtonClickRegistration != null) {
            prevButtonClickRegistration.remove();
            prevButtonClickRegistration = null;
        }
        if (nextButtonClickRegistration != null) {
            nextButtonClickRegistration.remove();
            nextButtonClickRegistration = null;
        }
        if (lastButtonClickRegistration != null) {
            lastButtonClickRegistration.remove();
            lastButtonClickRegistration = null;
        }
        removeItemsPerPageValueChangeSubscription();
    }

    protected void initListeners() {
        removeListeners();
        firstButtonClickRegistration = getFirstButton().addClickListener(this::onFirstButtonClick);
        prevButtonClickRegistration = getPrevButton().addClickListener(this::onPrevButtonClick);
        nextButtonClickRegistration = getNextButton().addClickListener(this::onNextButtonClick);
        lastButtonClickRegistration = getLastButton().addClickListener(this::onLastButtonClick);
        setupItemsPerPageValueChangeListener();
    }

    @Override
    public int getMaxVisiblePages() {
        return component.getMaxVisiblePages();
    }

    @Override
    public void setMaxVisiblePages(int maxVisiblePages) {
        component.setMaxVisiblePages(maxVisiblePages);
    }

    @Override
    public Subscription addPageChangeListener(Consumer<PageChangeEvent> listener) {
        return getEventHub().subscribe(PageChangeEvent.class, listener);
    }

    protected void firePageChangeEvent(Integer previousPageNumber, Integer pageNumber) {
        publish(PageChangeEvent.class, new PageChangeEvent(this, previousPageNumber, pageNumber));
    }

    protected void onItemsPerPageValueChange(HasValue.ValueChangeEvent<Integer> event) {
        checkDataBound();

        BeforeRefreshEvent refreshEvent = fireBeforeRefreshEvent();
        if (refreshEvent.isRefreshPrevented()) {
            setSilentlyItemsPerPageValue(event.getOldValue());
            return;
        }

        Integer maxResult = event.getValue();
        if (maxResult == null) {
            maxResult = getEntityMaxFetchSize(dataBinder.getEntityMetaClass());
        }
        dataBinder.setMaxResults(maxResult);

        // move to the first page, as page count could change
        component.forceSelectPage(FIRST_PAGE);
    }

    protected void onFirstButtonClick(Button.ClickEvent event) {
        component.selectFirstPage();
    }

    protected void onPrevButtonClick(Button.ClickEvent event) {
        component.selectPreviousPage();
    }

    protected void onNextButtonClick(Button.ClickEvent event) {
        component.selectNextPage();
    }

    protected void onLastButtonClick(Button.ClickEvent event) {
        component.selectLastPage();
    }

    protected void onCollectionChange(CollectionChangeType changeType) {
        int firstResult = dataBinder.getFirstResult();
        int maxResult = dataBinder.getMaxResults();

        if (refreshing) {
            if (CollectionUtils.isEmpty(component.getPages())) {
                createPageButtons();
                component.setCurrentPageNumber(FIRST_PAGE);
            }

            // check if current page corresponds to the dataSourceProvider's firstResult
            JmixPage currentPage = component.getCurrentPage();
            if (currentPage != null
                    && currentPage.getFirstResult() != firstResult) {
                component.setCurrentPageNumberByFirstResult(firstResult);
            }
        } else {
            if (!component.isFirstResultInRanges(firstResult)) {
                log.warn("Pagination component receives loader's first result ({}) that is out of last page range." +
                        " Component may work incorrectly.", firstResult);
                return;
            }
            // if maxResult was changed not in this component try to adjust pages and ItemsPerPage value
            if (component.getItemsToDisplay() != maxResult && isItemsPerPageVisible()) {
                Integer value = canSetUnlimitedValue(maxResult) ? null : maxResult;
                setSilentlyItemsPerPageValue(value);
            }
            createPageButtons(getTotalCount(), maxResult);

            if (CollectionUtils.isNotEmpty(component.getPages())) {
                component.setCurrentPageNumberByFirstResult(firstResult);
            } else if (component.getLastPageNumber() > 1
                    && component.getCurrentPageNumber() == component.getLastPageNumber()) {
                // If no data in the container and page number is last, it means
                // user removes all items in the last page. Need to select previous page.
                component.forceSelectPage(component.getCurrentPageNumber() - 1);
            }
        }

        component.updatePageNumbers();
        component.updatePageSelections();

        updateState();

        updateItemsPerPageAvailability();
    }

    protected boolean refreshData() {
        BeforeRefreshEvent refreshEvent = fireBeforeRefreshEvent();
        if (refreshEvent.isRefreshPrevented()) {
            return false;
        }

        refreshing = true;
        try {
            dataBinder.refresh();
        } finally {
            refreshing = false;
        }

        return true;
    }

    protected void createPageButtons() {
        createPageButtons(getTotalCount(), getItemsCountToDisplay());
    }

    protected void createPageButtons(int totalCount, int itemsToDisplay) {
        checkDataBound();
        component.removePages();

        if (!dataBinderContainsItems()) {
            return;
        }

        component.createPages(totalCount, itemsToDisplay);

        component.setOnAfterRefreshListener(this::fireAfterRefreshEvent);
        component.setPageChangeListener(this::firePageChangeEvent);
        component.setDataRefreshedProvider(this::refreshData);
    }

    protected void updateState() {
        State state = component.getLastPageNumber() == -1 ? State.FIRST_COMPLETE : getCurrentState();

        switch (state) {
            case FIRST_COMPLETE:
                getFirstButton().setEnabled(false);
                getPrevButton().setEnabled(false);
                getNextButton().setEnabled(false);
                getLastButton().setEnabled(false);
                break;
            case FIRST_INCOMPLETE:
                getFirstButton().setEnabled(false);
                getPrevButton().setEnabled(false);
                getNextButton().setEnabled(true);
                getLastButton().setEnabled(true);
                break;
            case MIDDLE:
                getFirstButton().setEnabled(true);
                getPrevButton().setEnabled(true);
                getNextButton().setEnabled(true);
                getLastButton().setEnabled(true);
                break;
            case LAST:
                getFirstButton().setEnabled(true);
                getPrevButton().setEnabled(true);
                getNextButton().setEnabled(false);
                getLastButton().setEnabled(false);
                break;
        }
    }

    protected State getCurrentState() {
        if (component.getCurrentPageNumber() == FIRST_PAGE) {
            return component.getLastPageNumber() != 1 ? State.FIRST_INCOMPLETE : State.FIRST_COMPLETE;
        } else if (component.getCurrentPageNumber() == component.getLastPageNumber()) {
            return State.LAST;
        }
        return State.MIDDLE;
    }

    protected void updateItemsPerPageAvailability() {
        getItemsPerPageComboBox().setEnabled(!isEmptyOrNullDataBinder() && dataBinderContainsItems());
    }
}