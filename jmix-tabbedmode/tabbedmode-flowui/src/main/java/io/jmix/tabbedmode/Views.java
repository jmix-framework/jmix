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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.EntityStates;
import io.jmix.core.Messages;
import io.jmix.core.UuidProvider;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.OpenedDialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.event.view.ViewOpenedEvent;
import io.jmix.flowui.sys.BeanUtil;
import io.jmix.flowui.sys.UiAccessChecker;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.tabbedmode.app.main.HasWorkArea;
import io.jmix.tabbedmode.builder.ViewOpeningContext;
import io.jmix.tabbedmode.component.breadcrumbs.ViewBreadcrumbs;
import io.jmix.tabbedmode.component.breadcrumbs.ViewBreadcrumbs.BreadcrumbsNavigationContext;
import io.jmix.tabbedmode.component.tabsheet.JmixViewTab;
import io.jmix.tabbedmode.component.tabsheet.MainTabSheetUtils;
import io.jmix.tabbedmode.component.viewcontainer.TabViewContainer;
import io.jmix.tabbedmode.component.viewcontainer.ViewContainer;
import io.jmix.tabbedmode.component.workarea.TabbedViewsContainer;
import io.jmix.tabbedmode.component.workarea.WorkArea;
import io.jmix.tabbedmode.view.*;
import io.jmix.tabbedmode.view.DialogWindow;
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
import static io.jmix.flowui.monitoring.ViewLifeCycle.BEFORE_SHOW;
import static io.jmix.flowui.monitoring.ViewLifeCycle.READY;
import static io.micrometer.core.instrument.Timer.start;

/**
 * API to create and open views.
 */
@org.springframework.stereotype.Component("tabmod_Views")
public class Views {

    private static final Logger log = LoggerFactory.getLogger(Views.class);

    public static final CloseAction CLOSE_SAME_VIEW_ACTION = new StandardCloseAction("closeSameView");

