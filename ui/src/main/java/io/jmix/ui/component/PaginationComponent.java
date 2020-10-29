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
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.pagination.data.PaginationDataSourceProvider;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Base interface for components that make a data binding to load data by pages.
 */
public interface PaginationComponent extends Component.BelongToFrame {

    /**
     * @return supplier which is used to get the total number of items.
     */
    @Nullable
    Supplier<Integer> getTotalCountSupplier();

    /**
     * Sets supplier which is used to get the total number of items.
     */
    void setTotalCountSupplier(@Nullable Supplier<Integer> totalCountSupplier);

    /**
     * @return a data source provider that is used for pagination.
     */
    @Nullable
    PaginationDataSourceProvider getDataSourceProvider();

    /**
     * Sets a data source provider that should be used for pagination.
     */
    void setDataSourceProvider(PaginationDataSourceProvider dataSourceProvider);

    /**
     * Adds before refresh listener. It is invoked when the user clicks navigation buttons (next, last etc).
     *
     * @return a registration object for removing an event listener
     */
    Subscription addBeforeRefreshListener(Consumer<BeforeRefreshEvent> listener);

    /**
     * Adds after refresh listener. It is invoked when {@link BeforeRefreshEvent} is not prevented.
     *
     * @return a registration object for removing an event listener
     */
    Subscription addAfterRefreshListener(Consumer<AfterRefreshEvent> listener);

    /**
     * @return true if items per page ComboBox is visible
     */
    boolean isItemsPerPageVisible();

    /**
     * Sets visibility of items per page ComboBox. Default value is {@code false}.
     */
    void setItemsPerPageVisible(boolean itemsPerPageVisible);

    /**
     * @return {@code true} if null option should be visible in the items per page ComboBox.
     */
    boolean isNullItemsPerPageOptionVisible();

    /**
     * Sets null option visible or hidden in the items per page ComboBox. If null option is selected
     * component will try to load data with {@link UiProperties#getEntityMaxFetchSize(String)} limitation.
     * Default value is {@code true}.
     */
    void setNullItemsPerPageOptionVisible(boolean nullOptionVisible);

    /**
     * @return items per page options.
     */
    List<Integer> getItemsPerPageOptions();

    /**
     * Sets items per page options which should be used in the ComboBox. Options less than or equal to 0
     * are ignored, options greater than entity's max fetch size will be replaced by it.
     *
     * @param options items per page options
     * @see UiProperties#getEntityMaxFetchSize(String)
     */
    void setItemsPerPageOptions(List<Integer> options);

    /**
     * @return items per page default value or {@code null}
     */
    @Nullable
    Integer getItemsPerPageDefaultValue();

    /**
     * Sets default value for the items per page ComboBox.
     */
    void setItemsPerPageDefaultValue(@Nullable Integer defaultValue);

    /**
     * Event that is fired before refreshing the data container when the user clicks next, previous, etc.
     * <br>
     * You can prevent the data container refresh by invoking {@link BeforeRefreshEvent#preventRefresh()},
     * for example:
     * <pre>{@code
     * usersTable.getPagination().addBeforeRefreshListener(refreshEvent -> {
     *     // check modified data and prevent refresh
     *     refreshEvent.preventRefresh();
     * });
     * }</pre>
     */
    class BeforeRefreshEvent extends EventObject {
        protected boolean refreshPrevented = false;

        public BeforeRefreshEvent(PaginationComponent source) {
            super(source);
        }

        @Override
        public PaginationComponent getSource() {
            return (PaginationComponent) super.getSource();
        }

        /**
         * If invoked, the component will not refresh the data container.
         */
        public void preventRefresh() {
            refreshPrevented = true;
        }

        public boolean isRefreshPrevented() {
            return refreshPrevented;
        }
    }

    /**
     * Event that is fired after data refresh, i.e. when {@link BeforeRefreshEvent} is not prevented.
     */
    class AfterRefreshEvent extends EventObject {

        public AfterRefreshEvent(PaginationComponent source) {
            super(source);
        }

        @Override
        public PaginationComponent getSource() {
            return (PaginationComponent) super.getSource();
        }
    }
}
