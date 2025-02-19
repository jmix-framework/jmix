/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.EntityStates;
import io.jmix.core.Messages;
import io.jmix.core.UuidProvider;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.OpenedDialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.event.view.ViewOpenedEvent;
import io.jmix.flowui.sys.BeanUtil;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.tabbedmode.app.main.HasWorkArea;
import io.jmix.tabbedmode.component.breadcrumbs.ViewBreadcrumbs;
import io.jmix.tabbedmode.component.breadcrumbs.ViewBreadcrumbs.BreadcrumbsNavigationContext;
import io.jmix.tabbedmode.component.tabsheet.JmixViewTab;
import io.jmix.tabbedmode.component.tabsheet.MainTabSheetUtils;
import io.jmix.tabbedmode.component.viewcontainer.TabViewContainer;
import io.jmix.tabbedmode.component.viewcontainer.ViewContainer;
import io.jmix.tabbedmode.component.workarea.TabbedViewsContainer;
import io.jmix.tabbedmode.component.workarea.WorkArea;
import io.jmix.tabbedmode.view.DialogWindow;
import io.jmix.tabbedmode.view.ViewOpenMode;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.flowui.monitoring.UiMonitoring.stopViewTimerSample;
import static io.jmix.flowui.monitoring.ViewLifeCycle.BEFORE_SHOW;
import static io.jmix.flowui.monitoring.ViewLifeCycle.READY;
import static io.micrometer.core.instrument.Timer.start;

@org.springframework.stereotype.Component("tabmod_Views")
public class Views {

    private static final Logger log = LoggerFactory.getLogger(Views.class);

    // TODO: gg, rename?
    public static final CloseAction NAVIGATION_CLOSE_ACTION = new StandardCloseAction("navigation");

    protected final ApplicationContext applicationContext;
    protected final ViewRegistry viewRegistry;
    protected final UiComponents uiComponents;
    protected final Notifications notifications;
    protected final Messages messages;
    protected final RouteSupport routeSupport;
    protected final EntityStates entityStates;
    protected final UiProperties uiProperties;
    protected final MeterRegistry meterRegistry;
    protected final TabbedModeProperties tabbedModeProperties;

    public Views(ApplicationContext applicationContext,
                 ViewRegistry viewRegistry,
                 UiComponents uiComponents,
                 Notifications notifications,
                 Messages messages,
                 RouteSupport routeSupport,
                 EntityStates entityStates,
                 UiProperties uiProperties,
                 MeterRegistry meterRegistry,
                 TabbedModeProperties tabbedModeProperties) {
        this.applicationContext = applicationContext;
        this.viewRegistry = viewRegistry;
        this.uiComponents = uiComponents;
        this.notifications = notifications;
        this.messages = messages;
        this.routeSupport = routeSupport;
        this.entityStates = entityStates;
        this.uiProperties = uiProperties;
        this.meterRegistry = meterRegistry;
        this.tabbedModeProperties = tabbedModeProperties;
    }

    public View<?> create(String viewId) {
        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
        return createInternal(viewInfo.getControllerClass());
    }

    @SuppressWarnings("unchecked")
    public <T extends View<?>> T create(Class<T> viewClass) {
        String id = ViewDescriptorUtils.getInferredViewId(viewClass);
        return (T) create(id);
    }

    protected <T extends View<?>> T createInternal(Class<T> viewClass) {
        return Instantiator.get(UI.getCurrent()).getOrCreate(viewClass);
    }

    public OperationResult open(View<?> view, ViewOpenMode openMode) {
        return open(getCurrentUI(), view, openMode);
    }

