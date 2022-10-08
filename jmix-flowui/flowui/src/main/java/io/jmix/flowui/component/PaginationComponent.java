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

package io.jmix.flowui.component;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.LoadContext;
import io.jmix.flowui.data.pagination.PaginationDataLoader;
import io.jmix.flowui.kit.component.pagination.AbstractPagination;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Base interface for components that make a data binding to load data by pages.
 */
public interface PaginationComponent<T extends AbstractPagination> {

    /**
     * @return pagination data loader or {@code null} if not set
     */
    @Nullable
    PaginationDataLoader getPaginationLoader();

    /**
     * Sets loader to the component.
     *
     * @param loader pagination data loader to set
     */
    void setPaginationLoader(@Nullable PaginationDataLoader loader);

    /**
     * @return delegate which is used to get the total count of items
     */
    @Nullable
    Function<LoadContext, Integer> getTotalCountDelegate();

    /**
     * Sets delegate which is used to get the total count of items. For instance:
     * <pre>
     * &#64;Autowired
     * private DataManager dataManager;
     *
     * &#64;Install(to = "pagination", subject = "totalCountDelegate")
     * private Integer paginationTotalCountDelegate(LoadContext&lt;User&gt; dataLoadContext) {
     *     return dataManager.loadValue("select count(e) from demo_User e", Integer.class).one();
     * }
     * </pre>
     *
     * @param totalCountDelegate total count delegate to set
     */
    void setTotalCountDelegate(@Nullable Function<LoadContext, Integer> totalCountDelegate);

    /**
     * Adds before refresh listener. It is invoked when data should be refreshed after user actions:
     * click on navigation buttons (next, last etc), change items per page value.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener
     */
    Registration addBeforeRefreshListener(ComponentEventListener<BeforeRefreshEvent<T>> listener);

    /**
     * Adds after refresh listener. It is invoked when data is refreshed.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener
     */
    Registration addAfterRefreshListener(ComponentEventListener<AfterRefreshEvent<T>> listener);

    /**
     * The event that is fired before refreshing the data, when the user clicks next, previous, etc.
     * <br>
     * You can prevent the data container refresh by invoking {@link BeforeRefreshEvent#preventRefresh()},
     * for example:
     * <pre>{@code
     * pagination.addBeforeRefreshListener(refreshEvent -> {
     *     // check modified data and prevent refresh
     *     refreshEvent.preventRefresh();
     * });
     * }</pre>
     */
    class BeforeRefreshEvent<T extends AbstractPagination> extends ComponentEvent<T> {
        protected boolean refreshPrevented = false;

        public BeforeRefreshEvent(T source) {
            super(source, false);
        }

        /**
         * If invoked, the component will not refresh the data container.
         */
        public void preventRefresh() {
            refreshPrevented = true;
        }

        /**
         * @return {@code true} if data refresh is prevented
         */
        public boolean isRefreshPrevented() {
            return refreshPrevented;
        }
    }

    /**
     * The event that is fired after data refresh.
     */
    class AfterRefreshEvent<T extends AbstractPagination> extends ComponentEvent<T> {

        public AfterRefreshEvent(T source) {
            super(source, false);
        }
    }
}
