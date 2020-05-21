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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.RowsCount;
import com.haulmont.cuba.gui.components.data.meta.DatasourceDataUnit;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.WeakCollectionChangeListener;
import io.jmix.ui.component.data.DataUnit;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@SuppressWarnings("rawtypes")
@Deprecated
public class WebRowsCount extends io.jmix.ui.component.impl.WebRowsCount implements RowsCount {

    @Override
    public CollectionDatasource getDatasource() {
        return adapter instanceof AbstractDatasourceAdapter
                ? ((AbstractDatasourceAdapter) adapter).getDatasource()
                : null;
    }

    @Override
    public void setDatasource(CollectionDatasource datasource) {
        checkNotNullArgument(datasource, "datasource is null");

        if (adapter != null) {
            adapter.unbind();
        }
        adapter = createDatasourceAdapter(datasource);

        initButtonListeners();
    }

    protected Adapter createDatasourceAdapter(CollectionDatasource datasource) {
        if (datasource instanceof CollectionDatasource.SupportsPaging) {
            return new DatasourceAdapter((CollectionDatasource.SupportsPaging) datasource);
        } else {
            return new NoPagingDatasourceAdapter(datasource);
        }
    }

    @Override
    protected Adapter createDefaultAdapter(DataUnit items) {
        try {
            // since RowsCount does not have its own loader and cuba module is added
            // AbstractTableLoader will always create legacy component, so firstly we
            // need to invoke parent implementation
            return super.createDefaultAdapter(items);
        } catch (IllegalStateException e) {
            // try to create with datasource
            if (items instanceof DatasourceDataUnit) {
                return createDatasourceAdapter(((DatasourceDataUnit) items).getDatasource());
            }
        }

        throw new IllegalStateException("Unsupported data unit type: " + items);
    }

    protected class DatasourceAdapter extends AbstractDatasourceAdapter {

        public DatasourceAdapter(CollectionDatasource.SupportsPaging datasource) {
            super(datasource);
        }

        @Override
        public int getFirstResult() {
            return ((CollectionDatasource.SupportsPaging) datasource).getFirstResult();
        }

        @Override
        public int getMaxResults() {
            return datasource.getMaxResults();
        }

        @Override
        public void setFirstResult(int startPosition) {
            ((CollectionDatasource.SupportsPaging) datasource).setFirstResult(startPosition);
        }

        @Override
        public void setMaxResults(int maxResults) {
            datasource.setMaxResults(maxResults);
        }

        @Override
        public int getCount() {
            return ((CollectionDatasource.SupportsPaging) datasource).getCount();
        }

        @Override
        public void refresh() {
            datasource.refresh();
        }
    }

    protected class NoPagingDatasourceAdapter extends AbstractDatasourceAdapter {

        public NoPagingDatasourceAdapter(CollectionDatasource datasource) {
            super(datasource);
        }

        @Override
        public int getFirstResult() {
            return 0;
        }

        @Override
        public int getMaxResults() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void setFirstResult(int startPosition) {
            // do nothing
        }

        @Override
        public void setMaxResults(int maxResults) {
            // do nothing
        }

        @Override
        public int getCount() {
            return size();
        }

        @Override
        public void refresh() {
            // do nothing
        }
    }

    protected abstract class AbstractDatasourceAdapter implements Adapter {

        protected CollectionDatasource datasource;

        protected CollectionDatasource.CollectionChangeListener datasourceCollectionChangeListener;
        protected WeakCollectionChangeListener weakDatasourceCollectionChangeListener;

        public AbstractDatasourceAdapter(CollectionDatasource datasource) {
            this.datasource = datasource;

            datasourceCollectionChangeListener = e -> {
                samePage = CollectionDatasource.Operation.REFRESH != e.getOperation()
                        && CollectionDatasource.Operation.CLEAR != e.getOperation();
                onCollectionChanged();
            };

            weakDatasourceCollectionChangeListener = new WeakCollectionChangeListener(datasource, datasourceCollectionChangeListener);
            //noinspection unchecked
            datasource.addCollectionChangeListener(weakDatasourceCollectionChangeListener);

            if (datasource.getState() == Datasource.State.VALID) {
                onCollectionChanged();
            }
        }

        @Override
        public void unbind() {
            //noinspection unchecked
            datasource.removeCollectionChangeListener(weakDatasourceCollectionChangeListener);
            weakDatasourceCollectionChangeListener = null;
        }

        @Override
        public int size() {
            return datasource.size();
        }

        public CollectionDatasource getDatasource() {
            return datasource;
        }
    }
}