    public OperationResult open(JmixUI ui, View<?> view, ViewOpenMode openMode) {
        checkNotNullArgument(view);
        checkNotNullArgument(openMode);
        checkNotYetOpened(view);

        if (isMaxTabCountExceeded(ui, openMode)) {
            showTooManyOpenTabsMessage();
            return OperationResult.fail();
        }

        Timer.Sample beforeShowSample = start(meterRegistry);
        fireViewBeforeShowEvent(view);
        stopViewTimerSample(beforeShowSample, meterRegistry, BEFORE_SHOW, view.getId().orElse(null));

        switch (openMode) {
            case ROOT:
                openRootView(ui, view);
                break;

            case THIS_TAB:
                openThisTab(ui, view);
                break;

            case NEW_TAB:
                openNewTab(ui, view);
                break;

            case DIALOG:
                openDialogWindow(ui, view);
                break;

            default:
                throw new UnsupportedOperationException("Unsupported OpenMode " + openMode);
        }

        log.trace("Screen {} {} opened", view.getId().orElse(null), view.getClass());

        // TODO: gg, single place
        // TODO: gg, get from breadcrumbs
        updateUrl(ui, resolveLocation(view));
        updatePageTitle(ui, view);
        // TODO: gg, fire QueryParametersChangeEvent?

        Timer.Sample readySample = start(meterRegistry);
        fireViewReadyEvent(view);
        stopViewTimerSample(readySample, meterRegistry, READY, view.getId().orElse(null));

        fireViewOpenedEvent(view);

        return OperationResult.success();
    }

    protected void checkNotYetOpened(View<?> view) {
        if (view.isAttached()) {
            throw new IllegalStateException("%s is already opened: '%s'"
                    .formatted(View.class.getSimpleName(), view.getId().orElse(null)));
        }
    }

    protected void updatePageTitle(JmixUI ui, View<?> view) {
        String title = ViewControllerUtils.getPageTitle(view);
        ui.getPage().setTitle(title);
    }

    protected void updateUrl(JmixUI ui, Location newLocation) {
        routeSupport.setLocation(ui, newLocation);
    }

    // TODO: gg, implement actual
    protected Location resolveLocation(View<?> view) {
        RouteParameters routeParameters = RouteParameters.empty();
        if (view instanceof DetailView<?> detailView) {
            // TODO: gg, implement actual
            String param = /*getRouteParamName();*/ "id";
            Object value = getParamValue(detailView);
            routeParameters = routeSupport.createRouteParameters(param, value);
        }

        String locationString = getRouteConfiguration().getUrl(view.getClass(), routeParameters);
        return new Location(locationString);
    }

    protected RouteConfiguration getRouteConfiguration() {
        return RouteConfiguration.forSessionScope();
    }

    protected Object getParamValue(DetailView<?> detailView) {
        Object editedEntity = detailView.getEditedEntity();
        return entityStates.isNew(editedEntity) ? "new" : Objects.requireNonNull(EntityValues.getId(editedEntity));
    }

    // TODO: gg, temporal, remove
    public OperationResult openFromNavigation(View<?> view, ViewOpenMode openMode) {
        return openFromNavigation(getCurrentUI(), view, openMode);
    }

    // TODO: gg, temporal, remove
    public OperationResult openFromNavigation(JmixUI ui, View<?> view, ViewOpenMode openMode) {
        // TODO: gg, temporal fix
        /*AppWorkArea workArea = getConfiguredWorkAreaOptional(ui)
                .orElseGet(() -> {
                    String mainViewId = uiProperties.getMainViewId();
                    View<?> mainView = create(mainViewId);
                    if (mainView instanceof HasWorkArea hasWorkArea) {
                        open(mainView, OpenMode.ROOT);
                        return hasWorkArea.getWorkArea();
                    } else {
                        throw new IllegalStateException("%s is not a %s"
                                .formatted(mainView.getClass().getSimpleName(),
                                        HasWorkArea.class.getSimpleName()));
                    }
                });*/


        if (openMode == ViewOpenMode.NEW_TAB) {
            if (isMaxTabCountExceeded(ui, openMode)) {
                showTooManyOpenTabsMessage();
                return OperationResult.fail();
            }

            // TODO: gg, check isMultipleOpen
            // TODO: gg, re-work?
            WorkArea workArea = getConfiguredWorkArea(ui);
            View<?> sameView = getTabbedViewsStacks(workArea)
                    .filter(viewStack -> viewStack.getBreadcrumbs().size() == 1) // never close non-top active screens
                    .map(viewStack -> viewStack.getBreadcrumbs().iterator().next())
                    .filter(tabScreen -> ViewControllerUtils.isSameView(view, tabScreen))
                    .findFirst()
                    .orElse(null);

            if (sameView != null) {
                OperationResult result = sameView.close(NAVIGATION_CLOSE_ACTION);
                if (result.getStatus() != OperationResult.Status.SUCCESS) {
                    // if unsaved changes dialog is shown, we can continue later
                    return result.compose(() -> openFromNavigation(ui, view, openMode));
                }
            }
        }

        return open(ui, view, openMode);
    }

