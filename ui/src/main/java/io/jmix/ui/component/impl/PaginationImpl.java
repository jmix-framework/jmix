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
import io.jmix.ui.component.pagination.Paging;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.CollectionChangeType;
import io.jmix.ui.widget.JmixPagination;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.function.Consumer;

public class PaginationImpl extends AbstractPagination<JmixPagination> implements Pagination,
        InitializingBean, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(PaginationImpl.class);

    protected static final int FIRST_PAGE = 1;

    protected Paging pages = new Paging();

    protected Registration firstButtonClickRegistration;
    protected Registration prevButtonClickRegistration;
    protected Registration nextButtonClickRegistration;
    protected Registration lastButtonClickRegistration;

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

        dataBinder.setCollectionChangeListener(this::onCollectionChange);

        removeListeners();

        initItemsPerPageOptions();
        initMaxResultValue();

        initListeners();

        createPageButtons();
        pages.setCurrentPageNumber(FIRST_PAGE);

        if (dataBinderContainsItems()) {
            pages.updatePageSelections();

            // if items has already loaded we should reload
            // them with maxResult from ComboBox
            if (isItemsPerPageVisible()) {
                dataBinder.refresh();
            }
        }

        updateItemsPerPageAvailability();
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
        return pages.getMaxVisiblePages();
    }

    @Override
    public void setMaxVisiblePages(int maxVisiblePages) {
        pages.setMaxVisiblePages(maxVisiblePages);
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

        createPageButtons();

        Integer maxResult = event.getValue();
        if (maxResult == null) {
            maxResult = getEntityMaxFetchSize(dataBinder.getEntityMetaClass());
        }
        dataBinder.setMaxResults(maxResult);

        // move to the first page, as page count could change
        pages.forceSelectPage(FIRST_PAGE);
    }

    protected void onFirstButtonClick(Button.ClickEvent event) {
        pages.selectFirstPage();
    }

    protected void onPrevButtonClick(Button.ClickEvent event) {
        pages.selectPreviousPage();
    }

    protected void onNextButtonClick(Button.ClickEvent event) {
        pages.selectNextPage();
    }

    protected void onLastButtonClick(Button.ClickEvent event) {
        pages.selectLastPage();
    }

    protected void onCollectionChange(CollectionChangeType changeType) {
        if (CollectionUtils.isEmpty(pages.getPages())) {
            createPageButtons();
            pages.setCurrentPageNumber(FIRST_PAGE);
        }

        int firstResult = dataBinder.getFirstResult();
        int maxResult = dataBinder.getMaxResult();

        if (pages.isFirstResultInRanges(firstResult)) {
            // if maxResult was changed not in this component try to adjust pages and ItemsPerPage value
            if (pages.getItemsToDisplay() != dataBinder.getMaxResult()) {
                if (isItemsPerPageVisible()) {
                    setSilentlyItemsPerPageValue(maxResult);
                }

                createPageButtons();

                pages.setCurrentPageNumberByFirstResult(firstResult);
            } else {
                // check if current page is corresponds to the dataBinder's firstResult
                Paging.Page currentPage = pages.getCurrentPage();
                if (currentPage != null
                        && currentPage.getFirstResult() != firstResult) {
                    pages.setCurrentPageNumberByFirstResult(firstResult);
                }
            }

            pages.updatePageNumbers();
            pages.updatePageSelections();

            updateState();
            return;
        }

        log.warn("Pagination component receives data that is out of page ranges. Component may work incorrectly.");
    }

    protected boolean refreshData() {
        BeforeRefreshEvent refreshEvent = fireBeforeRefreshEvent();
        if (refreshEvent.isRefreshPrevented()) {
            return false;
        }

        dataBinder.refresh();

        return true;
    }

    protected void createPageButtons() {
        checkDataBound();
        removePageButtons();

        if (!dataBinderContainsItems()) {
            return;
        }

        pages.setDataBinder(dataBinder);
        pages.createPages(getTotalCount(), getItemsCountToDisplay());
        pages.setOnAfterDataRefreshListener(this::fireAfterRefreshEvent);
        pages.setPageChangeListener(this::firePageChangeEvent);
        pages.setDataRefreshedProvider(this::refreshData);

        List<Paging.Page> pageBtns = pages.getPages();
        for (int i = 0; i < pageBtns.size(); i++) {
            component.addComponent(pageBtns.get(i).getButton(), 2 + i);
        }
    }

    protected void removePageButtons() {
        for (Paging.Page page : pages.getPages()) {
            component.removeComponent(page.getButton());
        }
        pages.removePages();
    }

    protected void updateState() {
        State state = pages.getLastPageNumber() == -1 ? State.FIRST_COMPLETE : getCurrentState();

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
        if (pages.getCurrentPageNumber() == FIRST_PAGE) {
            return pages.getLastPageNumber() != 1 ? State.FIRST_INCOMPLETE : State.FIRST_COMPLETE;
        } else if (pages.getCurrentPageNumber() == pages.getLastPageNumber()) {
            return State.LAST;
        }
        return State.MIDDLE;
    }

    protected void updateItemsPerPageAvailability() {
        getItemsPerPageComboBox().setEnabled(!isEmptyOrNullDataBinder());
    }
}