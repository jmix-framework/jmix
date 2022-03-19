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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.DataLoadContext;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.Pagination;
import io.jmix.ui.component.Table;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Component compatible with {@link Datasource}.
 *
 * @deprecated Use {@link Pagination} instead
 */
@SuppressWarnings("rawtypes")
@Deprecated
public interface RowsCount extends Component.BelongToFrame {

    /**
     * @deprecated assign {@link RowsCountTarget} instead.
     */
    @Deprecated
    CollectionDatasource getDatasource();

    /**
     * @deprecated assign {@link RowsCountTarget} instead.
     */
    @Deprecated
    void setDatasource(CollectionDatasource datasource);

    enum State {
        FIRST_COMPLETE,     // "63 rows"
        FIRST_INCOMPLETE,   // "1-100 rows of [?] >"
        MIDDLE,             // "< 101-200 rows of [?] >"
        LAST                // "< 201-252 rows"
    }

    String NAME = "rowsCount";

    /**
     * @return whether rows count should be loaded automatically
     */
    boolean getAutoLoad();

    /**
     * Sets whether rows count should be loaded automatically.
     *
     * @param autoLoad pass true to enable auto load, or false otherwise
     */
    void setAutoLoad(boolean autoLoad);

    /**
     * @return a component that displays data from the same datasource, usually a {@link Table}. Can be null.
     */
    @Deprecated
    @Nullable
    ListComponent getOwner();
    /**
     * @deprecated Use {@link #setRowsCountTarget(RowsCount.RowsCountTarget)} instead.
     */
    @Deprecated
    void setOwner(@Nullable ListComponent owner);

    /**
     * @return a component that displays data, usually a {@link Table}. Can be null.
     */
    RowsCountTarget getRowsCountTarget();
    void setRowsCountTarget(RowsCount.RowsCountTarget target);

    interface RowsCountTarget extends BelongToFrame {
    }

    /**
     * @return delegate which is used to get the total number of rows when user clicks "total count" or "last page".
     */
    Function<DataLoadContext, Long> getTotalCountDelegate();

    /**
     * Sets delegate which is used to get the total number of rows when user clicks "total count" or "last page".
     */
    void setTotalCountDelegate(Function<DataLoadContext, Long> delegate);

    /**
     * Event that is fired before refreshing the datasource when the user clicks next, previous, etc.
     * <br>
     * You can prevent the datasource refresh by invoking {@link RowsCount.BeforeRefreshEvent#preventRefresh()},
     * for example:
     * <pre>{@code
     * table.getRowsCount().addBeforeDatasourceRefreshListener(event -> {
     *     if (event.getDatasource().isModified()) {
     *         showNotification("Save changes before going to another page");
     *         event.preventRefresh();
     *     }
     * });
     * }</pre>
     */
    class BeforeRefreshEvent extends EventObject {
        protected boolean refreshPrevented = false;

        public BeforeRefreshEvent(RowsCount source) {
            super(source);
        }

        /**
         * If invoked, the component will not refresh the datasource.
         */
        public void preventRefresh() {
            refreshPrevented = true;
        }

        public boolean isRefreshPrevented() {
            return refreshPrevented;
        }
    }

    void addBeforeRefreshListener(Consumer<RowsCount.BeforeRefreshEvent> listener);
    void removeBeforeRefreshListener(Consumer<RowsCount.BeforeRefreshEvent> listener);

    /**
     * A listener to be notified before refreshing the datasource when the user clicks next, previous, etc.
     *
     * @deprecated Use {@link Consumer} with {@link RowsCount.BeforeRefreshEvent} instead.
     */
    @FunctionalInterface
    @Deprecated
    interface BeforeRefreshListener extends Consumer<RowsCount.BeforeRefreshEvent> {
        void beforeDatasourceRefresh(RowsCount.BeforeRefreshEvent event);

        @Override
        default void accept(RowsCount.BeforeRefreshEvent beforeRefreshEvent) {
            beforeDatasourceRefresh(beforeRefreshEvent);
        }
    }
}