    protected void openRootView(JmixUI ui, View<?> rootView) {
        ui.setTopLevelView(rootView);
    }

    protected void openDialogWindow(JmixUI ui, View<?> view) {
        DialogWindow<?> dialogWindow = createDialog(view);
        dialogWindow.open();
    }

    protected <V extends View<?>> DialogWindow<V> createDialog(V view) {
        DialogWindow<V> dialogWindow = new DialogWindow<>(view);
        BeanUtil.autowireContext(applicationContext, dialogWindow);

        return dialogWindow;
    }

    protected void openThisTab(JmixUI ui, View<?> view) {
        WorkArea workArea = getConfiguredWorkArea(ui);
        workArea.switchTo(WorkArea.State.VIEW_CONTAINER);

        TabbedViewsContainer<?> tabbedContainer = workArea.getTabbedViewsContainer();

        Tab selectedTab = tabbedContainer.getSelectedTab();
        if (selectedTab == null) {
            throw new IllegalStateException("No selected tab found");
        }

        Component tabComponent = tabbedContainer.getComponent(selectedTab);
        if (!(tabComponent instanceof ViewContainer viewContainer)
                || viewContainer.getBreadcrumbs() == null) {
            throw new IllegalStateException("%s not found"
                    .formatted(ViewBreadcrumbs.class.getSimpleName()));
        }

        viewContainer.setView(view);

        ViewBreadcrumbs breadcrumbs = viewContainer.getBreadcrumbs();
        breadcrumbs.addView(view, resolveLocation(view));

        ViewControllerUtils.setViewCloseDelegate(view, __ -> removeThisTabView(ui, view));

        if (selectedTab instanceof JmixViewTab viewTab) {
            viewTab.setText(ViewControllerUtils.getPageTitle(view));
            viewTab.setClosable(true/*view.isCloseable()*/); // TODO: gg, implement view.isCloseable()
        } else {
            selectedTab.setLabel(ViewControllerUtils.getPageTitle(view));
        }
    }

    protected void removeThisTabView(JmixUI ui, View<?> view) {
        ViewContainer viewContainer = getViewContainer(view);
        viewContainer.removeView();

        ViewBreadcrumbs breadcrumbs = viewContainer.getBreadcrumbs();
        if (breadcrumbs == null) {
            throw new IllegalStateException("%s not found"
                    .formatted(ViewBreadcrumbs.class.getSimpleName()));
        }

        breadcrumbs.removeView();

        ViewBreadcrumbs.ViewInfo currentViewInfo = breadcrumbs.getCurrentViewInfo();
        if (currentViewInfo == null) {
            throw new IllegalStateException("Current %s not found".formatted(View.class.getSimpleName()));
        }

        viewContainer.setView(currentViewInfo.view());

        WorkArea workArea = getConfiguredWorkArea(ui);
        TabbedViewsContainer<?> tabbedContainer = workArea.getTabbedViewsContainer();
        Tab tab = tabbedContainer.getTab(((Component) viewContainer));

        if (tab instanceof JmixViewTab viewTab) {
            viewTab.setText(ViewControllerUtils.getPageTitle(currentViewInfo.view()));
        } else {
            tab.setLabel(ViewControllerUtils.getPageTitle(currentViewInfo.view()));
        }

        // TODO: gg, move to a single place
        if (currentViewInfo.location() != null) {
            updateUrl(ui, currentViewInfo.location());
        }
        updatePageTitle(ui, currentViewInfo.view());
    }

