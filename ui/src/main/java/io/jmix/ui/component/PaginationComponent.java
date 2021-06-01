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
import io.jmix.ui.component.pagination.data.PaginationDataBinder;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

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
     * @return delegate which is used to get the total count of items.
     */
    @Nullable
    Supplier<Integer> getTotalCountDelegate();

    /**
     * Sets delegate which is used to get the total count of items. For instance:
     * <pre>
     * &#64;Autowired
     * private DataManager dataManager;
     *
     * &#64;Install(to = "pagination", subject = "totalCountDelegate")
     * private Integer paginationTotalCountDelegate() {
     *     return dataManager.loadValue("select count(e) from demo_User e", Integer.class).one();
     * }
     * </pre>
     */
    void setTotalCountDelegate(@Nullable Supplier<Integer> totalCountDelegate);

    /**
     * @return a data binder
     */
    @Nullable
    PaginationDataBinder getDataBinder();

    /**
     * Sets a data binder. It is used for managing data loading and dividing data to pages.
     */
    @StudioElement
    void setDataBinder(PaginationDataBinder dataBinder);

    /**
     * Adds before refresh listener. It is invoked when data should be refreshed after user actions:
     * click on navigation buttons (next, last etc), change items per page value.
     *
     * @return a registration object for removing an event listener
     */
    Subscription addBeforeRefreshListener(Consumer<BeforeRefreshEvent> listener);

    /**
     * Adds after refresh listener. It is invoked when data is refreshed.
     *
     * @return a registration object for removing an event listener
     */
    Subscription addAfterRefreshListener(Consumer<AfterRefreshEvent> listener);

    /**
     * @return true if items per page ComboBox is visible
     */
    boolean isItemsPerPageVisible();

    /**
     * Sets visibility of items per page ComboBox. This ComboBox contains options to limit the number
     * of items for one page. If custom options are not set component will use
     * {@link UiComponentProperties#getPaginationItemsPerPageOptions()}. The default value is {@code false}.
     */
    @StudioProperty(defaultValue = "false")
    void setItemsPerPageVisible(boolean itemsPerPageVisible);

    /**
     * @return {@code true} if unlimited (null) option should be visible in the items per page ComboBox.
     */
    boolean isItemsPerPageUnlimitedOptionVisible();

    /**
     * Sets visibility of unlimited (null) option value in the items per page ComboBox. If unlimited (null) option
     * is selected component will try to load data with {@link UiProperties#getEntityMaxFetchSize(String)} limitation.
     * The default value is {@code true}.
     */
    @StudioProperty(name = "itemsPerPageUnlimitedOptionVisible", defaultValue = "true")
    void setItemsPerPageUnlimitedOptionVisible(boolean unlimitedOptionVisible);

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
    @StudioProperty(name = "itemsPerPageOptions", type = PropertyType.STRING)
    void setItemsPerPageOptions(List<Integer> options);

    /**
     * @return items per page default value or {@code null}
     */
    @Nullable
    Integer getItemsPerPageDefaultValue();

    /**
     * Sets default value for the items per page ComboBox.
     */
    @StudioProperty(name = "itemsPerPageDefaultValue", type = PropertyType.INTEGER)
    void setItemsPerPageDefaultValue(@Nullable Integer defaultValue);

    /**
     * The event that is fired before refreshing the data when the user clicks next, previous, etc.
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
     * The event that is fired after data refresh.
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
