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

package io.jmix.ui.widget;

import com.vaadin.ui.Button;
import io.jmix.ui.component.pagination.data.PaginationDataBinder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class JmixPagination extends JmixAbstractPagination {

    protected static final String PRIMARY_STYLENAME = "jmix-pagination";

    protected List<JmixPage> pages;
    protected PaginationDataBinder dataBinder;

    protected int maxVisiblePages = 5;
    protected int currentPageNumber = -1;
    protected int lastPageNumber = -1;
    protected int itemsToDisplay = Integer.MAX_VALUE;

    protected BiConsumer<Integer, Integer> pageChangeListener;
    protected Supplier<Boolean> dataRefreshedProvider;
    protected Runnable afterRefreshListener;

    public JmixPagination() {
        super(PRIMARY_STYLENAME);

        createNavigationButtons();
        createContentLayout();
    }

    protected void createContentLayout() {
        setStyleName(PRIMARY_STYLENAME);

        addComponents(getFirstButton(), getPrevButton(), getNextButton(), getLastButton());

        getItemsPerPageLayout().setVisible(false);
        addComponent(getItemsPerPageLayout());
    }

    /**
     * @return maximum number of visible pages
     */
    public int getMaxVisiblePages() {
        return maxVisiblePages;
    }

    /**
     * Sets maximum number of visible pages.
     *
     * @param maxVisiblePages maximum visible pages value
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
     * Sets current page number. Note, page number starts from {@code 1}.
     *
     * @param currentPageNumber current page number value
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
     * @return data source provider
     */
    public PaginationDataBinder getDataBinder() {
        return dataBinder;
    }

    /**
     * Sets pagination data binder.
     *
     * @param dataBinder data binder
     */
    public void setDataBinder(PaginationDataBinder dataBinder) {
        this.dataBinder = dataBinder;
    }

    /**
     * Removes all pages from the layout.
     */
    public void removePages() {
        if (CollectionUtils.isNotEmpty(pages)) {
            for (JmixPage page : pages) {
                removeComponent(page.getButton());
            }
            pages.clear();
            pages = null;
        }
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

        if (pages == null) {
            pages = new ArrayList<>(maxVisiblePages);
        }

        for (int i = 0; i < lastPageNumber; i++) {
            if (i == maxVisiblePages) {
                break;
            }
            JmixPage page = new JmixPage(i + 1, itemsToDisplay);
            pages.add(page);
            addComponent(page.getButton(), 2 + i);
        }
    }

    /**
     * @return pages list
     */
    public List<JmixPage> getPages() {
        if (pages == null) {
            return Collections.emptyList();
        }
        return pages;
    }

    /**
     * @return current page or {@code null} if list of pages is empty
     * or number of current page is out of page number range
     */
    @Nullable
    public JmixPage getCurrentPage() {
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
     *
     * @param firstResult first result value
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

        throw new IllegalStateException("PaginationDataBinder has first result that is not applicable " +
                "for the page ranges");
    }

    /**
     * Checks that first result is not greater than last page's first result.
     *
     * @param firstResult first result value to check
     * @return {@code true} if provided first result is less than or equal to last page's first result
     */
    public boolean isFirstResultInRanges(int firstResult) {
        return firstResult <= (lastPageNumber - 1) * itemsToDisplay;
    }

    /**
     * Selects first page or do nothing if current page is first.
     */
    public void selectFirstPage() {
        selectPage(1);
    }

    /**
     * Selects next page or do nothing if current page is last.
     */
    public void selectNextPage() {
        selectPage(getCurrentPageNumber() + 1);
    }

    /**
     * Selects previous page or do nothing if current page is first.
     */
    public void selectPreviousPage() {
        selectPage(getCurrentPageNumber() - 1);
    }

    /**
     * Selects last page or do nothing if current page is last.
     */
    public void selectLastPage() {
        selectPage(getLastPageNumber());
    }

    /**
     * Sets page by its number. Note, page number starts from 1.
     *
     * @param pageNumber number of page
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
     * Sets page without firing refresh event. Note, page number starts from 1.
     *
     * @param pageNumber number of page
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
     * Sets after data refresh listener. Is invoked data refreshing is finished successfully.
     */
    public void setOnAfterRefreshListener(Runnable listener) {
        afterRefreshListener = listener;
    }

    protected void fireAfterRefreshEvent() {
        if (afterRefreshListener != null) {
            afterRefreshListener.run();
        }
    }

    /**
     * Sets page change listener. Listener is invoked when selected page switched to another.
     */
    public void setPageChangeListener(BiConsumer<Integer, Integer> listener) {
        pageChangeListener = listener;
    }

    protected void firePageChangeEvent(int previousPage, int currentPage) {
        if (pageChangeListener != null) {
            pageChangeListener.accept(previousPage, currentPage);
        }
    }

    /**
     * Sets data refreshed provider. Provides {@code true} if data refreshing is finished successfully.
     */
    public void setDataRefreshedProvider(Supplier<Boolean> dataRefreshedProvider) {
        this.dataRefreshedProvider = dataRefreshedProvider;
    }

    /**
     * Sets to pages selected or not based on {@link #getCurrentPageNumber()}.
     */
    public void updatePageSelections() {
        for (JmixPage page : getPages()) {
            page.setSelected(page.getNumber() == getCurrentPageNumber());
        }
    }

    /**
     * Calculates page numbers and captions based on {@link #getCurrentPageNumber()}.
     */
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

        for (JmixPage page : pages) {
            page.setNumber(visibleNumber);
            page.getButton().setCaption(String.valueOf(visibleNumber));

            visibleNumber++;
        }
    }

    protected boolean isDataRefreshed() {
        return dataRefreshedProvider != null && dataRefreshedProvider.get();
    }

    protected boolean isPageIndexNotInRange(int pageIdx) {
        return CollectionUtils.isEmpty(pages) || pageIdx < 0 || pageIdx >= lastPageNumber;
    }

    /**
     * Class describes data page in the component.
     */
    public class JmixPage {

        public static final String PRIMARY_STYLENAME = "jmix-pagination-page";
        public static final String SELECTED_PAGE_STYLENAME = "selected-page";

        protected Button button;
        protected boolean selected = false;

        protected final int itemsPerPage;
        protected int number;

        public JmixPage(int number, int itemsPerPage) {
            this.number = number;
            this.itemsPerPage = itemsPerPage;

            button = new Button();
            button.setCaption(String.valueOf(number));
            button.setStyleName(PRIMARY_STYLENAME);
            button.addClickListener(this::onPageButtonClickEvent);
        }

        /**
         * @return number of page starting from {@code 1}
         */
        public int getNumber() {
            return number;
        }

        /**
         * Sets page number. Note, page number starts from 1.
         *
         * @param number page number value
         */
        public void setNumber(int number) {
            this.number = number;
        }

        /**
         * @return {@code true} if page is currently selected
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * Sets page selected.
         *
         * @param selected whether page should be selected
         */
        public void setSelected(boolean selected) {
            this.selected = selected;

            button.removeStyleName(SELECTED_PAGE_STYLENAME);

            if (selected) {
                button.addStyleName(SELECTED_PAGE_STYLENAME);
            }
        }

        /**
         * @return the button that represents "page" in the UI
         */
        public Button getButton() {
            return button;
        }

        /**
         * @return the first result value that should be used for loading items
         */
        public int getFirstResult() {
            return (getNumber() - 1) * itemsPerPage;
        }

        protected void onPageButtonClickEvent(Button.ClickEvent event) {
            if (getCurrentPageNumber() == getNumber()) {
                return;
            }

            int previousPageNumber = getCurrentPageNumber();
            int previousFirstResult = getFirstResult();

            setCurrentPageNumber(getNumber());
            dataBinder.setFirstResult(getFirstResult());

            if (isDataRefreshed()) {
                fireAfterRefreshEvent();
                firePageChangeEvent(previousPageNumber, getCurrentPageNumber());
            } else {
                setCurrentPageNumber(previousPageNumber);
                dataBinder.setFirstResult(previousFirstResult);
            }
        }
    }
}