    protected void openNewTab(JmixUI ui, View<?> view) {
        WorkArea workArea = getConfiguredWorkArea(ui);
        workArea.switchTo(WorkArea.State.VIEW_CONTAINER);

        // work with new view
        createNewTabLayout(ui, view);
        ViewControllerUtils.setViewCloseDelegate(view, __ -> removeNewTabView(ui, view));
    }

    protected void removeNewTabView(JmixUI ui, View<?> view) {
        ViewContainer viewContainer = getViewContainer(view);

        WorkArea workArea = getConfiguredWorkArea(ui);

        TabbedViewsContainer<?> tabbedContainer = workArea.getTabbedViewsContainer();
        tabbedContainer.remove(((Component) viewContainer));

        boolean allViewsRemoved = tabbedContainer.getTabs().isEmpty();

        ViewBreadcrumbs breadcrumbs = viewContainer.getBreadcrumbs();
        if (breadcrumbs != null) {
            breadcrumbs.setNavigationHandler(null);
            breadcrumbs.removeView();
        }

        if (allViewsRemoved) {
            workArea.switchTo(WorkArea.State.INITIAL_LAYOUT);

            // TODO: gg, move or re-implement, e.g. to state change listener?
            View<?> rootView = UiComponentUtils.getView(workArea);
            updatePageTitle(ui, rootView);
            updateUrl(ui, resolveLocation(rootView));
        }
    }

    protected ViewContainer getViewContainer(View<?> view) {
        return view.getParent()
                .filter(parent -> parent instanceof ViewContainer)
                .map(parent -> ((ViewContainer) parent))
                .orElseThrow(() -> new IllegalStateException("%s is not attached to a %s"
                        .formatted(View.class.getSimpleName(), ViewContainer.class.getSimpleName())));
    }

    protected void createNewTabLayout(JmixUI ui, View<?> view) {
        ViewBreadcrumbs breadcrumbs = createViewBreadCrumbs();
        breadcrumbs.setNavigationHandler(this::onBreadcrumbsNavigate);
        breadcrumbs.addView(view, resolveLocation(view));

        TabViewContainer viewContainer = uiComponents.create(TabViewContainer.class);
        viewContainer.setSizeFull();

        viewContainer.setBreadcrumbs(breadcrumbs);
        viewContainer.setView(view);

        WorkArea workArea = getConfiguredWorkArea(ui);

        TabbedViewsContainer<?> tabbedContainer = workArea.getTabbedViewsContainer();

        String tabId = "tab_" + UuidProvider.createUuid();

        JmixViewTab newTab = uiComponents.create(JmixViewTab.class);
        newTab.setId(tabId);
        newTab.setText(ViewControllerUtils.getPageTitle(view));
        // TODO: gg, implement
        newTab.setClosable(true /*view.isCloseable()*/);
        newTab.addBeforeCloseListener(this::handleViewTabClose);

        Tab addedTab = tabbedContainer.add(newTab, viewContainer);

        tabbedContainer.setSelectedTab(addedTab);
    }

    protected void onBreadcrumbsNavigate(BreadcrumbsNavigationContext context) {
        new BreadcrumbsNavigationTask(context).run();
    }

