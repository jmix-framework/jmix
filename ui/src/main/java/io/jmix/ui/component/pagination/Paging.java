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

package io.jmix.ui.component.pagination;

import com.vaadin.ui.Button;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.component.Pagination;
import io.jmix.ui.component.pagination.data.PaginationDataBinder;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Class for managing pages in {@link Pagination} component.
 */
@Internal
public class Paging {

    public static final String NAME = "ui_PaginationPages";

    protected List<Page> pages;
    protected PaginationDataBinder dataBinder;

    protected int maxVisiblePages = 5;

    protected int currentPageNumber = -1;
    protected int lastPageNumber = -1;
    protected int itemsToDisplay = Integer.MAX_VALUE;

    protected BiConsumer<Integer, Integer> pageChangeListener;
    protected Supplier<Boolean> dataRefreshedProvider;
    protected Runnable afterRefreshListener;

    /**
     * @return maximum number of visible pages
     */
    public int getMaxVisiblePages() {
        return maxVisiblePages;
    }

    /**
     * Sets maximum number of visible pages.
     */
    public void setMaxVisiblePages(int maxVisiblePages) {
        if (maxVisiblePages < 1) {
            throw new IllegalStateException("At least one page must be visible");
        }

        this.maxVisiblePages = maxVisiblePages;
    }

    /**
     * @return current page number
     */
    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    /**
     * Sets current page number. Note, page number starts from {@code 0}.
     */
    public void setCurrentPageNumber(int currentPageNumber) {
        this.currentPageNumber = currentPageNumber;
    }

    /**
     * @return number of last page
     */
    public int getLastPageNumber() {
        return lastPageNumber;
    }

    /**
     * @return items count that should be displayed per page
     */
    public int getItemsToDisplay() {
        return itemsToDisplay;
    }

    /**
     * @return data binder
     */
    public PaginationDataBinder getDataBinder() {
        return dataBinder;
    }

    /**
     * Sets pagination data binder.
     */
    public void setDataBinder(PaginationDataBinder dataBinder) {
        this.dataBinder = dataBinder;
    }

    /**
     * Creates pages based on total count of items and items count to display per page.
     *
     * @param totalCount     total count of items
     * @param itemsToDisplay items count to display per page
     */
    public void createPages(int totalCount, int itemsToDisplay) {
        this.itemsToDisplay = itemsToDisplay;

        lastPageNumber = totalCount / itemsToDisplay;
        lastPageNumber = totalCount % itemsToDisplay == 0 ? lastPageNumber : lastPageNumber + 1;

        for (int i = 0; i < lastPageNumber; i++) {
            if (i == maxVisiblePages) {
                break;
            }
            addPage(new Page(i + 1, itemsToDisplay));
        }
    }

    protected void addPage(Page page) {
        if (pages == null) {
            pages = new ArrayList<>(maxVisiblePages);
        }

        pages.add(page);
    }

    /**
     * Removes all pages.
     */
    public void removePages() {
        if (CollectionUtils.isNotEmpty(pages)) {
            pages.clear();
            pages = null;
        }
    }

    /**
     * @return pages
     */
    public List<Page> getPages() {
        if (pages == null) {
            return Collections.emptyList();
        }
        return pages;
    }

    /**
     * @return current page or {@code null}
     */
    @Nullable
    public Page getCurrentPage() {
        if (CollectionUtils.isEmpty(pages)) {
            return null;
        }

        return pages.stream()
                .filter(page -> page.getNumber() == getCurrentPageNumber())
                .findFirst()
                .orElse(null);
    }

    /**
     * Sets page number based on provided first result. Throws an exception if page number cannot be set.
     */
    public void setCurrentPageNumberByFirstResult(int firstResult) {
        if (firstResult == 0) {
            setCurrentPageNumber(1);
        }

        if (firstResult % itemsToDisplay == 0) {
            int pageIdx = firstResult / itemsToDisplay;
            if (pageIdx + 1 > lastPageNumber) {
                throw new IllegalStateException(
                        String.format("Page number '%s' must be in range [1 - %s]",
                                pageIdx + 1, lastPageNumber));
            }
            setCurrentPageNumber(pageIdx + 1);
            return;
        }

        throw new IllegalStateException("PaginationDataSourceProvider has first result that is not applicable " +
                "for the page ranges");
    }

    public boolean isFirstResultInRanges(int firstResult) {
        return firstResult <= (lastPageNumber - 1) * itemsToDisplay;
    }

    /**
     * Selects first page.
     */
    public void selectFirstPage() {
        selectPage(1);
    }

    /**
     * Selects previous page.
     */
    public void selectPreviousPage() {
        selectPage(getCurrentPageNumber() - 1);
    }

    /**
     * Selects next page.
     */
    public void selectNextPage() {
        selectPage(getCurrentPageNumber() + 1);
    }

    /**
     * Selects last page.
     */
    public void selectLastPage() {
        selectPage(getLastPageNumber());
    }

