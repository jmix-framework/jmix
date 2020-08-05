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

import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.TablePagination;
import io.jmix.ui.component.VisibilityChangeNotifier;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.data.meta.EmptyDataUnit;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.model.BaseCollectionLoader;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.HasLoader;

import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@SuppressWarnings("rawtypes")
public class WebTablePagination extends WebPagination implements TablePagination, VisibilityChangeNotifier {

    public static final String TABLE_PAGINATION_STYLENAME = "c-table-pagination";

    protected ListComponent target;

    @Override
    public ListComponent getTablePaginationTarget() {
        return target;
    }

    @Override
    public void setTablePaginationTarget(ListComponent target) {
        checkNotNullArgument(target, "target is null");

        this.target = target;

        if (target.getItems() != null) {
            if (adapter != null) {
                adapter.unbind();
            }
            adapter = createAdapter(target);

            unregisterListeners();
            
            initMaxResultOptions();
            initMaxResultValue();

            initListeners();

            updateComponentAvailability();
        }
    }

    @Override
    public Subscription addVisibilityChangeListener(Consumer<VisibilityChangeEvent> listener) {
        return getEventHub().subscribe(VisibilityChangeEvent.class, listener);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        publish(VisibilityChangeEvent.class,
                new VisibilityChangeEvent(this, visible));
    }

    @Override
    protected void initComponent() {
        super.initComponent();

        component.addStyleName(TABLE_PAGINATION_STYLENAME);
    }

    @Override
    protected void onSuccessfulDataRefresh() {
        super.onSuccessfulDataRefresh();

        if (target instanceof WebAbstractTable) {
            target.withUnwrapped(com.vaadin.v7.ui.Table.class, vTable ->
                    vTable.setCurrentPageFirstItemIndex(0));
        } else if (target instanceof WebAbstractDataGrid) {
            ((WebAbstractDataGrid) target).scrollToStart();
        }
    }

    @Override
    protected void initMaxResultValue() {
        if (isComponentDisabled()) {
            return;
        }

        super.initMaxResultValue();
    }

    protected void updateComponentAvailability() {
        boolean disabled = isComponentDisabled();

        getComponent().getMaxResultComboBox().setEnabled(!disabled);
        if (disabled) {
            getComponent().getLabel().setValue(
                    messages.getMessage("", "pagination.status.label.disabledValue"));
        }
    }

    protected boolean isComponentDisabled() {
        return adapter == null || adapter instanceof EmptyAdapter;
    }

    protected Adapter createAdapter(ListComponent target) {
        DataUnit items = target.getItems();

        if (items instanceof ContainerDataUnit) {
            CollectionContainer container = ((ContainerDataUnit) items).getContainer();

            DataLoader loader = null;
            if (container instanceof HasLoader) {
                loader = ((HasLoader) container).getLoader();

                if (loader != null && !(loader instanceof BaseCollectionLoader)) {
                    throw new IllegalStateException("TablePagination component currently supports only BaseCollectionLoader");
                }
            }

            return new LoaderAdapter(container, (BaseCollectionLoader) loader);
        }

        if (items instanceof EmptyDataUnit
                && items instanceof EntityDataUnit) {
            return new EmptyAdapter(((EntityDataUnit) items).getEntityMetaClass());
        }

        throw new IllegalStateException("Unsupported data unit type: " + items);
    }

    protected static class EmptyAdapter implements Adapter {

        protected MetaClass metaClass;

        public EmptyAdapter(MetaClass metaClass) {
            this.metaClass = metaClass;
        }

        @Override
        public void unbind() {
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
        }

        @Override
        public void setMaxResults(int maxResults) {
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public MetaClass getEntityMetaClass() {
            return metaClass;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void refresh() {
        }

        @Override
        public int getLoadedMaxResults() {
            return -1;
        }
    }
}