    protected void handleViewTabClose(JmixViewTab.BeforeCloseEvent<JmixViewTab> event) {
        JmixViewTab tab = event.getSource();
        UI ui = tab.getUI().orElse(null);
        if (!(ui instanceof JmixUI jmixUI)) {
            throw new IllegalStateException("%s is not attached to UI or UI is not a %s"
                    .formatted(tab.getClass().getSimpleName(), JmixUI.class.getSimpleName()));
        }

        WorkArea workArea = getConfiguredWorkArea(jmixUI);
        TabbedViewsContainer<?> tabbedContainer = workArea.getTabbedViewsContainer();

        ViewBreadcrumbs breadcrumbs = getViewBreadcrumbs(tabbedContainer, tab);
        createTabCloseTask(breadcrumbs).run();
    }

    protected TabCloseTask createTabCloseTask(ViewBreadcrumbs breadcrumbs) {
        return new TabCloseTask(breadcrumbs);
    }

    protected ViewBreadcrumbs getViewBreadcrumbs(TabbedViewsContainer<?> tabbedContainer, Tab tab) {
        Component tabComponent = tabbedContainer.getComponent(tab);
        if (!(tabComponent instanceof ViewContainer viewContainer)
                || viewContainer.getBreadcrumbs() == null) {
            throw new IllegalStateException("%s not found"
                    .formatted(ViewBreadcrumbs.class.getSimpleName()));
        }

        return viewContainer.getBreadcrumbs();
    }

    public String getLoginViewId() {
        return uiProperties.getLoginViewId();
    }

    public String getMainViewId() {
        return uiProperties.getMainViewId();
    }

    protected boolean isCloseable(View<?> view) {
        // TODO: gg, implement
        return true;
        /*if (!view.isCloseable()) {
            return true;
        }*/

        /*if (applicationContext.getBean(UiProperties.class).isDefaultScreenCanBeClosed()) {
            return false;
        }*/

//        return ((WindowImpl) view).isDefaultScreenWindow();

    }

    protected ViewBreadcrumbs createViewBreadCrumbs() {
        ViewBreadcrumbs breadcrumbs = uiComponents.create(ViewBreadcrumbs.class);
        breadcrumbs.setVisible(tabbedModeProperties.isShowBreadcrumbs());

        return breadcrumbs;
    }

    public Optional<WorkArea> findConfiguredWorkArea(JmixUI ui) {
        View<?> topLevelView = ui.getTopLevelView();
        if (topLevelView instanceof HasWorkArea hasWorkArea) {
            return Optional.ofNullable(hasWorkArea.getWorkArea());
        }

        return Optional.empty();
    }

    public WorkArea getConfiguredWorkArea(JmixUI ui) {
        return findConfiguredWorkArea(ui)
                .orElseThrow(() -> new IllegalStateException("Root %s does not have any configured work area"
                        .formatted(View.class.getSimpleName())));
    }

    protected Stream<ViewStack> getTabbedViewsStacks(WorkArea workArea) {
        return workArea.getTabbedViewsContainer().getTabComponentsStream()
                .map(MainTabSheetUtils::asViewContainer)
                .map(viewContainer ->
                        new ViewStack(workArea.getTabbedViewsContainer(), viewContainer));
    }

    public OpenedViews getOpenedViews() {
        return getOpenedViews(getCurrentUI());
    }

    public OpenedViews getOpenedViews(JmixUI ui) {
        OpenedDialogWindows openedDialogWindows = applicationContext.getBean(OpenedDialogWindows.class);
        return new OpenedViews(ui, openedDialogWindows);
    }

    public View<?> getCurrentView(JmixUI ui) {
        return findCurrentView(ui)
                .orElseThrow(() -> new IllegalStateException("No %s found"
                        .formatted(View.class.getSimpleName())));
    }

    public Optional<View<?>> findCurrentView(JmixUI ui) {
        OpenedViews openedViews = getOpenedViews(ui);
        Iterator<View<?>> dialogsIterator = openedViews.getDialogWindows().iterator();
        if (dialogsIterator.hasNext()) {
            return Optional.of(dialogsIterator.next());
        }

        Iterator<View<?>> viewsIterator = openedViews.getCurrentBreadcrumbs().iterator();
        if (viewsIterator.hasNext()) {
            return Optional.of(viewsIterator.next());
        }

        return openedViews.findRootView();
    }

