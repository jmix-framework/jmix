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
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperty;

import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Component that makes a data binding to load data by pages. It contains page numbers that enable the
 * user to select a specific page.
 */
@StudioComponent(
        caption = "Pagination",
        category = "Components",
        xmlElement = "pagination",
        icon = "io/jmix/ui/icon/component/pagination.svg",
        canvasBehaviour = CanvasBehaviour.BOX,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/pagination.html",
        unsupportedProperties = {"responsive", "enable"}
)
public interface Pagination extends PaginationComponent {

    String NAME = "pagination";

    /**
     * @return maximum number of visible pages.
     */
    int getMaxVisiblePages();

    /**
     * Sets the maximum number of visible pages. The component can have a lot of pages, but users will see
     * a number of pages at once that corresponds to the maximum number of visible pages. For instance,
     * the component has 10 pages and the maximum number of visible pages is 3, so users will see only 3 pages
     * at once. The default value is 5.
     */
    @StudioProperty(defaultValue = "5")
    void setMaxVisiblePages(int maxVisiblePages);

    /**
     * Adds page change listener. It will be invoked when the user selects another page or clicks on navigation
     * buttons (next, previous, etc).
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addPageChangeListener(Consumer<PageChangeEvent> listener);

    /**
     * The event that is fired when the user selects another page or clicks on navigation buttons (next, previous, etc).
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
