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

package io.jmix.ui.component;

import io.jmix.core.common.event.Subscription;

import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Component that makes a data binding to load data by pages. It contains page numbers that enable the
 * user to select a specific page.
 */
public interface Pagination extends PaginationComponent {

    String NAME = "pagination";

    /**
     * @return maximum number of visible pages.
     */
    int getMaxVisiblePages();

    /**
     * Sets maximum number of visible pages.
     */
    void setMaxVisiblePages(int maxVisiblePages);

    /**
     * Adds page change listener. Is fired when page number is changed.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addPageChangeListener(Consumer<PageChangeEvent> listener);

    /**
     * Event that is fired when user goes to another page.
     */
    class PageChangeEvent extends EventObject {

        protected int previousPageNumber;
        protected int pageNumber;

        public PageChangeEvent(Pagination source, int previousPageNumber, int pageNumber) {
            super(source);

            this.previousPageNumber = previousPageNumber;
            this.pageNumber = pageNumber;
        }

        @Override
        public Pagination getSource() {
            return (Pagination) super.getSource();
        }

        /**
         * @return previous page number
         */
        public int getPreviousPageNumber() {
            return previousPageNumber;
        }

        /**
         * @return current page number
         */
        public int getPageNumber() {
            return pageNumber;
        }
    }
}