    protected void fireViewBeforeShowEvent(View<?> view) {
        ViewControllerUtils.fireEvent(view, new View.BeforeShowEvent(view));
    }

    protected void fireViewReadyEvent(View<?> view) {
        ViewControllerUtils.fireEvent(view, new View.ReadyEvent(view));
    }

    protected void fireViewOpenedEvent(View<?> view) {
        ViewOpenedEvent viewOpenedEvent = new ViewOpenedEvent(view);
        applicationContext.publishEvent(viewOpenedEvent);
    }

    protected boolean isMaxTabCountExceeded(JmixUI ui, ViewOpenMode openMode) {
        if (openMode == ViewOpenMode.NEW_TAB) {
            WorkArea workArea = getConfiguredWorkArea(ui);

            int maxTabCount = tabbedModeProperties.getMaxTabCount();
            return maxTabCount > 0 && workArea.getOpenedTabCount() + 1 > maxTabCount;
        }

        return false;
    }

    protected void showTooManyOpenTabsMessage() {
        notifications.show(messages.formatMessage("", "tooManyOpenTabs.message",
                tabbedModeProperties.getMaxTabCount()));
    }

    protected JmixUI getCurrentUI() {
        UI ui = UI.getCurrent();
        if (!(ui instanceof JmixUI jmixUI)) {
            throw new IllegalStateException("UI is not a " + JmixUI.class.getSimpleName());
        }

        return jmixUI;
    }

    public class OpenedViews {

        protected final JmixUI ui;
        protected final OpenedDialogWindows openedDialogWindows;

        public OpenedViews(JmixUI ui,
                           OpenedDialogWindows openedDialogWindows) {
            this.ui = ui;
            this.openedDialogWindows = openedDialogWindows;
        }

        /**
         * @return the root view of UI
         * @throws IllegalStateException in case there is no root view in UI
         */
        public View<?> getRootView() {
            return ui.getTopLevelView();
        }

        /**
         * @return the root view of UI of present
         */
        public Optional<View<?>> findRootView() {
            return ui.getTopLevelViewOptional();
        }

        /**
         * @return all opened views excluding the root view or empty collection if there is no root view
         * or root view does not have {@link WorkArea}
         */
        public Collection<View<?>> getAll() {
            List<View<?>> views = new ArrayList<>();
            views.addAll(getOpenedWorkAreaViews());
            views.addAll(getDialogWindows());

            return views;
        }

        /**
         * @return all opened views excluding the root view and dialogs or empty collection
         * if there is no root view or root view does not have {@link WorkArea}
         */
        public Collection<View<?>> getOpenedWorkAreaViews() {
            return findConfiguredWorkArea(ui)
                    .map(WorkArea::getOpenedWorkAreaViews)
                    .orElse(Collections.emptyList());
        }

        /**
         * @return top views from work area tabs and all dialog windows or empty collection if there is no root view
         * or root view does not have {@link WorkArea}
         */
        public Collection<View<?>> getActiveViews() {
            List<View<?>> views = new ArrayList<>();
            views.addAll(getActiveWorkAreaViews());
            views.addAll(getDialogWindows());

            return views;
        }

        /**
         * @return top views from work area tabs or empty collection if there is no root view
         * or root view does not have {@link WorkArea}
         */
        public Collection<View<?>> getActiveWorkAreaViews() {
            return findConfiguredWorkArea(ui)
                    .map(WorkArea::getActiveWorkAreaViews)
                    .orElse(Collections.emptyList());
        }

        /**
         * @return all views opened in a dialog window
         */
        public Collection<View<?>> getDialogWindows() {
            return openedDialogWindows.getDialogs(ui);
        }

