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

import com.vaadin.data.HasValue;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.SimplePagination;
import io.jmix.ui.component.VisibilityChangeNotifier;
import io.jmix.ui.component.pagination.data.PaginationDataBinder;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundTaskHandler;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.CollectionChangeType;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.widget.JmixSimplePagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

import static io.jmix.ui.widget.JmixSimplePagination.PAGINATION_COUNT_NUMBER_STYLENAME;

public class SimplePaginationImpl extends AbstractPagination<JmixSimplePagination> implements SimplePagination,
        VisibilityChangeNotifier, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(SimplePaginationImpl.class);

    protected BackgroundWorker backgroundWorker;
    protected CurrentAuthentication currentAuthentication;

    protected boolean autoLoad;

    protected Registration onLinkClickRegistration;
    protected Registration onPrevClickRegistration;
    protected Registration onNextClickRegistration;
    protected Registration onFirstClickRegistration;
    protected Registration onLastClickRegistration;

    protected Datatype countDatatype;
    protected boolean lastPage = false;
    protected boolean samePage;
    protected boolean refreshing;
    protected State state;
    protected State lastState;
    protected int start;
    protected int size;

    protected BackgroundTaskHandler<Integer> itemsCountTaskHandler;

    public SimplePaginationImpl() {
        component = createComponent();
    }

    protected JmixSimplePagination createComponent() {
        return new JmixSimplePagination();
    }

    @Autowired
    public void setBackgroundWorker(BackgroundWorker backgroundWorker) {
        this.backgroundWorker = backgroundWorker;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Autowired
    public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        countDatatype = datatypeRegistry.get(Integer.class);
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    @SuppressWarnings("ConstantConditions")
    protected void initComponent() {
        getFirstButton().setEnabled(false);
        getPrevButton().setEnabled(false);

        getCountButton().setEnabled(false);

        getNextButton().setEnabled(false);
        getLastButton().setEnabled(false);

        String iconName = getIconName(JmixIcon.ANGLE_DOUBLE_LEFT);
        getFirstButton().setIcon(getIconResource(iconName));

        iconName = getIconName(JmixIcon.ANGLE_LEFT);
        getPrevButton().setIcon(getIconResource(iconName));

        iconName = getIconName(JmixIcon.ANGLE_RIGHT);
        getNextButton().setIcon(getIconResource(iconName));

        iconName = getIconName(JmixIcon.ANGLE_DOUBLE_RIGHT);
        getLastButton().setIcon(getIconResource(iconName));

        initItemsPerPageLayout();

        updateItemsPerPageAvailability();
    }

    protected void updateItemsPerPageAvailability() {
        getItemsPerPageComboBox().setEnabled(!isEmptyOrNullDataBinder() && dataBinderContainsItems());
    }

    @Override
    public boolean isAutoLoad() {
        return autoLoad;
    }

    @Override
    public void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
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
    public void setDataBinder(PaginationDataBinder dataBinder) {
        super.setDataBinder(dataBinder);

        dataBinder.setCollectionChangeListener(this::onRefreshItems);

        removeListeners();

        initItemsPerPageOptions();
        initMaxResultValue();

        initListeners();

        if (dataBinderContainsItems()) {
            // if items has already loaded we should reload them
            // with maxResult from ComboBox or update shown items
            if (isItemsPerPageVisible()) {
                dataBinder.refresh();
            } else {
                onCollectionChanged();
            }
        }
    }

    protected void initListeners() {
        removeListeners();
        onFirstClickRegistration = getFirstButton().addClickListener(this::onFirstClick);
        onPrevClickRegistration = getPrevButton().addClickListener(this::onPrevClick);
        onLinkClickRegistration = getCountButton().addClickListener(this::onLinkClick);
        onNextClickRegistration = getNextButton().addClickListener(this::onNextClick);
        onLastClickRegistration = getLastButton().addClickListener(this::onLastClick);
        setupItemsPerPageValueChangeListener();
    }

    protected void removeListeners() {
        if (onFirstClickRegistration != null) {
            onFirstClickRegistration.remove();
            onFirstClickRegistration = null;
        }
        if (onPrevClickRegistration != null) {
            onPrevClickRegistration.remove();
            onPrevClickRegistration = null;
        }
        if (onLinkClickRegistration != null) {
            onLinkClickRegistration.remove();
            onLinkClickRegistration = null;
        }
        if (onNextClickRegistration != null) {
            onNextClickRegistration.remove();
            onNextClickRegistration = null;
        }
        if (onLastClickRegistration != null) {
            onLastClickRegistration.remove();
            onLastClickRegistration = null;
        }
        removeItemsPerPageValueChangeSubscription();
    }

    protected void onRefreshItems(CollectionChangeType changeType) {
        samePage = CollectionChangeType.REFRESH != changeType;
        onCollectionChanged();
    }

    protected void onFirstClick(Button.ClickEvent event) {
        int firstResult = dataBinder.getFirstResult();
        dataBinder.setFirstResult(0);

        if (refreshData()) {
            fireAfterRefreshEvent();
        } else {
            dataBinder.setFirstResult(firstResult);
        }
    }

    protected void onPrevClick(Button.ClickEvent event) {
        int firstResult = dataBinder.getFirstResult();
        int newStart = dataBinder.getFirstResult() - dataBinder.getMaxResults();
        dataBinder.setFirstResult(Math.max(newStart, 0));

        if (refreshData()) {
            fireAfterRefreshEvent();
        } else {
            dataBinder.setFirstResult(firstResult);
        }
    }

    protected void onNextClick(Button.ClickEvent event) {
        int firstResult = dataBinder.getFirstResult();
        dataBinder.setFirstResult(dataBinder.getFirstResult() + dataBinder.getMaxResults());
        if (refreshData()) {
            if (state == State.LAST && size == 0) {
                dataBinder.setFirstResult(firstResult);
                lastPage = true;
                refreshData();
                lastPage = false;
            }
            fireAfterRefreshEvent();
        } else {
            dataBinder.setFirstResult(firstResult);
        }
    }

    protected void onLastClick(Button.ClickEvent event) {
        int count = getTotalCount();
        int itemsToDisplay = count % dataBinder.getMaxResults();
        if (itemsToDisplay == 0) itemsToDisplay = dataBinder.getMaxResults();

        int firstResult = dataBinder.getFirstResult();
        dataBinder.setFirstResult(count - itemsToDisplay);

        lastPage = true;
        if (refreshData()) {
            fireAfterRefreshEvent();
        } else {
            dataBinder.setFirstResult(firstResult);
        }
        lastPage = false;
    }

    protected void onLinkClick(Button.ClickEvent event) {
        showItemsCountValue(getTotalCount());
    }

    protected void onItemsPerPageValueChange(HasValue.ValueChangeEvent<Integer> event) {
        checkDataBound();

        BeforeRefreshEvent refreshEvent = fireBeforeRefreshEvent();
        if (refreshEvent.isRefreshPrevented()) {
            setSilentlyItemsPerPageValue(event.getOldValue());
            return;
        }

        Integer maxResult = event.getValue();
        if (maxResult == null) {
            maxResult = getEntityMaxFetchSize(dataBinder.getEntityMetaClass());
        }

        dataBinder.setFirstResult(0);
        dataBinder.setMaxResults(maxResult);
        dataBinder.refresh();

        fireAfterRefreshEvent();
    }

    protected boolean refreshData() {
        BeforeRefreshEvent refreshEvent = fireBeforeRefreshEvent();
        if (refreshEvent.isRefreshPrevented()) {
            return false;
        }

        refreshing = true;
        try {
            dataBinder.refresh();
        } finally {
            refreshing = false;
        }

        return true;
    }

    protected void onCollectionChanged() {
        if (dataBinder == null) {
            return;
        }

        size = dataBinder.size();
        start = 0;

        boolean refreshSizeButton = false;
        if (samePage) {
            state = lastState == null ? State.FIRST_COMPLETE : lastState;
            start = dataBinder.getFirstResult();
            samePage = false;
            refreshSizeButton = State.LAST.equals(state);
        } else if ((size == 0 || size < dataBinder.getMaxResults()) && dataBinder.getFirstResult() == 0) {
            state = State.FIRST_COMPLETE;
            lastState = state;
        } else if (size == dataBinder.getMaxResults() && dataBinder.getFirstResult() == 0) {
            state = State.FIRST_INCOMPLETE;
            lastState = state;
        } else if (size == dataBinder.getMaxResults() && dataBinder.getFirstResult() > 0 && !lastPage) {
            state = State.MIDDLE;
            start = dataBinder.getFirstResult();
            lastState = state;
        } else if (size <= dataBinder.getMaxResults() && dataBinder.getFirstResult() > 0) {
            state = State.LAST;
            start = dataBinder.getFirstResult();
            lastState = state;
        } else {
            state = State.FIRST_COMPLETE;
            lastState = state;
        }

        updateNavigationButtonsAvailability();
        updateItemsPerPageAvailability();

        getLabel().setValue(messages.formatMessage("", getLabelMessageKey(), getLabelCountValue()));

        // update visible total count
        if (getCountButton().isVisible() && !refreshing || refreshSizeButton) {
            updateTotalCountButton();
        }

        if (dataBinder.getMaxResults() != getItemsCountToDisplay()) {
            if (isItemsPerPageVisible()) {
                Integer maxResult = canSetUnlimitedValue(dataBinder.getMaxResults()) ? null : dataBinder.getMaxResults();
                setSilentlyItemsPerPageValue(maxResult);
            }
        }
    }

    protected void updateTotalCountButton() {
        if (autoLoad) {
            loadItemsCount();
        } else {
            getCountButton().setCaption(messages.getMessage("pagination.msg3"));
            getCountButton().removeStyleName(PAGINATION_COUNT_NUMBER_STYLENAME);
            getCountButton().setEnabled(true);
        }
    }

    protected String getLabelMessageKey() {
        String msgKey;
        switch (state) {
            case FIRST_COMPLETE:
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
                break;
            case FIRST_INCOMPLETE:
            case MIDDLE:
                msgKey = "pagination.msg1";
                break;
            case LAST:
                msgKey = "pagination.msg2Plural2";
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return msgKey;
    }

    protected String getLabelCountValue() {
        switch (state) {
            case FIRST_COMPLETE:
                return String.valueOf(size);
            case FIRST_INCOMPLETE:
            case MIDDLE:
            case LAST:
                if (size == 0) {
                    return String.valueOf(size);
                } else {
                    return (start + 1) + "-" + (start + size);
                }
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected void updateNavigationButtonsAvailability() {
        switch (state) {
            case FIRST_COMPLETE:
                getFirstButton().setEnabled(false);
                getPrevButton().setEnabled(false);
                getCountButton().setVisible(false);
                getNextButton().setEnabled(false);
                getLastButton().setEnabled(false);
                break;
            case FIRST_INCOMPLETE:
                getFirstButton().setEnabled(false);
                getPrevButton().setEnabled(false);
                getCountButton().setVisible(true);
                getNextButton().setEnabled(true);
                getLastButton().setEnabled(true);
                break;
            case MIDDLE:
                getFirstButton().setEnabled(true);
                getPrevButton().setEnabled(true);
                getCountButton().setVisible(true);
                getNextButton().setEnabled(true);
                getLastButton().setEnabled(true);
                break;
            case LAST:
                getFirstButton().setEnabled(true);
                getPrevButton().setEnabled(true);
                getCountButton().setVisible(false);
                getNextButton().setEnabled(false);
                getLastButton().setEnabled(false);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    /*
     * Loads total count in the background thread if autoLoad is true.
     */
    protected void loadItemsCount() {
        if (itemsCountTaskHandler != null
                && itemsCountTaskHandler.isAlive()) {
            log.debug("Cancel previous items count task");
            itemsCountTaskHandler.cancel();
            itemsCountTaskHandler = null;
        }

        itemsCountTaskHandler = backgroundWorker.handle(getLoadCountTask());
        itemsCountTaskHandler.execute();
    }

    protected BackgroundTask<Long, Integer> getLoadCountTask() {
        if (getFrame() == null) {
            throw new IllegalStateException("SimplePagination component is not attached to the Frame");
        }

        Screen screen = UiControllerUtils.getScreen(getFrame().getFrameOwner());
        return new BackgroundTask<Long, Integer>(30, screen) {

            @Override
            public Integer run(TaskLifeCycle<Long> taskLifeCycle) {
                return dataBinder.getCount();
            }

            @Override
            public void done(Integer result) {
                showItemsCountValue(result);
            }

            @Override
            public void canceled() {
                log.debug("Loading items count for screen '{}' is canceled", screen);
            }

            @Override
            public boolean handleTimeoutException() {
                log.warn("Time out while loading items count for screen '{}'", screen);
                return true;
            }
        };
    }

    protected void showItemsCountValue(int count) {
        getCountButton().setCaption(countDatatype.format(count, currentAuthentication.getLocale()));
        getCountButton().addStyleName(PAGINATION_COUNT_NUMBER_STYLENAME);
        getCountButton().setEnabled(false);
    }

    protected Button getCountButton() {
        return component.getCountButton();
    }

    protected Label getLabel() {
        return component.getLabel();
    }
}