    /**
     * Sets selected page.
     *
     * @param pageNumber number of page that should be selected, starts from 1
     */
    public void selectPage(int pageNumber) {
        if (getCurrentPageNumber() == pageNumber
                || isPageIndexNotInRange(pageNumber - 1)) {
            return;
        }

        int previousPage = getCurrentPageNumber();
        int previousFirstResult = dataBinder.getFirstResult();

        setCurrentPageNumber(pageNumber);

        dataBinder.setFirstResult((pageNumber - 1) * itemsToDisplay);

        if (isDataRefreshed()) {
            fireAfterRefreshEvent();
            firePageChangeEvent(previousPage, getCurrentPageNumber());
        } else {
            setCurrentPageNumber(previousPage);
            dataBinder.setFirstResult(previousFirstResult);
        }
    }

    /**
     * Sets selected page without firing refresh event.
     *
     * @param pageNumber number of page that should be selected, starts from 1
     */
    public void forceSelectPage(int pageNumber) {
        int previousPage = getCurrentPageNumber();

        setCurrentPageNumber(pageNumber);

        dataBinder.setFirstResult((pageNumber - 1) * itemsToDisplay);
        dataBinder.refresh();

        fireAfterRefreshEvent();

        if (previousPage != getCurrentPageNumber()) {
            firePageChangeEvent(previousPage, getCurrentPageNumber());
        }
    }

    /**
     * Sets after data refresh listener. Is invoked if {@link #isDataRefreshed()} returns {@code true}.
     */
    public void setOnAfterDataRefreshListener(Runnable listener) {
        afterRefreshListener = listener;
    }

    /**
     * Sets page change listener.
     */
    public void setPageChangeListener(BiConsumer<Integer, Integer> listener) {
        pageChangeListener = listener;
    }

    /**
     * Sets data refreshed provider. Provides {@code true} if data refreshing is finished successfully.
     */
    public void setDataRefreshedProvider(Supplier<Boolean> dataRefreshedProvider) {
        this.dataRefreshedProvider = dataRefreshedProvider;
    }

    protected void fireAfterRefreshEvent() {
        if (afterRefreshListener != null) {
            afterRefreshListener.run();
        }
    }

    protected void firePageChangeEvent(int previousPage, int currentPage) {
        if (pageChangeListener != null) {
            pageChangeListener.accept(previousPage, currentPage);
        }
    }

    protected boolean isDataRefreshed() {
        return dataRefreshedProvider != null && dataRefreshedProvider.get();
    }

    protected boolean isPageIndexNotInRange(int pageIdx) {
        return CollectionUtils.isEmpty(pages) || pageIdx < 0 || pageIdx >= lastPageNumber;
    }

    public void updatePageSelections() {
        for (Paging.Page page : getPages()) {
            page.setSelected(page.getNumber() == getCurrentPageNumber());
        }
    }

    public void updatePageNumbers() {
        if (getCurrentPageNumber() == -1 || CollectionUtils.isEmpty(pages)) {
            return;
        }

        // decrease maxVisiblePages by 1 because we do not include current page
        // to length of shown pages from left and right;
        int rightSize = (getMaxVisiblePages() - 1) / 2;
        int leftSize = (getMaxVisiblePages() - 1) % 2 != 0 ? rightSize + 1 : rightSize;

        int newRightNumber = getCurrentPageNumber() + rightSize;
        int newLeftNumber = getCurrentPageNumber() - leftSize;

        int visibleNumber = newLeftNumber;

        if (newRightNumber > getLastPageNumber()) {
            newRightNumber = getLastPageNumber();
            visibleNumber = newRightNumber - getPages().size() + 1;
        } else if (newLeftNumber < 1) {
            visibleNumber = 1;
        }

        for (Page page : pages) {
            page.setNumber(visibleNumber);
            page.setCaption(String.valueOf(visibleNumber));

            visibleNumber++;
        }
    }

    /**
     * Class that contains information about page.
     */
    @Internal
    public class Page {

        public static final String PRIMARY_STYLENAME = "jmix-pagination-page";
        public static final String SELECTED_PAGE_STYLENAME = "selected-page";

        protected Button button;
        protected boolean selected = false;

        protected final int itemsPerPage;
        protected int number;

        public Page(int number, int itemsPerPage) {
            this.number = number;
            this.itemsPerPage = itemsPerPage;

            button = new Button();
            button.setCaption(String.valueOf(number));
            button.setStyleName(PRIMARY_STYLENAME);
            button.addClickListener(this::onPageButtonClickEvent);
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;

            button.removeStyleName(SELECTED_PAGE_STYLENAME);

            if (selected) {
                button.addStyleName(SELECTED_PAGE_STYLENAME);
            }
        }

        public Button getButton() {
            return button;
        }

        public void setCaption(String caption) {
            button.setCaption(caption);
        }

        public int getFirstResult() {
            return (getNumber() - 1) * itemsPerPage;
        }

        protected void onPageButtonClickEvent(Button.ClickEvent event) {
            if (getCurrentPageNumber() == getNumber()) {
                return;
            }

            int previousPageNumber = getCurrentPageNumber();
            int savedFirstResult = getFirstResult();

            setCurrentPageNumber(getNumber());
            dataBinder.setFirstResult(getFirstResult());

            if (isDataRefreshed()) {
                fireAfterRefreshEvent();
                firePageChangeEvent(previousPageNumber, getCurrentPageNumber());
            } else {
                setCurrentPageNumber(previousPageNumber);
                dataBinder.setFirstResult(savedFirstResult);
            }
        }
    }
}