        /**
         * @return views of the currently opened tab of work area in descending order (first element is active view)
         * or empty collection if there is no root view or root view does not have {@link WorkArea}
         */
        public Collection<View<?>> getCurrentBreadcrumbs() {
            return findConfiguredWorkArea(ui)
                    .map(WorkArea::getCurrentBreadcrumbs)
                    .orElse(Collections.emptyList());
        }

        /**
         * @return tab containers with access to breadcrumbs or empty collection
         * if there is no root view or root view does not have {@link WorkArea}
         */
        public Collection<ViewStack> getWorkAreaViewStacks() {
            Optional<WorkArea> workArea = findConfiguredWorkArea(ui);
            if (workArea.isEmpty()) {
                return Collections.emptyList();
            }

            TabbedViewsContainer<?> tabbedContainer = workArea.get().getTabbedViewsContainer();
            return tabbedContainer.getTabComponentsStream()
                    .map(MainTabSheetUtils::asViewContainer)
                    .map(viewContainer ->
                            new ViewStack(tabbedContainer, viewContainer))
                    .toList();
        }
    }

    public static class ViewStack {

        protected final TabbedViewsContainer<?> tabbedContainer;
        protected final ViewContainer viewContainer;

        public ViewStack(TabbedViewsContainer<?> tabbedContainer, ViewContainer viewContainer) {
            this.tabbedContainer = tabbedContainer;
            this.viewContainer = viewContainer;
        }

        /**
         * @return screens of the container in descending order, first element is active screen
         * @throws IllegalStateException in case view stack has been closed
         */
        public Collection<View<?>> getBreadcrumbs() {
            checkAttached();

            ViewBreadcrumbs breadcrumbs = viewContainer.getBreadcrumbs();
            if (breadcrumbs == null) {
                return Collections.emptyList();
            }

            List<View<?>> views = new ArrayList<>(breadcrumbs.getViews().size());
            breadcrumbs.getViews().descendingIterator().forEachRemaining(views::add);

            return views;
        }

        public boolean isSelected() {
            checkAttached();

            Tab selectedTab = tabbedContainer.getSelectedTab();
            return selectedTab != null
                    && tabbedContainer.getComponent(selectedTab) == viewContainer;
        }

        /**
         * Select tab in tabbed UI.
         */
        public void select() {
            checkAttached();

            Tab tab = tabbedContainer.getTab(((Component) viewContainer));
            tabbedContainer.setSelectedTab(tab);
        }

        protected void checkAttached() {
            if (!((Component) viewContainer).isAttached()) {
                throw new IllegalStateException("%s has been detached"
                        .formatted(ViewStack.class.getSimpleName()));
            }
        }
    }

    protected static class BreadcrumbsNavigationTask implements Runnable {

        private final BreadcrumbsNavigationContext context;

        public BreadcrumbsNavigationTask(BreadcrumbsNavigationContext context) {
            this.context = context;
        }

        @Override
        public void run() {
            ViewBreadcrumbs.ViewInfo viewToClose = context.breadcrumbs().getCurrentViewInfo();
            if (viewToClose == null) {
                return;
            }

            // TODO: gg, implement
            /*if (!viewToClose.isCloseable()) {
                return;
            }*/

            if (context.view() != viewToClose.view()) {
                viewToClose.view().close(StandardOutcome.CLOSE)
                        .then(this);
            }
        }
    }

    protected class TabCloseTask implements Runnable {

        protected final ViewBreadcrumbs breadcrumbs;

        public TabCloseTask(ViewBreadcrumbs breadcrumbs) {
            this.breadcrumbs = breadcrumbs;
        }

        @Override
        public void run() {
            ViewBreadcrumbs.ViewInfo viewToClose = breadcrumbs.getCurrentViewInfo();
            if (viewToClose == null) {
                return;
            }

            if (isCloseable(viewToClose.view())) {
                viewToClose.view().close(StandardOutcome.CLOSE)
                        .then(this);
            }
        }
    }
}
