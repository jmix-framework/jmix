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
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.event.view.ViewOpenedEvent;
import io.jmix.flowui.sys.BeanUtil;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.tabbedmode.component.breadcrumbs.ViewBreadcrumbs;
import io.jmix.tabbedmode.component.tabsheet.JmixMainTabSheet;
import io.jmix.tabbedmode.component.tabsheet.JmixViewTab;
import io.jmix.tabbedmode.component.viewcontainer.TabViewContainer;
import io.jmix.tabbedmode.component.workarea.AppWorkArea;
import io.jmix.tabbedmode.component.workarea.HasWorkArea;
import io.jmix.tabbedmode.view.DialogWindow;
import io.jmix.tabbedmode.view.ViewOpenMode;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.flowui.monitoring.UiMonitoring.stopViewTimerSample;
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

    // TODO: gg, OperationResult
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

    protected JmixUI getCurrentUI() {
        UI ui = UI.getCurrent();
        if (!(ui instanceof JmixUI jmixUI)) {
            throw new IllegalStateException("UI is not a " + JmixUI.class.getSimpleName());
        }

        return jmixUI;
    }

    public OperationResult open(JmixUI ui, View<?> view, ViewOpenMode openMode) {
        checkNotNullArgument(view);
        checkNotNullArgument(openMode);
        // TODO: gg, implement?
//        checkNotYetOpened(view);

        if (isMaxTabCountExceeded(ui, view, openMode)) {
            showTooManyOpenTabsMessage();
            return OperationResult.fail();
        }

//        Timer.Sample beforeShowSample = Timer.start(meterRegistry);

        // TODO: gg, implement ViewContext that stores OpenMode
//        ViewControllerUtils.setViewCloseDelegate(view, __ -> removeThisTabView(ui, view));

        fireViewBeforeShowEvent(view);

//        beforeShowSample.stop(createScreenTimer(meterRegistry, ScreenLifeCycle.BEFORE_SHOW, screen.getId()));

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

//        userActionsLog.trace("Screen {} {} opened", screen.getId(), screen.getClass());


        // TODO: gg, single place
        // TODO: gg, get from breadcrumbs
        updateUrl(ui, resolveLocation(view));
        updatePageTitle(ui, view);
        // TODO: gg, fire QueryParametersChangeEvent?

        Timer.Sample sample = start(meterRegistry);
        fireViewReadyEvent(view);
        stopViewTimerSample(sample, meterRegistry, READY, view.getId().orElse(null));

        fireViewOpenedEvent(view);

        return OperationResult.success();
    }

    protected void updatePageTitle(JmixUI ui, View<?> view) {
        String title = ViewControllerUtils.getPageTitle(view);
        ui.getPage().setTitle(title);
    }

    protected void updateUrl(JmixUI ui, Location newLocation) {
        // TODO: gg, implement
        /*WindowImpl windowImpl = (WindowImpl) screen.getWindow();
        Map<String, String> params = windowImpl.getResolvedState() != null
                ? windowImpl.getResolvedState().getParams()
                : Collections.emptyMap();

        ui.getUrlRouting().pushState(screen, params);*/

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

    private Object getParamValue(DetailView<?> detailView) {
        Object editedEntity = detailView.getEditedEntity();
        return entityStates.isNew(editedEntity) ? "new" : Objects.requireNonNull(EntityValues.getId(editedEntity));
    }

    private boolean hasRouteParams(View<?> view) {
        return view instanceof DetailView;
    }

    /*private String generateViewRoute(TabbedUI ui, View<?> view) {

        RouteConfiguration.forSessionScope().getRoute()
    }*/

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
            if (isMaxTabCountExceeded(ui, view, openMode)) {
                showTooManyOpenTabsMessage();
                return OperationResult.fail();
            }

            // TODO: gg, check isMultipleOpen
            // TODO: gg, re-work?
            AppWorkArea workArea = getConfiguredWorkArea(ui);
            View<?> sameView = getTabbedViewsStacks(workArea)
                    .filter(windowStack -> windowStack.getBreadcrumbs().size() == 1) // never close non-top active screens
                    .map(windowStack -> windowStack.getBreadcrumbs().iterator().next())
                    .filter(tabScreen -> isAlreadyOpened(view, tabScreen))
                    .findFirst()
                    .orElse(null);

            if (sameView != null) {
                OperationResult result = sameView.close(NAVIGATION_CLOSE_ACTION);
                if (result.getStatus() != OperationResult.Status.SUCCESS) {
                    // TODO: gg, test
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
        AppWorkArea workArea = getConfiguredWorkArea(ui);
        workArea.switchTo(AppWorkArea.State.VIEW_CONTAINER);

//        TabSheetBehaviour tabSheet = workArea.getTabbedWindowContainer().getTabSheetBehaviour();
        JmixMainTabSheet tabSheet = workArea.getTabbedWindowContainer();

        // TODO: gg, method?
        Tab selectedTab = tabSheet.getSelectedTab();
        TabViewContainer windowContainer = selectedTab != null
                ? (TabViewContainer) tabSheet.getContentByTab(selectedTab)
                : null;

        if (windowContainer == null || windowContainer.getBreadcrumbs() == null) {
            throw new IllegalStateException(ViewBreadcrumbs.class + " not found");
        }

        ViewBreadcrumbs breadcrumbs = windowContainer.getBreadcrumbs();
        // TODO: gg, remove after test
        // TODO: gg, exception?
        /*View<?> currentView = breadcrumbs.getCurrentViewInfo().view();

        windowContainer.remove(currentView);*/

        windowContainer.setView(view);
        breadcrumbs.addView(view, resolveLocation(view));

        ViewControllerUtils.setViewCloseDelegate(view, __ -> removeThisTabView(ui, view));

        if (selectedTab instanceof JmixViewTab viewTab) {
            viewTab.setText(ViewControllerUtils.getPageTitle(view));
            viewTab.setClosable(true); // TODO: gg, implement view.isCloseable()
        } else {
            // TODO: gg, exception or ignore?
            selectedTab.setLabel(ViewControllerUtils.getPageTitle(view));
        }

//        ContentSwitchMode contentSwitchMode = ContentSwitchMode.valueOf(tabWindow.getContentSwitchMode().name());
//        tabSheet.setContentSwitchMode(tabId, contentSwitchMode);
    }

    protected void removeThisTabView(JmixUI ui, View<?> view) {
        TabViewContainer windowContainer = getTabWindowContainer(view);
        windowContainer.removeView();

        ViewBreadcrumbs breadcrumbs = windowContainer.getBreadcrumbs();
        if (breadcrumbs == null) {
            throw new IllegalStateException(ViewBreadcrumbs.class + " not found");
        }

        breadcrumbs.removeView();

        ViewBreadcrumbs.ViewInfo currentViewInfo = breadcrumbs.getCurrentViewInfo();
        if (currentViewInfo == null) {
            throw new IllegalStateException("Current %s not found".formatted(View.class.getSimpleName()));
        }

        windowContainer.setView(currentViewInfo.view());

        AppWorkArea workArea = getConfiguredWorkArea(ui);
        JmixMainTabSheet tabSheet = workArea.getTabbedWindowContainer();
        Tab tab = tabSheet.getTab(windowContainer);
        if (tab == null) {
            throw new IllegalStateException("No tab not found");
        }

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
        AppWorkArea workArea = getConfiguredWorkArea(ui);
        workArea.switchTo(AppWorkArea.State.VIEW_CONTAINER);

        // work with new view
        createNewTabLayout(ui, view);
        ViewControllerUtils.setViewCloseDelegate(view, __ -> removeNewTabView(ui, view));
    }

    protected void removeNewTabView(JmixUI ui, View<?> view) {
        TabViewContainer windowContainer = getTabWindowContainer(view);

        AppWorkArea workArea = getConfiguredWorkArea(ui);

//        TabSheetBehaviour tabSheet = workArea.getTabbedWindowContainer().getTabSheetBehaviour();
        JmixMainTabSheet tabSheet = workArea.getTabbedWindowContainer();
        // TODO: gg, implement?
//        tabSheet.silentCloseTabAndSelectPrevious(windowContainer);
        tabSheet.remove(windowContainer);

        boolean allWindowsRemoved = tabSheet.getTabs().isEmpty();

        ViewBreadcrumbs breadcrumbs = windowContainer.getBreadcrumbs();
        if (breadcrumbs != null) {
            breadcrumbs.setNavigationHandler(null);
            breadcrumbs.removeView();
        }

        if (allWindowsRemoved) {
            workArea.switchTo(AppWorkArea.State.INITIAL_LAYOUT);

            // TODO: gg, move or re-implement, e.g. to state change listener?
            View<?> rootView = UiComponentUtils.getView(workArea);
            updatePageTitle(ui, rootView);
            updateUrl(ui, resolveLocation(rootView));
        }
    }

    protected TabViewContainer getTabWindowContainer(View<?> view) {
        return view.getParent()
                .filter(parent -> parent instanceof TabViewContainer)
                .map(parent -> ((TabViewContainer) parent))
                .orElseThrow(() -> new IllegalStateException("%s is not attached to a %s"
                        .formatted(View.class.getSimpleName(), TabViewContainer.class.getSimpleName())));
    }

    protected void createNewTabLayout(JmixUI ui, View<?> view) {
        ViewBreadcrumbs breadcrumbs = createViewBreadCrumbs(/*ui, view*/);
        breadcrumbs.setNavigationHandler(this::onBreadcrumbsNavigate);
        breadcrumbs.addView(view, resolveLocation(view));

        // TODO: gg, store route
        /*WindowImpl windowImpl = (WindowImpl) screen.getWindow();
        windowImpl.setResolvedState(createOrUpdateState(
                windowImpl.getResolvedState(),
                getConfiguredWorkArea().generateUrlStateMark()));*/

        TabViewContainer windowContainer = uiComponents.create(TabViewContainer.class);
//        windowContainer.setClassName("jmix-tab-view-container");
        windowContainer.setSizeFull();

        windowContainer.setBreadcrumbs(breadcrumbs);
        windowContainer.setView(view);

        AppWorkArea workArea = getConfiguredWorkArea(ui);

//        TabSheetBehaviour tabSheetBehaviour = workArea.getTabbedWindowContainer().getTabSheetBehaviour();
        // TODO: gg, interface?
        JmixMainTabSheet tabSheet = workArea.getTabbedWindowContainer();

        String tabId = "tab_" + UuidProvider.createUuid();

        // TODO: gg, UiComponents
        JmixViewTab newTab = new JmixViewTab(ViewControllerUtils.getPageTitle(view));
        newTab.setId(tabId);
        // TODO: gg, implement
        newTab.setClosable(true /*view.isCloseable()*/);
        newTab.addBeforeCloseListener(this::handleViewTabClose);

        Tab addedTab = tabSheet.add(newTab, windowContainer);

        /*if (ui.isTestMode()) {
            String id = "tab_" + window.getId();

            tabSheet.setTabTestId(tabId, ui.getTestIdManager().getTestId(id));
            tabSheet.setTabJmixId(tabId, id);
        }*/
//        TabWindow tabWindow = (TabWindow) window;

        /*String windowContentSwitchMode = tabWindow.getContentSwitchMode().name();
        ContentSwitchMode contentSwitchMode = ContentSwitchMode.valueOf(windowContentSwitchMode);
        tabSheet.setContentSwitchMode(tabId, contentSwitchMode);*/

//        tabSheet.setTabCloseHandler(windowContainer, this::handleTabWindowClose);
        tabSheet.setSelectedTab(addedTab);
//        } else {
        // TODO: gg, implement?
//        }
    }

    protected void handleViewTabClose(JmixViewTab.BeforeCloseEvent<JmixViewTab> event) {
        JmixViewTab tab = event.getSource();
        JmixUI jmixUI = tab.getUI()
                .filter(ui -> ui instanceof JmixUI)
                .map(ui -> (JmixUI) ui)
                .orElseThrow(() -> new IllegalStateException("%s is not attached to UI or UI is not a %s"
                        .formatted(tab.getClass().getSimpleName(), JmixUI.class.getSimpleName())));

        AppWorkArea workArea = getConfiguredWorkArea(jmixUI);

        JmixMainTabSheet tabSheet = workArea.getTabbedWindowContainer();

        TabViewContainer windowContainer = ((TabViewContainer) tabSheet.getContentByTab(tab));
        if (windowContainer == null || windowContainer.getBreadcrumbs() == null) {
            throw new IllegalStateException(ViewBreadcrumbs.class + " not found");
        }

        ViewBreadcrumbs breadcrumbs = windowContainer.getBreadcrumbs();
        Runnable closeTask = new TabCloseTask(breadcrumbs, tabSheet, tab);
        closeTask.run();
    }

    public String getLoginViewId() {
        return uiProperties.getLoginViewId();
    }

    public String getMainViewId() {
        return uiProperties.getMainViewId();
    }

    // TODO: gg, create base class?
    public class TabCloseTask implements Runnable {

        protected final ViewBreadcrumbs breadcrumbs;
        protected final JmixMainTabSheet tabSheet;
        protected final JmixViewTab tab;

        public TabCloseTask(ViewBreadcrumbs breadcrumbs, JmixMainTabSheet tabSheet, JmixViewTab tab) {
            this.breadcrumbs = breadcrumbs;
            this.tabSheet = tabSheet;
            this.tab = tab;
        }

        @Override
        public void run() {
            ViewBreadcrumbs.ViewInfo viewToClose = breadcrumbs.getCurrentViewInfo();
            if (viewToClose == null) {
//                tabSheet.remove(tab);
                return;
            }

            if (isCloseable(viewToClose.view())
                /*&& !isViewClosePrevented(viewToClose, CloseOriginType.CLOSE_BUTTON)*/) {

                viewToClose.view().close(StandardOutcome.CLOSE)
                        .then(this);
                // TODO: gg, why not this?
            }
        }
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

    protected void onBreadcrumbsNavigate(ViewBreadcrumbs.BreadcrumbsNavigationContext context) {
        // TODO: gg, create class?
        Runnable closeOperation = new Runnable() {
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
                    // TODO: gg, do we need this?
//                    if (!isViewClosePrevented(currentWindow, CloseOriginType.BREADCRUMBS)) {
                    viewToClose.view().close(StandardOutcome.CLOSE)
                            .then(this);
//                    }
                }
            }
        };
        closeOperation.run();
    }

    protected ViewBreadcrumbs createViewBreadCrumbs(/*TabbedUI ui, View<?> view*/) {
//        AppWorkArea appWorkArea = getConfiguredWorkArea(ui);

        ViewBreadcrumbs breadcrumbs = uiComponents.create(ViewBreadcrumbs.class);

        // TODO: gg, implement
//        boolean showBreadCrumbs = uiProperties.isShowBreadCrumbs() || appWorkArea.getMode() == Mode.SINGLE;
//        breadcrumbs.setVisible(showBreadCrumbs);

        return breadcrumbs;
    }

    public Optional<AppWorkArea> getConfiguredWorkAreaOptional(JmixUI ui) {
        View<?> topLevelView = ui.getTopLevelViewOptional()
                .orElseThrow(() -> new IllegalStateException("There is no root view opened"));

        if (topLevelView instanceof HasWorkArea hasWorkArea) {
            return Optional.ofNullable(hasWorkArea.getWorkArea());
        }

        return Optional.empty();
    }

    public AppWorkArea getConfiguredWorkArea(JmixUI ui) {
        return getConfiguredWorkAreaOptional(ui)
                .orElseThrow(() -> new IllegalStateException("Root View does not have any configured work area"));
    }

    protected Stream<ViewStack> getTabbedViewsStacks(AppWorkArea workArea) {
//        TabSheetBehaviour tabSheetBehaviour = workArea.getTabbedWindowContainer().getTabSheetBehaviour();
        JmixMainTabSheet tabSheetBehaviour = workArea.getTabbedWindowContainer();

        return tabSheetBehaviour.getTabComponentsStream()
                .map(component -> {
                    if (component instanceof TabViewContainer tabViewContainer) {
                        return tabViewContainer;
                    } else {
                        throw new IllegalStateException("Component is not a TabWindowContainer");
                    }
                })
                .map(windowContainer -> new ViewStack(windowContainer, workArea.getTabbedWindowContainer()));
    }

    public OpenedViews getOpenedViews() {
        return new OpenedViewsImpl();
    }

    public Component getCurrentView(JmixUI ui) {
        Component currentView = findCurrentView(ui);
        if (currentView == null) {
            throw new IllegalStateException("No view found");
        }

        return currentView;
    }

    @Nullable
    public Component findCurrentView(JmixUI ui) {
        // TODO: gg, implement
        /*Iterator<Screen> dialogsIterator = getOpenedScreens().getDialogScreens().iterator();
        if (dialogsIterator.hasNext()) {
            return dialogsIterator.next();
        }

        Iterator<Screen> screensIterator = getOpenedScreens().getCurrentBreadcrumbs().iterator();
        if (screensIterator.hasNext()) {
            return screensIterator.next();
        }

        return getOpenedScreens().getRootScreenOrNull();*/

        // TODO: gg, temp solution
        return ui.getTopLevelView();
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

    protected boolean isMaxTabCountExceeded(JmixUI ui, View<?> view, ViewOpenMode openMode) {
        if (openMode == ViewOpenMode.NEW_TAB) {
            AppWorkArea workArea = getConfiguredWorkArea(ui);

            int maxTabCount = tabbedModeProperties.getMaxTabCount();
            return maxTabCount > 0 && workArea.getOpenedTabCount() + 1 > maxTabCount;
        }

        return false;
    }

    protected void showTooManyOpenTabsMessage() {
        notifications.show(messages.formatMessage("", "tooManyOpenTabs.message",
                        tabbedModeProperties.getMaxTabCount()));
    }

    // TODO: gg, move to util

    public static boolean isAlreadyOpened(View<?> newView, View<?> openedView) {
//        return newView.isSameScreen(openedView);
        return newView.getClass() == openedView.getClass()
                && newView.getId().equals(openedView.getId());
    }

    protected class OpenedViewsImpl implements OpenedViews {

        @Override
        public View<?> getRootView() {
            // TODO: gg, implement
            return null;
        }

        @Override
        public Optional<View<?>> findRootView() {
            return Optional.empty();
        }
    }

    protected static class ViewStack {

        protected final TabViewContainer windowContainer;
        protected final JmixMainTabSheet tabbedWindowContainer;

        public ViewStack(TabViewContainer windowContainer, JmixMainTabSheet tabbedWindowContainer) {
            this.windowContainer = windowContainer;
            this.tabbedWindowContainer = tabbedWindowContainer;
        }

        /**
         * @return screens of the container in descending order, first element is active screen
         * @throws IllegalStateException in case window stack has been closed
         */
        public Collection<View<?>> getBreadcrumbs() {
            checkAttached();

            // TODO: gg, re-work?
            Deque<View<?>> viewDeque = windowContainer.getBreadcrumbs().getViews();
            Iterator<View<?>> windowIterator = viewDeque.descendingIterator();

            List<View<?>> views = new ArrayList<>(viewDeque.size());

            while (windowIterator.hasNext()) {
                View<?> view = windowIterator.next();
                views.add(view);
            }

            return views;
        }

        public boolean isSelected() {
            checkAttached();

            // TODO: gg, implement
            Tab selectedTab = tabbedWindowContainer.getSelectedTab();
//            return selectedTab == windowContainer;
            return false;
        }

        /**
         * Select tab in tabbed UI.
         */
        public void select() {
            checkAttached();
            // TODO: gg, implement
//            tabbedWindowContainer.setSelectedTab(windowContainer);
        }

        protected void checkAttached() {
//            windowContainer.getParent().isPresent()
            if (!windowContainer.isAttached()) {
                throw new IllegalStateException("WindowStack has been detached");
            }
        }
    }

    public interface OpenedViews {

        View<?> getRootView();

        Optional<View<?>> findRootView();


    }
}