    protected final ApplicationContext applicationContext;
    protected final ViewRegistry viewRegistry;
    protected final UiComponents uiComponents;
    protected final Notifications notifications;
    protected final Messages messages;
    protected final RouteSupport routeSupport;
    protected final EntityStates entityStates;
    protected final UiProperties uiProperties;
    protected final MeterRegistry meterRegistry;
    protected final UiAccessChecker uiAccessChecker;
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
                 UiAccessChecker uiAccessChecker,
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
        this.uiAccessChecker = uiAccessChecker;
        this.tabbedModeProperties = tabbedModeProperties;
    }

    /**
     * Creates a new {@link View} instance by the passed view id.
     *
     * @param viewId view id to create a new {@link View} instance
     * @return a new {@link View} instance
     */
    public View<?> create(String viewId) {
        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
        return createInternal(viewInfo);
    }

    /**
     * Creates a new {@link View} instance by the passed view class.
     *
     * @param viewClass the class of the view to create
     * @param <T>       the type of the view to create, which extends {@link View}
     * @return a new {@link View} instance
     */
    @SuppressWarnings("unchecked")
    public <T extends View<?>> T create(Class<T> viewClass) {
        String id = ViewDescriptorUtils.getInferredViewId(viewClass);
        return (T) create(id);
    }

    protected View<?> createInternal(ViewInfo viewInfo) {
        checkPermissions(viewInfo);

        View<?> view = Instantiator.get(UI.getCurrent()).getOrCreate(viewInfo.getControllerClass());
        initProperties(view);

        return view;
    }

    protected void initProperties(View<?> view) {
        TabbedModeViewProperties properties = TabbedModeUtils.getViewProperties(view);
        if (properties == null) {
            properties = new TabbedModeViewProperties();
        }

        boolean closeable = ViewControllerUtils.findAnnotation(view, ViewProperties.class)
                .map(ViewProperties::closeable).orElse(true);
        properties.setCloseable(closeable);

        boolean forceDialog = ViewControllerUtils.findAnnotation(view, ViewProperties.class)
                .map(ViewProperties::forceDialog).orElse(false);
        properties.setForceDialog(forceDialog);

        TabbedModeUtils.setViewProperties(view, properties);
    }

    protected void checkPermissions(ViewInfo viewInfo) {
        if (uiProperties.getLoginViewId().equals(viewInfo.getId())) {
            return;
        }

        boolean viewPermitted = uiAccessChecker.isViewPermitted(viewInfo.getControllerClass());
        if (!viewPermitted) {
            throw new AccessDeniedException("view", viewInfo.getId());
        }
    }

    /**
     * Opens the passed {@link View} in the current UI with the passed {@link ViewOpenMode}.
     *
     * @param view     a view to open
     * @param openMode in which open mode to open a view
     * @return {@link OperationResult} with the result of the opening operation
     */
    public OperationResult open(View<?> view, ViewOpenMode openMode) {
        return open(getCurrentUI(), view, openMode);
    }

    /**
     * Opens the passed {@link View} in the given UI with the passed {@link ViewOpenMode}.
     *
     * @param ui       UI in which to open a view
     * @param view     a view to open
     * @param openMode in which open mode to open a view
     * @return {@link OperationResult} with the result of the opening operation
     */
    public OperationResult open(JmixUI ui, View<?> view, ViewOpenMode openMode) {
        return open(ui, ViewOpeningContext.create(view, openMode));
    }

    public OperationResult open(ViewOpeningContext context) {
        return open(getCurrentUI(), context);
    }

    /**
     * Opens a {@link View} in the given UI using parameters obtained from
     * passed {@link ViewOpeningContext}.
     *
     * @param ui      UI in which to open a view
     * @param context context with the view to open and other parameters
     * @return {@link OperationResult} with the result of the opening operation
     */
    public OperationResult open(JmixUI ui, ViewOpeningContext context) {
        checkNotNullArgument(context);

        View<?> view = context.getView();
        ViewOpenMode openMode = getActualOpenMode(ui, context);

        OperationResult result = closeSameView(ui, context);
        if (result != null) {
            return result;
        }

        checkNotYetOpened(view);

        if (isMaxTabCountExceeded(ui, openMode)) {
            showTooManyOpenTabsMessage();
            return OperationResult.fail();
        }

        sendNavigationEvent(ui, context);

        fireQueryParametersChangeEvent(view, context.getQueryParameters());

        Timer.Sample beforeShowSample = start(meterRegistry);
        fireViewBeforeShowEvent(view);
        stopViewTimerSample(beforeShowSample, meterRegistry, BEFORE_SHOW, view.getId().orElse(null));

        switch (openMode) {
            case ROOT:
                openRootView(ui, context);
                break;

            case THIS_TAB:
                openThisTab(ui, context);
                break;

            case NEW_TAB:
                openNewTab(ui, context);
                break;

            case DIALOG:
                openDialogWindow(ui, context);
                break;

            default:
                throw new UnsupportedOperationException("Unsupported OpenMode " + openMode);
        }

        log.trace("View {} {} opened", view.getId().orElse(null), view.getClass());

        if (openMode != ViewOpenMode.DIALOG) {
            updateUrl(ui, resolveLocation(view, context));
            updatePageTitle(view);
        }

        Timer.Sample readySample = start(meterRegistry);
        fireViewReadyEvent(view);
        stopViewTimerSample(readySample, meterRegistry, READY, view.getId().orElse(null));

        fireViewOpenedEvent(view);

        return OperationResult.success();
    }

    @Nullable
    protected OperationResult closeSameView(JmixUI ui, ViewOpeningContext context) {
        if (ViewOpenMode.NEW_TAB != context.getOpenMode()
                || !context.isCheckMultipleOpen()
                || isMultipleOpen(context.getView())) {
            return null;
        }

        View<?> view = context.getView();
        WorkArea workArea = getConfiguredWorkArea(ui);
        View<?> sameView = getTabbedViewsStacks(workArea)
                .filter(viewStack -> viewStack.getBreadcrumbs().size() == 1) // never close non-top active screens
                .map(viewStack -> viewStack.getBreadcrumbs().iterator().next())
                .filter(tabScreen -> ViewControllerUtils.isSameView(view, tabScreen))
                .findFirst()
                .orElse(null);

        if (sameView != null) {
            // Select tab before close
            ViewContainer viewContainer = TabbedModeUtils.getViewContainer(sameView);
            TabbedViewsContainer<?> tabbedViewsContainer = workArea.getTabbedViewsContainer();
            Tab tab = tabbedViewsContainer.getTab(((Component) viewContainer));
            tabbedViewsContainer.setSelectedTab(tab);

            OperationResult result = sameView.close(CLOSE_SAME_VIEW_ACTION);
            if (result.getStatus() != OperationResult.Status.SUCCESS) {
                // if the unsaved changes dialog is shown, we can continue later
                return result.compose(() -> open(ui, context));
            }
        }

        return null;
    }

    protected boolean isMultipleOpen(View<?> view) {
        return ViewControllerUtils.findAnnotation(view, MultipleOpen.class)
                .map(MultipleOpen::value)
                .orElseGet(tabbedModeProperties::isMultipleOpen);
    }

    protected ViewOpenMode getActualOpenMode(JmixUI ui, ViewOpeningContext context) {
        ViewOpenMode openMode = context.getOpenMode();

        if (TabbedModeUtils.isForceDialog(context.getView())) {
            openMode = ViewOpenMode.DIALOG;
        }

        if (openMode == ViewOpenMode.THIS_TAB
                && getConfiguredWorkArea(ui).getState() == WorkArea.State.INITIAL_LAYOUT) {
            openMode = ViewOpenMode.NEW_TAB;
        }

        if (openMode != ViewOpenMode.DIALOG
                && openMode != ViewOpenMode.ROOT
                && hasModalDialogWindow(ui)) {
            openMode = ViewOpenMode.DIALOG;
        }

        return openMode;
    }

    protected boolean hasModalDialogWindow(JmixUI ui) {
        return getOpenedViews(ui)
                .getDialogWindows()
                .stream()
                .anyMatch(view -> {
                    Dialog dialog = UiComponentUtils.findDialog(view);
                    return dialog != null && dialog.isModal();
                });
    }

    // For compatibility with navigation, only.
    protected void sendNavigationEvent(JmixUI ui, ViewOpeningContext context) {
        RouteParameters routeParameters = context.getRouteParameters();
        QueryParameters queryParameters = context.getQueryParameters();
        if (routeParameters.getParameterNames().isEmpty()
                && queryParameters.getParameters().isEmpty()) {
            return;
        }

        View<?> view = context.getView();
        BeforeEnterEvent event = new BeforeEnterEvent(
                ui.getInternals().getRouter(),
                NavigationTrigger.PROGRAMMATIC,
                resolveLocation(view, context),
                view.getClass(),
                routeParameters,
                ui,
                Collections.emptyList()
        );
        ViewControllerUtils.processBeforeEnterInternal(view, event);
    }

    protected void checkNotYetOpened(View<?> view) {
        if (view.isAttached()) {
            throw new IllegalStateException("%s is already opened: '%s'"
                    .formatted(View.class.getSimpleName(), view.getId().orElse(null)));
        }
    }

    protected void updatePageTitle(View<?> view) {
        String title = ViewControllerUtils.getPageTitle(view);
        updatePageTitle(view, title);
    }

    protected void updatePageTitle(View<?> view, String title) {
        JmixUI ui = getUI(view);
        ui.getInternals().cancelPendingTitleUpdate();
        ui.getInternals().setTitle(title);
    }

    protected void updateUrl(JmixUI ui, Location newLocation) {
        routeSupport.setLocation(ui, newLocation);
    }

    protected Location resolveLocation(View<?> view) {
        return resolveLocation(view, null);
    }

    protected Location resolveLocation(View<?> view, @Nullable ViewOpeningContext context) {
        String locationString = ViewControllerUtils.findAnnotation(view, Route.class).isPresent()
                ? resolveLocationString(view, context)
                : getEmptyLocationString(view, context);

        QueryParameters queryParameters = context != null
                ? context.getQueryParameters()
                : QueryParameters.empty();

        return new Location(locationString, queryParameters);
    }

    protected String resolveLocationString(View<?> view, @Nullable ViewOpeningContext context) {
        RouteParameters routeParameters = context != null
                ? context.getRouteParameters()
                : RouteParameters.empty();

        if (routeParameters.getParameterNames().isEmpty()
                && view instanceof StandardDetailView<?> detailView) {
            String param = getRouteParamName(detailView);
            Object value = getParamValue(detailView);
            routeParameters = routeSupport.createRouteParameters(param, value);
        }

        Class<? extends Component> navigationTarget = view.getClass();
        if (getRouteConfiguration().isRouteRegistered(navigationTarget)) {
            return getRouteConfiguration().getUrl(navigationTarget, routeParameters);
        } else if (routeParameters.getParameterNames().isEmpty() && isRootView(view)) {
            // Happens if root view is hot-deployed
            return resolveLocationBaseString(view);
        } else {
            throw new NotFoundException(String.format(
                    "No route found for the given navigation target '%s' and parameters '%s'",
                    navigationTarget.getName(), routeParameters));
        }
    }

    protected boolean isRootView(Component component) {
        Optional<Route> annotation = ViewControllerUtils.findAnnotation(component, Route.class);
        return annotation.isPresent()
                && UI.class.isAssignableFrom(annotation.get().layout())
                || UiComponentUtils.sameId(component, getMainViewId())
                || UiComponentUtils.sameId(component, getLoginViewId());
    }

    protected String resolveLocationBaseString(View<?> view) {
        Optional<Route> annotation = ViewControllerUtils.findAnnotation(view, Route.class);
        return annotation.isPresent()
                ? annotation.get().value()
                : getEmptyLocationString(view, null);
    }

    protected String getEmptyLocationString(View<?> view, @Nullable ViewOpeningContext context) {
        return "";
    }

    protected String getRouteParamName(StandardDetailView<?> detailView) {
        return ViewControllerUtils.getRouteParamName(detailView);
    }

    protected RouteConfiguration getRouteConfiguration() {
        return RouteConfiguration.forSessionScope();
    }

    protected Object getParamValue(DetailView<?> detailView) {
        Object editedEntity = detailView.getEditedEntity();
        return entityStates.isNew(editedEntity)
                ? StandardDetailView.NEW_ENTITY_ID
                : Objects.requireNonNull(EntityValues.getId(editedEntity));
    }

    protected void openRootView(JmixUI ui, ViewOpeningContext context) {
        ui.setTopLevelView(context.getView());
    }

    protected void openDialogWindow(JmixUI ui, ViewOpeningContext context) {
        View<?> view = context.getView();
        DialogWindow<?> dialogWindow = createDialog(view);

        if (ui.hasModalComponent()) {
            // force modal
            dialogWindow.setModal(true);
        }

        dialogWindow.open();
    }

    protected <V extends View<?>> DialogWindow<V> createDialog(V view) {
        DialogWindow<V> dialogWindow = new DialogWindow<>(view);
        BeanUtil.autowireContext(applicationContext, dialogWindow);

        return dialogWindow;
    }

    protected void openThisTab(JmixUI ui, ViewOpeningContext context) {
        View<?> view = context.getView();
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
        breadcrumbs.addView(view, resolveLocation(view, context));

        ViewControllerUtils.setViewCloseDelegate(view, closingView ->
                removeThisTabView(workArea, closingView));
        ViewControllerUtils.setPageTitleDelegate(view, title -> {
            updateTabTitle(selectedTab, title);
            updatePageTitle(view, title);
        });

        updateTabTitle(selectedTab, ViewControllerUtils.getPageTitle(view));
        if (selectedTab instanceof JmixViewTab viewTab) {
            viewTab.setClosable(TabbedModeUtils.isCloseable(view));
        }
    }

    protected void removeThisTabView(WorkArea workArea, View<?> viewToRemove) {
        ViewContainer viewContainer = TabbedModeUtils.getViewContainer(viewToRemove);
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

        View<?> viewToDisplay = currentViewInfo.view();
        viewContainer.setView(viewToDisplay);

        TabbedViewsContainer<?> tabbedContainer = workArea.getTabbedViewsContainer();
        Tab tab = tabbedContainer.getTab(((Component) viewContainer));

        updateTabTitle(tab, ViewControllerUtils.getPageTitle(viewToDisplay));
        if (tab instanceof JmixViewTab viewTab) {
            viewTab.setClosable(TabbedModeUtils.isCloseable(viewToDisplay));
        }

        JmixUI ui = getUI(workArea);
        updateUrl(ui, currentViewInfo.location());
        updatePageTitle(viewToDisplay);
    }

    protected void openNewTab(JmixUI ui, ViewOpeningContext context) {
        View<?> view = context.getView();
        WorkArea workArea = getConfiguredWorkArea(ui);
        workArea.switchTo(WorkArea.State.VIEW_CONTAINER);

        createNewTabLayout(workArea, context);
        ViewControllerUtils.setViewCloseDelegate(view, closingView ->
                removeNewTabView(workArea, closingView));
    }

    protected void removeNewTabView(WorkArea workArea, View<?> view) {
        ViewContainer viewContainer = TabbedModeUtils.getViewContainer(view);

        TabbedViewsContainer<?> tabbedContainer = workArea.getTabbedViewsContainer();
        tabbedContainer.remove(((Component) viewContainer));

        boolean allViewsRemoved = tabbedContainer.getTabsStream().findAny().isEmpty();

        ViewBreadcrumbs breadcrumbs = viewContainer.getBreadcrumbs();
        if (breadcrumbs != null) {
            breadcrumbs.setNavigationHandler(null);
            breadcrumbs.removeView();
        }

        if (allViewsRemoved) {
            workArea.switchTo(WorkArea.State.INITIAL_LAYOUT);

            View<?> rootView = UiComponentUtils.getView(workArea);
            updatePageTitle(rootView);

            JmixUI ui = getUI(workArea);
            updateUrl(ui, resolveLocation(rootView));
        }
    }

    protected void createNewTabLayout(WorkArea workArea, ViewOpeningContext context) {
        View<?> view = context.getView();
        ViewBreadcrumbs breadcrumbs = createViewBreadCrumbs();
        breadcrumbs.setNavigationHandler(this::onBreadcrumbsNavigate);
        breadcrumbs.addView(view, resolveLocation(view, context));

        TabViewContainer viewContainer = uiComponents.create(TabViewContainer.class);
        viewContainer.setSizeFull();

        viewContainer.setBreadcrumbs(breadcrumbs);
        viewContainer.setView(view);

        TabbedViewsContainer<?> tabbedContainer = workArea.getTabbedViewsContainer();

        String tabId = "tab_" + UuidProvider.createUuid();

        JmixViewTab newTab = uiComponents.create(JmixViewTab.class);
        newTab.setId(tabId);
        newTab.setText(ViewControllerUtils.getPageTitle(view));
        newTab.setClosable(TabbedModeUtils.isCloseable(view));
        newTab.setCloseDelegate(this::handleViewTabClose);

        ViewControllerUtils.setPageTitleDelegate(view, title -> {
            updateTabTitle(newTab, title);
            updatePageTitle(view, title);
        });

        Tab addedTab = tabbedContainer.add(newTab, viewContainer);
        tabbedContainer.setSelectedTab(addedTab);
    }

    protected void updateTabTitle(Tab tab, String title) {
        if (tab instanceof JmixViewTab viewTab) {
            viewTab.setText(title);
        } else {
            tab.setLabel(title);
        }
    }

    protected void onBreadcrumbsNavigate(BreadcrumbsNavigationContext context) {
        new BreadcrumbsNavigationTask(context).run();
    }

    protected void handleViewTabClose(JmixViewTab.CloseContext<JmixViewTab> context) {
        JmixViewTab tab = context.source();
        UI ui = tab.getUI().orElse(null);
        if (!(ui instanceof JmixUI jmixUI)) {
            throw new IllegalStateException("%s is not attached to UI or UI is not a %s"
                    .formatted(tab.getClass().getSimpleName(), JmixUI.class.getSimpleName()));
        }

        WorkArea workArea = getConfiguredWorkArea(jmixUI);
        TabbedViewsContainer<?> tabbedContainer = workArea.getTabbedViewsContainer();

        createTabCloseTask(tabbedContainer, tab).run();
    }

    protected TabCloseTask createTabCloseTask(TabbedViewsContainer<?> tabbedContainer, Tab tab) {
        return new TabCloseTask(tabbedContainer, tab);
    }

    /**
     * Returns id of the {@link View} that will be used as Login view
     *
     * @return id of the {@link View} that will be used as Login view
     * @see UiProperties#getLoginViewId()
     */
    public String getLoginViewId() {
        return uiProperties.getLoginViewId();
    }

    /**
     * Returns id of the {@link View} that will be used as Main view
     *
     * @return id of the {@link View} that will be used as Main view
     * @see UiProperties#getMainViewId()
     */
    public String getMainViewId() {
        return uiProperties.getMainViewId();
    }

    protected ViewBreadcrumbs createViewBreadCrumbs() {
        ViewBreadcrumbs breadcrumbs = uiComponents.create(ViewBreadcrumbs.class);
        breadcrumbs.setVisible(tabbedModeProperties.isShowBreadcrumbs());

        return breadcrumbs;
    }

    /**
     * Finds the {@link WorkArea} instance for the given {@link JmixUI} instance, if available.
     *
     * @param ui the {@link JmixUI} instance for which the work area is being searched
     * @return an {@link Optional} containing the configured {@link WorkArea} if found;
     * otherwise, an empty {@link Optional}
     */
    public Optional<WorkArea> findConfiguredWorkArea(JmixUI ui) {
        return ui.getTopLevelView() instanceof HasWorkArea hasWorkArea
                ? hasWorkArea.getWorkAreaOptional()
                : Optional.empty();
    }

    /**
     * Returns the {@link WorkArea} instance for the given {@link JmixUI} instance.
     *
     * @param ui the {@link JmixUI} instance for which the work area is being searched
     * @return the {@link WorkArea} instance for the given {@link JmixUI} instance
     * @throws IllegalStateException if root view does not have any configured work area
     */
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

    /**
     * Returns an instance of {@link OpenedViews} for the current {@link JmixUI} instance.
     *
     * @return an instance of {@link OpenedViews} for the current {@link JmixUI} instance
     */
    public OpenedViews getOpenedViews() {
        return getOpenedViews(getCurrentUI());
    }

    /**
     * Returns an instance of {@link OpenedViews} for the given {@link JmixUI} instance.
     *
     * @param ui the {@link JmixUI} instance for which to get the {@link OpenedViews}
     * @return an instance of {@link OpenedViews} for the given {@link JmixUI} instance
     */
    public OpenedViews getOpenedViews(JmixUI ui) {
        OpenedDialogWindows openedDialogWindows = applicationContext.getBean(OpenedDialogWindows.class);
        return new OpenedViews(ui, openedDialogWindows);
    }

    /**
     * Returns the current active {@link View} within a specific UI context.
     * This method searches for the current view using a prioritized approach:
     * <ol>
     *     <li>Dialog windows</li>
     *     <li>Views of the currently opened tab of a {@link WorkArea}</li>
     *     <li>The root view</li>
     * </ol>
     *
     * @param ui the {@link JmixUI} instance representing the UI context in which
     *           to find the current view.
     * @return the current active {@link View}
     * @throws IllegalStateException if the current {@link View} is not found
     */
    public View<?> getCurrentView(JmixUI ui) {
        return findCurrentView(ui)
                .orElseThrow(() -> new IllegalStateException("No %s found"
                        .formatted(View.class.getSimpleName())));
    }

    /**
     * Finds the current active view within a specific UI context.
     * This method searches for the current view using a prioritized approach:
     * <ol>
     *     <li>Dialog windows</li>
     *     <li>Views of the currently opened tab of a {@link WorkArea}</li>
     *     <li>The root view</li>
     * </ol>
     *
     * @param ui the {@link JmixUI} instance representing the UI context in which
     *           to find the current view
     * @return an {@link Optional} containing the current view if found;
     * otherwise, an empty {@link Optional}
     */
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

    protected void fireQueryParametersChangeEvent(View<?> view, QueryParameters queryParameters) {
        ViewControllerUtils.fireEvent(view, new View.QueryParametersChangeEvent(view, queryParameters));
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
        notifications.create(messages.formatMessage("", "tooManyOpenTabs.message",
                        tabbedModeProperties.getMaxTabCount()))
                .withType(Notifications.Type.WARNING)
                .withClassName(LumoUtility.Whitespace.PRE)
                .show();
    }

    protected JmixUI getCurrentUI() {
        UI ui = UI.getCurrent();
        if (!(ui instanceof JmixUI jmixUI)) {
            throw new IllegalStateException("UI is not a " + JmixUI.class.getSimpleName());
        }

        return jmixUI;
    }

    protected JmixUI getUI(Component component) {
        UI ui = component.getUI()
                .orElseThrow(() ->
                        new IllegalStateException("%s is not attached to an UI".formatted(component)));

        if (!(ui instanceof JmixUI jmixUI)) {
            throw new IllegalStateException("UI is not a " + JmixUI.class.getSimpleName());
        }

        return jmixUI;
    }

    /**
     * A class that provides information about all opened views.
     */
    public class OpenedViews {

        protected final JmixUI ui;
        protected final OpenedDialogWindows openedDialogWindows;

        public OpenedViews(JmixUI ui,
                           OpenedDialogWindows openedDialogWindows) {
            this.ui = ui;
            this.openedDialogWindows = openedDialogWindows;
        }

        /**
         * @return the root view of the UI
         * @throws IllegalStateException in case there is no root view in UI
         */
        public View<?> getRootView() {
            return ui.getTopLevelView();
        }

        /**
         * @return the root view of the UI if present
         */
        public Optional<View<?>> findRootView() {
            return ui.getTopLevelViewOptional();
        }

        /**
         * @return all opened views excluding the root view or empty collection
         * if the root view does not have {@link WorkArea}
         * @throws IllegalStateException in case there is no root view in UI
         */
        public Collection<View<?>> getAll() {
            List<View<?>> views = new ArrayList<>();
            views.addAll(getOpenedWorkAreaViews());
            views.addAll(getDialogWindows());

            return views;
        }

        /**
         * @return all opened views excluding the root view and dialogs or empty collection
         * if the root view does not have {@link WorkArea}
         * @throws IllegalStateException in case there is no root view in UI
         */
        public Collection<View<?>> getOpenedWorkAreaViews() {
            return findConfiguredWorkArea(ui)
                    .map(WorkArea::getOpenedWorkAreaViews)
                    .orElse(Collections.emptyList());
        }

        /**
         * @return top views from work area tabs and all dialog windows or empty collection
         * if the root view does not have {@link WorkArea}
         * @throws IllegalStateException in case there is no root view in UI
         */
        public Collection<View<?>> getActiveViews() {
            List<View<?>> views = new ArrayList<>();
            views.addAll(getActiveWorkAreaViews());
            views.addAll(getDialogWindows());

            return views;
        }

        /**
         * @return top views from work area tabs or empty collection
         * if the root view does not have {@link WorkArea}
         * @throws IllegalStateException in case there is no root view in UI
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
         * @return views of the currently opened tab of a {@link WorkArea}
         * in descending order (first element is active view) or empty collection
         * if the root view does not have {@link WorkArea}
         * @throws IllegalStateException in case there is no root view in UI
         */
        public Collection<View<?>> getCurrentBreadcrumbs() {
            return findConfiguredWorkArea(ui)
                    .map(WorkArea::getCurrentBreadcrumbs)
                    .orElse(Collections.emptyList());
        }

        /**
         * @return tab containers with access to breadcrumbs or empty collection
         * if the root view does not have {@link WorkArea}
         * @throws IllegalStateException in case there is no root view in UI
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

        /**
         * Closes all child views (views if {@link WorkArea} and dialog windows)
         * from the root view.
         */
        public void closeAll() {
            Collection<View<?>> dialogWindows = getDialogWindows();
            for (View<?> view : dialogWindows) {
                if (!TabbedModeUtils.isCloseable(view)) {
                    continue;
                }

                OperationResult closeResult = view.close(StandardOutcome.CLOSE);
                if (closeResult.getStatus() != OperationResult.Status.SUCCESS) {
                    return;
                }
            }

            Collection<ViewStack> workAreaViewStacks = getWorkAreaViewStacks();
            for (ViewStack viewStack : workAreaViewStacks) {
                if (!viewStack.close()) {
                    viewStack.select();
                    return;
                }
            }
        }

        /**
         * Check if there are views that have unsaved changes.
         *
         * @return {@code true} if there are views with unsaved changes, {@code false} otherwise
         */
        public boolean hasUnsavedChanges() {
            View<?> rootView = getRootView();
            if (rootView instanceof ChangeTracker changeTracker
                    && changeTracker.hasUnsavedChanges()) {
                return true;
            }

            return getAll().stream()
                    .anyMatch(openedView -> (openedView instanceof ChangeTracker changeTracker)
                            && changeTracker.hasUnsavedChanges());
        }
    }

    /**
     * A class representing views of a tab.
     */
    public static class ViewStack {

        protected final TabbedViewsContainer<?> tabbedContainer;
        protected final ViewContainer viewContainer;

        public ViewStack(TabbedViewsContainer<?> tabbedContainer, ViewContainer viewContainer) {
            this.tabbedContainer = tabbedContainer;
            this.viewContainer = viewContainer;
        }

        /**
         * Returns a collection of {@link View} stack in descending order, the first element is an active view.
         *
         * @return a collection of {@link View} stack representing the current breadcrumbs,
         * or an empty collection if no breadcrumbs are available.
         */
        public Collection<View<?>> getBreadcrumbs() {
            checkAttached();

            return TabbedModeUtils.getBreadcrumbs(viewContainer);
        }

        /**
         * Whether a tab displaying this {@link ViewStack} is active.
         *
         * @return {code true} if a tab displaying this {@link ViewStack} i
         * s active, {@code false} otherwise
         */
        public boolean isSelected() {
            checkAttached();

            Tab selectedTab = tabbedContainer.getSelectedTab();
            return selectedTab != null
                    && tabbedContainer.getComponent(selectedTab).equals(viewContainer);
        }

        /**
         * Makes a tab displaying this {@link ViewStack} active.
         */
        public void select() {
            checkAttached();

            Tab tab = tabbedContainer.getTab(((Component) viewContainer));
            if (!tab.equals(tabbedContainer.getSelectedTab())) {
                tabbedContainer.setSelectedTab(tab);
            }
        }

        /**
         * Closes all views of this {@link ViewStack} returned by {@link #getBreadcrumbs()}.
         * <p>
         * Note that a tab representing a view stack is not selected before closing.
         *
         * @return {@code true} if all views have been closed, {@code false} otherwise
         * @see #close(boolean)
         */
        public boolean close() {
            return close(false);
        }

        /**
         * Closes all views of this {@link ViewStack} returned by {@link #getBreadcrumbs()}.
         *
         * @param selectBeforeClose whether a tab representing a view stack should be
         *                          selected before closing
         * @return {@code true} if all views have been closed, {@code false} otherwise
         * @see #close()
         */
        public boolean close(boolean selectBeforeClose) {
            boolean closed = true;

            if (selectBeforeClose) {
                select();
            }

            Collection<View<?>> views = getBreadcrumbs();
            for (View<?> view : views) {
                if (!TabbedModeUtils.isCloseable(view)) {
                    continue;
                }

                OperationResult closeResult = view.close(StandardOutcome.CLOSE);
                if (closeResult.getStatus() != OperationResult.Status.SUCCESS) {
                    closed = false;
                    break;
                }
            }

            return closed;
        }

        protected void checkAttached() {
            Optional<Tab> tab = tabbedContainer.findTab(((Component) viewContainer));
            if (tab.isEmpty()) {
                throw new IllegalStateException("%s has been detached"
                        .formatted(ViewStack.class.getSimpleName()));
            }
        }
    }

    protected static class BreadcrumbsNavigationTask extends AbstractTabCloseTask {

        protected final BreadcrumbsNavigationContext context;

        public BreadcrumbsNavigationTask(BreadcrumbsNavigationContext context) {
            this.context = context;
        }

        @Override
        protected boolean isCloseable(ViewBreadcrumbs.ViewInfo viewToClose) {
            return super.isCloseable(viewToClose)
                    && !viewToClose.view().equals(context.view());
        }

        @Nullable
        @Override
        protected ViewBreadcrumbs.ViewInfo getViewToClose() {
            return context.breadcrumbs().getCurrentViewInfo();
        }
    }

    protected static class TabCloseTask extends AbstractTabCloseTask {

        protected final TabbedViewsContainer<?> tabbedContainer;
        protected final Tab tab;
        protected final ViewBreadcrumbs breadcrumbs;

        public TabCloseTask(TabbedViewsContainer<?> tabbedContainer, Tab tab) {
            this.tabbedContainer = tabbedContainer;
            this.tab = tab;

            breadcrumbs = getViewBreadcrumbs(tabbedContainer, tab);
        }

        @Nullable
        @Override
        protected ViewBreadcrumbs.ViewInfo getViewToClose() {
            return breadcrumbs.getCurrentViewInfo();
        }

        @Override
        protected void closePrevented() {
            tabbedContainer.setSelectedTab(tab);
        }
    }

    protected abstract static class AbstractTabCloseTask implements Runnable {

        @Override
        public void run() {
            ViewBreadcrumbs.ViewInfo viewToClose = getViewToClose();
            if (viewToClose != null
                    && isCloseable(viewToClose)) {
                OperationResult closeResult = viewToClose.view().close(StandardOutcome.CLOSE);
                if (closeResult.getStatus() == OperationResult.Status.SUCCESS) {
                    closeResult.then(this);
                } else {
                    closePrevented();
                }
            }
        }

        protected void closePrevented() {
            // do nothing
        }

        @Nullable
        protected abstract ViewBreadcrumbs.ViewInfo getViewToClose();

        protected boolean isCloseable(ViewBreadcrumbs.ViewInfo viewToClose) {
            return TabbedModeUtils.isCloseable(viewToClose.view());
        }
    }

    // TODO: gg, use where possible and move to util
    protected static ViewBreadcrumbs getViewBreadcrumbs(TabbedViewsContainer<?> tabbedContainer, Tab tab) {
        Component tabComponent = tabbedContainer.getComponent(tab);
        if (!(tabComponent instanceof ViewContainer viewContainer)
                || viewContainer.getBreadcrumbs() == null) {
            throw new IllegalStateException("%s not found"
                    .formatted(ViewBreadcrumbs.class.getSimpleName()));
        }

        return viewContainer.getBreadcrumbs();
    }
}
