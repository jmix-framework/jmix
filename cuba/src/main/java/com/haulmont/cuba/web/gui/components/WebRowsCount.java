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

import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.RowsCount;
import com.haulmont.cuba.gui.components.data.meta.DatasourceDataUnit;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.WeakCollectionChangeListener;
import com.vaadin.shared.Registration;
import io.jmix.core.*;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.VisibilityChangeNotifier;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.impl.AbstractComponent;
import io.jmix.ui.component.impl.AbstractTable;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundTaskHandler;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.*;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.widget.JmixRowsCount;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@SuppressWarnings("rawtypes")
@Deprecated
public class WebRowsCount extends AbstractComponent<JmixRowsCount> implements RowsCount, VisibilityChangeNotifier,
        InitializingBean {

    protected static final String TABLE_ROWS_COUNT_STYLENAME = "jmix-table-rows-count";
    protected static final String PAGING_COUNT_NUMBER_STYLENAME = "jmix-paging-count-number";

    private static final Logger log = LoggerFactory.getLogger(WebRowsCount.class);

    protected Messages messages;
    protected BackgroundWorker backgroundWorker;
    protected Icons icons;
    protected IconResolver iconResolver;
    protected UserSessionSource userSessionSource;

    protected WebRowsCount.Adapter adapter;

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    protected boolean refreshing;
    protected State state;
    protected State lastState;
    protected int start;
    protected int size;
    protected boolean samePage;

    protected boolean autoLoad;
    protected BackgroundTaskHandler<Integer> rowsCountTaskHandler;

    protected RowsCountTarget target;

    protected Registration onLinkClickRegistration;
    protected Registration onPrevClickRegistration;
    protected Registration onNextClickRegistration;
    protected Registration onFirstClickRegistration;
    protected Registration onLastClickRegistration;
    protected Function<DataLoadContext, Long> totalCountDelegate;
    protected Datatype countDatatype;

    public WebRowsCount() {
        component = new JmixRowsCount();
        component.setStyleName(TABLE_ROWS_COUNT_STYLENAME);

        //hide all buttons. They will become visible after data is loaded
        component.getCountButton().setVisible(false);
        component.getPrevButton().setVisible(false);
        component.getNextButton().setVisible(false);
        component.getFirstButton().setVisible(false);
        component.getLastButton().setVisible(false);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setIconResolver(IconResolver iconResolver) {
        this.iconResolver = iconResolver;
    }

    @Autowired
    public void setIcons(Icons icons) {
        this.icons = icons;
    }

    @Autowired
    public void setBackgroundWorker(BackgroundWorker backgroundWorker) {
        this.backgroundWorker = backgroundWorker;
    }

    @Autowired
    public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        countDatatype = datatypeRegistry.get(Integer.class);
    }

    @Autowired
    public void setUserSessionSource(UserSessionSource userSessionSource) {
        this.userSessionSource = userSessionSource;
    }

    @Override
    public void afterPropertiesSet() {
        component.getFirstButton().setIcon(iconResolver.getIconResource(icons.get(JmixIcon.ANGLE_DOUBLE_LEFT)));
        component.getPrevButton().setIcon(iconResolver.getIconResource(icons.get(JmixIcon.ANGLE_LEFT)));
        component.getNextButton().setIcon(iconResolver.getIconResource(icons.get(JmixIcon.ANGLE_RIGHT)));
        component.getLastButton().setIcon(iconResolver.getIconResource(icons.get(JmixIcon.ANGLE_DOUBLE_RIGHT)));
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(TABLE_ROWS_COUNT_STYLENAME, ""));
    }

    protected void initButtonListeners() {
        unregisterListeners();
        onLinkClickRegistration = component.getCountButton().addClickListener(event -> onLinkClick());
        onPrevClickRegistration = component.getPrevButton().addClickListener(event -> onPrevClick());
        onNextClickRegistration = component.getNextButton().addClickListener(event -> onNextClick());
        onFirstClickRegistration = component.getFirstButton().addClickListener(event -> onFirstClick());
        onLastClickRegistration = component.getLastButton().addClickListener(event -> onLastClick());
    }

    protected void unregisterListeners() {
        if (onLinkClickRegistration != null)
            onLinkClickRegistration.remove();

        if (onPrevClickRegistration != null)
            onPrevClickRegistration.remove();

        if (onNextClickRegistration != null)
            onNextClickRegistration.remove();

        if (onFirstClickRegistration != null)
            onFirstClickRegistration.remove();

        if (onLastClickRegistration != null)
            onLastClickRegistration.remove();
    }

    @Nullable
    @Override
    public ListComponent getOwner() {
        if (target instanceof ListComponent) {
            return (ListComponent) target;
        }
        return null;
    }

    @Override
    public void setOwner(@Nullable ListComponent owner) {
        if (owner instanceof RowsCountTarget) {
            this.target = (RowsCountTarget) owner;
        }
    }

    @Override
    public RowsCount.RowsCountTarget getRowsCountTarget() {
        return target;
    }

    protected WebRowsCount.Adapter createAdapter(RowsCount.RowsCountTarget target) {
        if (target instanceof ListComponent) {
            DataUnit items = ((ListComponent) target).getItems();
            if (items instanceof DatasourceDataUnit) {
                return createDatasourceAdapter(((DatasourceDataUnit) items).getDatasource());
            } else if (items instanceof ContainerDataUnit) {
                return createLoaderAdapter(((ContainerDataUnit<?>) items).getContainer());
            }
            throw new IllegalStateException("Unsupported data unit type: " + items);
        }

        throw new UnsupportedOperationException("Unsupported RowsCountTarget: " + target);
    }

    @Override
    public void setRowsCountTarget(RowsCount.RowsCountTarget target) {
        checkNotNullArgument(target, "target is null");

        if (!(target instanceof ListComponent)) {
            throw new UnsupportedOperationException("Unsupported RowsCountTarget: " + target);
        }

        this.target = target;

        if (((ListComponent) target).getItems() != null) {
            if (adapter != null) {
                adapter.unbind();
            }
            adapter = createAdapter(target);
        }

        initButtonListeners();
    }

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

    protected Adapter createLoaderAdapter(CollectionContainer container) {
        DataLoader loader = null;
        if (container instanceof HasLoader) {
            loader = ((HasLoader) container).getLoader();
        }
        if (loader != null && !(loader instanceof BaseCollectionLoader)) {
            throw new IllegalStateException("RowsCount component currently supports only BaseCollectionLoader");
        }
        return new LoaderAdapter(container, (BaseCollectionLoader) loader);
    }

    @Override
    public Function<DataLoadContext, Long> getTotalCountDelegate() {
        return totalCountDelegate;
    }

    @Override
    public void setTotalCountDelegate(Function<DataLoadContext, Long> countDelegate) {
        this.totalCountDelegate = countDelegate;
    }

    @Override
    public void addBeforeRefreshListener(Consumer<RowsCount.BeforeRefreshEvent> listener) {
        getEventHub().subscribe(RowsCount.BeforeRefreshEvent.class, listener);
    }

    @Override
    public void removeBeforeRefreshListener(Consumer<RowsCount.BeforeRefreshEvent> listener) {
        unsubscribe(RowsCount.BeforeRefreshEvent.class, listener);
    }

    protected void onPrevClick() {
        int firstResult = adapter.getFirstResult();
        int newStart = adapter.getFirstResult() - adapter.getMaxResults();
        adapter.setFirstResult(newStart < 0 ? 0 : newStart);
        if (refreshData()) {
            if (target instanceof AbstractTable) {
                resetCurrentDataPage((Table) target);
            }
        } else {
            adapter.setFirstResult(firstResult);
        }
    }

    protected void onNextClick() {
        int firstResult = adapter.getFirstResult();
        adapter.setFirstResult(adapter.getFirstResult() + adapter.getMaxResults());
        if (refreshData()) {
            if (state == RowsCount.State.LAST && size == 0) {
                adapter.setFirstResult(firstResult);
                int maxResults = adapter.getMaxResults();
                adapter.setMaxResults(maxResults + 1);
                refreshData();
                adapter.setMaxResults(maxResults);
            }
            if (target instanceof AbstractTable) {
                resetCurrentDataPage((Table) target);
            }
        } else {
            adapter.setFirstResult(firstResult);
        }
    }

    protected void onFirstClick() {
        int firstResult = adapter.getFirstResult();
        adapter.setFirstResult(0);
        if (refreshData()) {
            if (target instanceof AbstractTable) {
                resetCurrentDataPage((Table) target);
            }
        } else {
            adapter.setFirstResult(firstResult);
        }
    }

    protected void onLastClick() {
        int count = adapter.getCount();
        int itemsToDisplay = count % adapter.getMaxResults();
        if (itemsToDisplay == 0) itemsToDisplay = adapter.getMaxResults();

        int firstResult = adapter.getFirstResult();
        adapter.setFirstResult(count - itemsToDisplay);
        if (refreshData()) {
            if (target instanceof AbstractTable) {
                resetCurrentDataPage((Table) target);
            }
        } else {
            adapter.setFirstResult(firstResult);
        }
    }

    @SuppressWarnings("deprecation")
    protected void resetCurrentDataPage(Table table) {
        table.withUnwrapped(com.vaadin.v7.ui.Table.class, vTable ->
                vTable.setCurrentPageFirstItemIndex(0));
    }

    protected boolean refreshData() {
        if (hasSubscriptions(RowsCount.BeforeRefreshEvent.class)) {
            RowsCount.BeforeRefreshEvent event = new RowsCount.BeforeRefreshEvent(this);

            publish(RowsCount.BeforeRefreshEvent.class, event);

            if (event.isRefreshPrevented()) {
                return false;
            }
        }

        refreshing = true;
        try {
            adapter.refresh();
        } finally {
            refreshing = false;
        }

        return true;
    }

    protected void onLinkClick() {
        showRowsCountValue(adapter.getCount());
    }

    protected void showRowsCountValue(int count) {
        component.getCountButton().setCaption(countDatatype.format(count, userSessionSource.getLocale()));
        component.getCountButton().addStyleName(PAGING_COUNT_NUMBER_STYLENAME);
        component.getCountButton().setEnabled(false);
    }

    protected void onCollectionChanged() {
        if (adapter == null) {
            return;
        }

        String msgKey;
        size = adapter.size();
        start = 0;

        boolean refreshSizeButton = false;
        if (samePage) {
            state = lastState == null ? RowsCount.State.FIRST_COMPLETE : lastState;
            start = adapter.getFirstResult();
            samePage = false;
            refreshSizeButton = RowsCount.State.LAST.equals(state);
        } else if ((size == 0 || size < adapter.getMaxResults()) && adapter.getFirstResult() == 0) {
            state = RowsCount.State.FIRST_COMPLETE;
            lastState = state;
        } else if (size == adapter.getMaxResults() && adapter.getFirstResult() == 0) {
            state = RowsCount.State.FIRST_INCOMPLETE;
            lastState = state;
        } else if (size == adapter.getMaxResults() && adapter.getFirstResult() > 0) {
            state = RowsCount.State.MIDDLE;
            start = adapter.getFirstResult();
            lastState = state;
        } else if (size < adapter.getMaxResults() && adapter.getFirstResult() > 0) {
            state = RowsCount.State.LAST;
            start = adapter.getFirstResult();
            lastState = state;
        } else {
            state = RowsCount.State.FIRST_COMPLETE;
            lastState = state;
        }

        String countValue;
        switch (state) {
            case FIRST_COMPLETE:
                component.getCountButton().setVisible(false);
                component.getPrevButton().setVisible(false);
                component.getNextButton().setVisible(false);
                component.getFirstButton().setVisible(false);
                component.getLastButton().setVisible(false);
                if (size == 1) {
                    msgKey = "table.rowsCount.msg2Singular1";
                } else if (size % 100 > 10 && size % 100 < 20) {
                    msgKey = "table.rowsCount.msg2Plural1";
                } else {
                    switch (size % 10) {
                        case 1:
                            msgKey = "table.rowsCount.msg2Singular";
                            break;
                        case 2:
                        case 3:
                        case 4:
                            msgKey = "table.rowsCount.msg2Plural2";
                            break;
                        default:
                            msgKey = "table.rowsCount.msg2Plural1";
                    }
                }
                countValue = String.valueOf(size);
                break;
            case FIRST_INCOMPLETE:
                component.getCountButton().setVisible(true);
                component.getPrevButton().setVisible(false);
                component.getNextButton().setVisible(true);
                component.getFirstButton().setVisible(false);
                component.getLastButton().setVisible(true);
                msgKey = "table.rowsCount.msg1";
                countValue = countValue(start, size);
                break;
            case MIDDLE:
                component.getCountButton().setVisible(true);
                component.getPrevButton().setVisible(true);
                component.getNextButton().setVisible(true);
                component.getFirstButton().setVisible(true);
                component.getLastButton().setVisible(true);
                msgKey = "table.rowsCount.msg1";
                countValue = countValue(start, size);
                break;
            case LAST:
                component.getCountButton().setVisible(false);
                component.getPrevButton().setVisible(true);
                component.getNextButton().setVisible(false);
                component.getFirstButton().setVisible(true);
                component.getLastButton().setVisible(false);
                msgKey = "table.rowsCount.msg2Plural2";
                countValue = countValue(start, size);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        component.getLabel().setValue(messages.formatMessage(msgKey, countValue));

        if (component.getCountButton().isVisible() && !refreshing || refreshSizeButton) {
            if (autoLoad) {
                loadRowsCount();
            } else {
                component.getCountButton().setCaption(messages.getMessage("table.rowsCount.msg3"));
                component.getCountButton().removeStyleName(PAGING_COUNT_NUMBER_STYLENAME);
                component.getCountButton().setEnabled(true);
            }
        }
    }

    protected String countValue(int start, int size) {
        if (size == 0) {
            return String.valueOf(size);
        } else {
            return (start + 1) + "-" + (start + size);
        }
    }

    protected void loadRowsCount() {
        if (rowsCountTaskHandler != null
                && rowsCountTaskHandler.isAlive()) {
            log.debug("Cancel previous rows count task");
            rowsCountTaskHandler.cancel();
            rowsCountTaskHandler = null;
        }
        rowsCountTaskHandler = backgroundWorker.handle(getLoadCountTask());
        rowsCountTaskHandler.execute();
    }

    protected BackgroundTask<Long, Integer> getLoadCountTask() {
        Screen screen = UiControllerUtils.getScreen(target.getFrame().getFrameOwner());
        return new BackgroundTask<Long, Integer>(30, screen) {

            @Override
            public Integer run(TaskLifeCycle<Long> taskLifeCycle) {
                return adapter.getCount();
            }

            @Override
            public void done(Integer result) {
                showRowsCountValue(result);
            }

            @Override
            public void canceled() {
                log.debug("Loading rows count for screen '{}' is canceled", screen);
            }

            @Override
            public boolean handleTimeoutException() {
                log.warn("Time out while loading rows count for screen '{}'", screen);
                return true;
            }
        };
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        publish(VisibilityChangeNotifier.VisibilityChangeEvent.class,
                new VisibilityChangeNotifier.VisibilityChangeEvent(this, visible));
    }

    @Override
    public Subscription addVisibilityChangeListener(Consumer<VisibilityChangeNotifier.VisibilityChangeEvent> listener) {
        return getEventHub().subscribe(VisibilityChangeNotifier.VisibilityChangeEvent.class, listener);
    }

    @Override
    public boolean getAutoLoad() {
        return autoLoad;
    }

    @Override
    public void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
    }

    public interface Adapter {
        void unbind();

        int getFirstResult();

        int getMaxResults();

        void setFirstResult(int startPosition);

        void setMaxResults(int maxResults);

        int getCount();

        int size();

        void refresh();
    }

    protected class LoaderAdapter implements Adapter {

        protected CollectionContainer container;

        protected Consumer<CollectionContainer.CollectionChangeEvent> containerCollectionChangeListener;
        protected io.jmix.ui.model.impl.WeakCollectionChangeListener weakContainerCollectionChangeListener;

        @Nullable
        protected BaseCollectionLoader loader;

        @SuppressWarnings("unchecked")
        public LoaderAdapter(CollectionContainer container, @Nullable BaseCollectionLoader loader) {
            this.container = container;
            this.loader = loader;

            containerCollectionChangeListener = e -> {
                samePage = CollectionChangeType.REFRESH != e.getChangeType();
                onCollectionChanged();
            };

            weakContainerCollectionChangeListener = new io.jmix.ui.model.impl.WeakCollectionChangeListener(
                    container, containerCollectionChangeListener);

            onCollectionChanged();
        }

        @Override
        public void unbind() {
            weakContainerCollectionChangeListener.removeItself();
        }

        @Override
        public int getFirstResult() {
            return loader != null ? loader.getFirstResult() : 0;
        }

        @Override
        public int getMaxResults() {
            return loader != null ? loader.getMaxResults() : Integer.MAX_VALUE;
        }

        @Override
        public void setFirstResult(int startPosition) {
            if (loader != null)
                loader.setFirstResult(startPosition);
        }

        @Override
        public void setMaxResults(int maxResults) {
            if (loader != null)
                loader.setMaxResults(maxResults);
        }

        @SuppressWarnings("unchecked")
        @Override
        public int getCount() {
            if (loader == null) {
                return container.getItems().size();
            }

            if (loader instanceof CollectionLoader) {
                LoadContext context = ((CollectionLoader) loader).createLoadContext();
                if (totalCountDelegate == null) {
                    return (int) dataManager.getCount(context);
                } else {
                    return Math.toIntExact(totalCountDelegate.apply(context));
                }
            } else if (loader instanceof KeyValueCollectionLoader) {
                ValueLoadContext context = ((KeyValueCollectionLoader) loader).createLoadContext();
                if (totalCountDelegate == null) {
                    return (int) dataManager.getCount(context);
                } else {
                    return Math.toIntExact(totalCountDelegate.apply(context));
                }
            } else {
                log.warn("Unsupported loader type: {}", loader.getClass().getName());
                return 0;
            }
        }

        @Override
        public int size() {
            return container.getItems().size();
        }

        @Override
        public void refresh() {
            if (loader != null)
                loader.load();
        }
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
