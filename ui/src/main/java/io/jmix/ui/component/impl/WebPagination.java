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

import com.vaadin.shared.Registration;
import io.jmix.core.*;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.*;
import io.jmix.ui.component.pagination.PaginationDelegate;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundTaskHandler;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.*;
import io.jmix.ui.model.impl.WeakCollectionChangeListener;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import io.jmix.ui.widget.JmixPagination;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class WebPagination extends WebAbstractComponent<JmixPagination> implements Pagination, InitializingBean {

    protected static final String PAGINATION_STYLENAME = "c-pagination";
    protected static final String PAGINATION_COUNT_NUMBER_STYLENAME = "c-pagination-count-number";

    private static final Logger log = LoggerFactory.getLogger(WebPagination.class);

    protected Messages messages;
    protected DataManager dataManager;
    protected BackgroundWorker backgroundWorker;
    protected IconResolver iconResolver;
    protected UiProperties uiProperties;
    protected ThemeConstantsManager themeConstantsManager;
    protected PaginationDelegate delegate;

    protected WebPagination.Adapter adapter;
    protected BaseCollectionLoader loader;

    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    protected boolean refreshing;
    protected Pagination.State state;
    protected Pagination.State lastState;
    protected int start;
    protected int size;
    protected int count = -1; // temporal, is set only if last button is clicked then value is reset
    protected boolean samePage;

    protected List<Integer> maxResultOptions;
    protected List<Integer> options = Collections.emptyList();

    protected boolean autoLoad;
    protected BackgroundTaskHandler<Integer> rowsCountTaskHandler;

    protected Registration onLinkClickRegistration;
    protected Registration onPrevClickRegistration;
    protected Registration onNextClickRegistration;
    protected Registration onFirstClickRegistration;
    protected Registration onLastClickRegistration;
    protected Registration maxResultsValueChangeRegistration;
    protected Function<DataLoadContext, Long> totalCountDelegate;

    public WebPagination() {
        component = createComponent();
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
    public void setBackgroundWorker(BackgroundWorker backgroundWorker) {
        this.backgroundWorker = backgroundWorker;
    }

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Autowired
    public void setUiProperties(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @Autowired
    public void setThemeConstantsManager(ThemeConstantsManager themeConstantsManager) {
        this.themeConstantsManager = themeConstantsManager;
    }

    @Autowired
    public void setDelegate(PaginationDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initComponent();
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(PAGINATION_STYLENAME, ""));
    }

    @Override
    public void setLoaderTarget(BaseCollectionLoader loader) {
        checkNotNullArgument(loader);

        this.loader = loader;

        if (adapter != null) {
            adapter.unbind();
        }

        adapter = createAdapter(loader);

        unregisterListeners();

        initMaxResultOptions();
        initMaxResultValue();

        initListeners();

        updateComponentAvailability();
    }

    @Nullable
    @Override
    public BaseCollectionLoader getLoaderTarget() {
        return loader;
    }

    @Override
    public boolean getAutoLoad() {
        return autoLoad;
    }

    @Override
    public void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
    }

    @Nullable
    @Override
    public Function<DataLoadContext, Long> getTotalCountDelegate() {
        return totalCountDelegate;
    }

    @Override
    public void setTotalCountDelegate(@Nullable Function<DataLoadContext, Long> countDelegate) {
        this.totalCountDelegate = countDelegate;
    }

    @Override
    public Subscription addBeforeRefreshListener(Consumer<BeforeRefreshEvent> listener) {
        return getEventHub().subscribe(BeforeRefreshEvent.class, listener);
    }

    @Override
    public void setContentAlignment(ContentAlignment position) {
        component.setContentAlignment(position);
    }

    @Override
    public ContentAlignment getContentAlignment() {
        return component.getContentAlignment();
    }

    protected JmixPagination createComponent() {
        return new JmixPagination();
    }

    @Override
    public boolean isShowMaxResults() {
        return component.getMaxResultLayout().isVisible();
    }

    @Override
    public void setShowMaxResults(boolean showMaxResults) {
        component.getMaxResultLayout().setVisible(showMaxResults);

        refreshMaxResultValue();
    }

    @Override
    public boolean isShowNullMaxResult() {
        return component.getMaxResultComboBox().isEmptySelectionAllowed();
    }

    @Override
    public void setShowNullMaxResult(boolean showNullMaxResult) {
        component.getMaxResultComboBox().setEmptySelectionAllowed(showNullMaxResult);
    }

    @Override
    public List<Integer> getMaxResultOptions() {
        if (maxResultOptions == null) {
            return Collections.emptyList();
        }
        return maxResultOptions;
    }

    @Override
    public void setMaxResultOptions(List<Integer> maxResults) {
        this.maxResultOptions = maxResults;
    }

    protected void initComponent() {
        component.setStyleName(PAGINATION_STYLENAME);

        // hide all buttons. They will become visible after data is loaded
        component.getCountButton().setVisible(false);
        component.getPrevButton().setVisible(false);
        component.getNextButton().setVisible(false);
        component.getFirstButton().setVisible(false);
        component.getLastButton().setVisible(false);

        component.getFirstButton().setIcon(iconResolver.getIconResource(JmixIcon.ANGLE_DOUBLE_LEFT.source()));
        component.getPrevButton().setIcon(iconResolver.getIconResource(JmixIcon.ANGLE_LEFT.source()));
        component.getNextButton().setIcon(iconResolver.getIconResource(JmixIcon.ANGLE_RIGHT.source()));
        component.getLastButton().setIcon(iconResolver.getIconResource(JmixIcon.ANGLE_DOUBLE_RIGHT.source()));

        component.getMaxResultLabel().setValue(messages.getMessage("", "pagination.maxResult.label.value"));

        ThemeConstants theme = themeConstantsManager.getConstants();
        component.getMaxResultComboBox().setWidth(theme.get("jmix.ui.pagination.maxResult.width"));
        component.getMaxResultComboBox().setEmptySelectionAllowed(true);
        component.getMaxResultComboBox().setItems(delegate.getMaxResultsFromProperty());

        updateComponentAvailability();
    }

    protected void initListeners() {
        unregisterListeners();
        onLinkClickRegistration = component.getCountButton().addClickListener(event -> onLinkClick());
        onPrevClickRegistration = component.getPrevButton().addClickListener(event -> onPrevClick());
        onNextClickRegistration = component.getNextButton().addClickListener(event -> onNextClick());
        onFirstClickRegistration = component.getFirstButton().addClickListener(event -> onFirstClick());
        onLastClickRegistration = component.getLastButton().addClickListener(event -> onLastClick());
        maxResultsValueChangeRegistration = component.getMaxResultComboBox()
                .addValueChangeListener(event -> onMaxResultsValueChange(event.getValue()));
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

        if (maxResultsValueChangeRegistration != null) {
            maxResultsValueChangeRegistration.remove();
        }
    }

    protected void updateComponentAvailability() {
        boolean disabled = adapter == null;

        getComponent().getMaxResultComboBox().setEnabled(!disabled);
        if (disabled) {
            getComponent().getLabel().setValue(
                    messages.getMessage("", "pagination.status.label.disabledValue"));
        }
    }

    protected Adapter createAdapter(BaseCollectionLoader loader) {
        return new LoaderAdapter(loader);
    }

    protected void onLinkClick() {
        showRowsCountValue(adapter.getCount());
    }

    protected void onPrevClick() {
        int firstResult = adapter.getFirstResult();
        int newStart = adapter.getFirstResult() - adapter.getMaxResults();
        adapter.setFirstResult(newStart < 0 ? 0 : newStart);

        if (refreshData()) {
            onSuccessfulDataRefresh();
        } else {
            adapter.setFirstResult(firstResult);
        }
    }

    protected void onNextClick() {
        int firstResult = adapter.getFirstResult();
        adapter.setFirstResult(adapter.getFirstResult() + adapter.getMaxResults());
        if (refreshData()) {
            if (state == Pagination.State.LAST && size == 0) {
                adapter.setFirstResult(firstResult);
                int maxResults = adapter.getMaxResults();
                adapter.setMaxResults(maxResults + 1);
                refreshData();
                adapter.setMaxResults(maxResults);
            }
            onSuccessfulDataRefresh();
        } else {
            adapter.setFirstResult(firstResult);
        }
    }

    protected void onFirstClick() {
        int firstResult = adapter.getFirstResult();
        adapter.setFirstResult(0);

        if (refreshData()) {
            onSuccessfulDataRefresh();
        } else {
            adapter.setFirstResult(firstResult);
        }
    }

    protected void onLastClick() {
        count = adapter.getCount();
        int itemsToDisplay = count % adapter.getMaxResults();
        if (itemsToDisplay == 0) itemsToDisplay = adapter.getMaxResults();

        int firstResult = adapter.getFirstResult();
        adapter.setFirstResult(count - itemsToDisplay);

        if (refreshData()) {
            onSuccessfulDataRefresh();
        } else {
            adapter.setFirstResult(firstResult);
        }
    }

    protected void onMaxResultsValueChange(@Nullable Integer value) {
        checkState();

        Integer maxResult = value;
        if (maxResult == null) {
            maxResult = delegate.getMaxFetchValue(adapter.getEntityMetaClass());
        }

        adapter.setMaxResults(maxResult);
        adapter.refresh();
    }

    protected void initMaxResultOptions() {
        checkState();

        if (CollectionUtils.isNotEmpty(maxResultOptions)) {
            options = delegate.filterOptions(maxResultOptions, adapter.getMaxResults(), adapter.getEntityMetaClass());
        } else {
            options = delegate.filterPropertyOptions(adapter.getMaxResults(), adapter.getEntityMetaClass());
        }

        component.getMaxResultComboBox().setItems(options);
    }

    protected void initMaxResultValue() {
        checkState();

        // use values from ComboBox
        if (isShowMaxResults()) {
            Integer maxResult = delegate.getAllowedOption(options, adapter.getMaxResults(), adapter.getEntityMetaClass());
            adapter.setMaxResults(maxResult);
            component.getMaxResultComboBox().setValue(maxResult);

            // if loader has items
            if (adapter.size() > 0) {
                onMaxResultsValueChange(maxResult); // reload it with value form ComboBox
            }
        } else {
            // otherwise from loader
            int maxResultLoaded = adapter.getLoadedMaxResults();
            int maxFetch = delegate.getMaxFetchValue(adapter.getEntityMetaClass());
            adapter.setMaxResults(Math.min(maxResultLoaded, maxFetch));

            // if loader has items
            if (adapter.size() > 0) {
                onCollectionChanged(); // update state
            }
        }
    }

    protected void refreshMaxResultValue() {
        if (adapter == null) {
            return;
        }

        if (isShowMaxResults()) {
            Integer maxResult = delegate.getAllowedOption(options, adapter.getMaxResults(), adapter.getEntityMetaClass());
            adapter.setMaxResults(maxResult);

            Integer oldValue = component.getMaxResultComboBox().getValue();
            component.getMaxResultComboBox().setValue(maxResult);

            // if value the same, fire event manually
            if (maxResult.equals(oldValue)) {
                onMaxResultsValueChange(maxResult); // reload it with value form ComboBox
            }
        } else {
            int maxResultLoaded = adapter.getLoadedMaxResults();
            int maxFetch = delegate.getMaxFetchValue(adapter.getEntityMetaClass());
            adapter.setMaxResults(Math.min(maxResultLoaded, maxFetch));
            adapter.refresh();
        }
    }

    protected boolean refreshData() {
        if (hasSubscriptions(Pagination.BeforeRefreshEvent.class)) {
            Pagination.BeforeRefreshEvent event = new Pagination.BeforeRefreshEvent(this);

            publish(Pagination.BeforeRefreshEvent.class, event);

            if (event.isRefreshPrevented()) {
                return false;
            }
        }

        refreshing = true;
        try {
            adapter.refresh();
        } finally {
            count = -1;
            refreshing = false;
        }

        return true;
    }

    // hook for TablePagination
    protected void onSuccessfulDataRefresh() {
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
            state = lastState == null ? Pagination.State.FIRST_COMPLETE : lastState;
            start = adapter.getFirstResult();
            samePage = false;
            refreshSizeButton = Pagination.State.LAST.equals(state);
        } else if ((size == 0 || size < adapter.getMaxResults()) && adapter.getFirstResult() == 0) {
            state = Pagination.State.FIRST_COMPLETE;
            lastState = state;
        } else if (size == adapter.getMaxResults() && adapter.getFirstResult() == 0) {
            state = Pagination.State.FIRST_INCOMPLETE;
            lastState = state;
        } else if (size == adapter.getMaxResults() && adapter.getFirstResult() > 0 && count == -1) {
            state = Pagination.State.MIDDLE;
            start = adapter.getFirstResult();
            lastState = state;
        } else if (size <= adapter.getMaxResults() && adapter.getFirstResult() > 0) {
            state = Pagination.State.LAST;
            start = adapter.getFirstResult();
            lastState = state;
        } else {
            state = Pagination.State.FIRST_COMPLETE;
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
                    msgKey = "pagination.msg2Singular1";
                } else if (size % 100 > 10 && size % 100 < 20) {
                    msgKey = "pagination.msg2Plural1";
                } else {
                    switch (size % 10) {
                        case 1:
                            msgKey = "pagination.msg2Singular";
                            break;
                        case 2:
                        case 3:
                        case 4:
                            msgKey = "pagination.msg2Plural2";
                            break;
                        default:
                            msgKey = "pagination.msg2Plural1";
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
                msgKey = "pagination.msg1";
                countValue = countValue(start, size);
                break;
            case MIDDLE:
                component.getCountButton().setVisible(true);
                component.getPrevButton().setVisible(true);
                component.getNextButton().setVisible(true);
                component.getFirstButton().setVisible(true);
                component.getLastButton().setVisible(true);
                msgKey = "pagination.msg1";
                countValue = countValue(start, size);
                break;
            case LAST:
                component.getCountButton().setVisible(false);
                component.getPrevButton().setVisible(true);
                component.getNextButton().setVisible(false);
                component.getFirstButton().setVisible(true);
                component.getLastButton().setVisible(false);
                msgKey = "pagination.msg2Plural2";
                countValue = countValue(start, size);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        component.getLabel().setValue(messages.formatMessage("", msgKey, countValue));

        // update visible total count
        if (component.getCountButton().isVisible() && !refreshing || refreshSizeButton) {
            if (autoLoad) {
                loadRowsCount();
            } else {
                component.getCountButton().setCaption(messages.getMessage("","pagination.msg3"));
                component.getCountButton().removeStyleName(PAGINATION_COUNT_NUMBER_STYLENAME);
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
        if (getFrame() == null) {
            throw new IllegalStateException("Pagination component is not attached to the Frame");
        }

        Screen screen = UiControllerUtils.getScreen(getFrame().getFrameOwner());
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

    protected void showRowsCountValue(int count) {
        component.getCountButton().setCaption(String.valueOf(count)); // todo rework with datatype
        component.getCountButton().addStyleName(PAGINATION_COUNT_NUMBER_STYLENAME);
        component.getCountButton().setEnabled(false);
    }

    protected void checkState() {
        if (adapter == null) {
            throw new IllegalStateException("Pagination component is not bound with DataLoader");
        }
    }

    public interface Adapter {
        void unbind();

        int getFirstResult();

        int getMaxResults();

        void setFirstResult(int startPosition);

        void setMaxResults(int maxResults);

        int getCount();

        MetaClass getEntityMetaClass();

        int size();

        void refresh();

        int getLoadedMaxResults();
    }

    @SuppressWarnings("rawtypes")
    protected class LoaderAdapter implements WebPagination.Adapter {

        protected CollectionContainer container;

        protected Consumer<CollectionContainer.CollectionChangeEvent> containerCollectionChangeListener;
        protected WeakCollectionChangeListener weakContainerCollectionChangeListener;

        protected BaseCollectionLoader loader;
        protected int loadedMaxResults = -1;

        public LoaderAdapter(BaseCollectionLoader loader) {
            this(loader.getContainer(), loader);
        }

        @SuppressWarnings("unchecked")
        public LoaderAdapter(CollectionContainer container, @Nullable BaseCollectionLoader loader) {
            this.loader = loader;
            this.container = container;

            if (loader != null) {
                loadedMaxResults = loader.getMaxResults();
            }

            containerCollectionChangeListener = e -> {
                samePage = CollectionChangeType.REFRESH != e.getChangeType();
                onCollectionChanged();
            };

            weakContainerCollectionChangeListener = new WeakCollectionChangeListener(
                    container, containerCollectionChangeListener);
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
                    QueryTransformer transformer = queryTransformerFactory.transformer(context.getQuery().getQueryString());
                    // TODO it doesn't work for query containing scalars in select
                    transformer.replaceWithCount();
                    context.getQuery().setQueryString(transformer.getResult());
                    context.setProperties(Collections.singletonList("cnt"));
                    List<KeyValueEntity> list = dataManager.loadValues(context);
                    Number count = list.get(0).getValue("cnt");
                    return count == null ? 0 : count.intValue();
                } else {
                    return Math.toIntExact(totalCountDelegate.apply(context));
                }
            } else {
                log.warn("Unsupported loader type: {}", loader.getClass().getName());
                return 0;
            }
        }

        @Override
        public MetaClass getEntityMetaClass() {
            return container.getEntityMetaClass();
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

        @Override
        public int getLoadedMaxResults() {
            return loadedMaxResults;
        }
    }
}
