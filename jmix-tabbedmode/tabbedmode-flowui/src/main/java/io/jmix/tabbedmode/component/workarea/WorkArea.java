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

package io.jmix.tabbedmode.component.workarea;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.ComponentContainer;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.tabbedmode.TabbedModeProperties;
import io.jmix.tabbedmode.component.breadcrumbs.ViewBreadcrumbs;
import io.jmix.tabbedmode.component.tabsheet.MainTabSheetUtils;
import io.jmix.tabbedmode.component.viewcontainer.ViewContainer;
import io.jmix.tabbedmode.view.TabbedModeUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.flowui.component.UiComponentUtils.sameId;

@Tag("jmix-work-area")
@JsModule("./src/workarea/jmix-work-area.js")
public class WorkArea extends Component implements HasSize, ComponentContainer, ApplicationContextAware {

    protected ApplicationContext applicationContext;
    protected RouteSupport routeSupport;    // lazy initialized
    protected ViewRegistry viewRegistry;    // lazy initialized

    protected State state = State.INITIAL_LAYOUT;

    protected TabbedViewsContainer<?> tabbedContainer;
    protected VerticalLayout initialLayout;

    protected ShortcutRegistration nextTabShortcutRegistration;
    protected ShortcutRegistration previousTabShortcutRegistration;

    public WorkArea() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * @return the current state
     */
    public State getState() {
        return state;
    }

    /**
     * Switches the {@link WorkArea} to the specified state and updates its
     * content layout accordingly.
     *
     * @param state the new state to switch to
     * @throws IllegalStateException if an unsupported state is passed.
     */
    public void switchTo(State state) {
        if (this.state == state) {
            return;
        }

        getElement().removeAllChildren();

        switch (state) {
            case INITIAL_LAYOUT:
                getElement().appendChild(initialLayout.getElement());
                break;
            case VIEW_CONTAINER:
                getElement().appendChild(tabbedContainer.getElement());
                break;
            default:
                throw new IllegalStateException("Unexpected state: " + state);
        }

        this.state = state;

        fireEvent(new StateChangeEvent(this, state));
    }

    /**
     * Returns a collection of all views currently opened in the work area's
     * tabbed view container.
     *
     * @return a collection of {@link View} objects representing the views
     * currently opened in the tabbed view container.
     */
    public Collection<View<?>> getOpenedWorkAreaViews() {
        TabbedViewsContainer<?> tabbedContainer = getTabbedViewsContainer();

        return tabbedContainer.getTabComponentsStream()
                .flatMap(component -> {
                    ViewContainer viewContainer = MainTabSheetUtils.asViewContainer(component);
                    ViewBreadcrumbs breadcrumbs = viewContainer.getBreadcrumbs();
                    return breadcrumbs != null
                            ? breadcrumbs.getViews().stream()
                            : Stream.empty();
                }).toList();
    }

    /**
     * Returns a collection of active views currently displayed in the work area's
     * tabbed view container. An active view corresponds to the views associated
     * with the current components within the tabs.
     *
     * @return a collection of {@link View} objects representing the active views
     * in the tabbed view container
     */
    public Collection<View<?>> getActiveWorkAreaViews() {
        TabbedViewsContainer<?> tabbedContainer = getTabbedViewsContainer();
        return tabbedContainer.getTabComponentsStream()
                .map(MainTabSheetUtils::getViewFromContent)
                .collect(Collectors.toList());
    }

    /**
     * Returns a collection of {@link View} stack of currently active tab in descending order,
     * the first element is an active view.
     *
     * @return a collection of {@link View} stack representing the current breadcrumbs,
     * or an empty collection if no breadcrumbs are available.
     */
    public Collection<View<?>> getCurrentBreadcrumbs() {
        ViewContainer viewContainer = getCurrentViewContainer();
        if (viewContainer == null) {
            return Collections.emptyList();
        }

        return TabbedModeUtils.getBreadcrumbs(viewContainer);
    }

    /**
     * Returns the {@link TabbedViewsContainer} instance associated with the work area.
     * If the container is not initialized, an {@link IllegalStateException} is thrown.
     *
     * @return the {@link TabbedViewsContainer} instance representing the tabbed view
     * container of the work area
     * @throws IllegalStateException if the tabbed view container is not initialized
     */
    public TabbedViewsContainer<?> getTabbedViewsContainer() {
        checkState(tabbedContainer != null, "%s is not initialized"
                .formatted(TabbedViewsContainer.class.getSimpleName()));

        return tabbedContainer;
    }

    /**
     * Sets the {@link TabbedViewsContainer} for this work area. The tabbed views container
     * is responsible for managing tabbed views within the work area. This method ensures
     * that the container is initialized only once and will throw an exception if a container
     * has already been set.
     *
     * @param tabbedContainer the {@link TabbedViewsContainer} to associate with this work area
     * @throws IllegalStateException if the tabbed views container has already been initialized
     * @throws NullPointerException  if {@code tabbedContainer} is {@code null}
     */
    public void setTabbedViewsContainer(TabbedViewsContainer<?> tabbedContainer) {
        checkState(this.tabbedContainer == null, "%s has already been initialized"
                .formatted(TabbedViewsContainer.class.getSimpleName()));
        Preconditions.checkNotNullArgument(tabbedContainer);

        this.tabbedContainer = tabbedContainer;

        initTabbedViewsContainer(tabbedContainer);
    }

    protected void initTabbedViewsContainer(TabbedViewsContainer<?> tabbedContainer) {
        tabbedContainer.addSelectedChangeListener(this::onSelectedTabChanged);
        initTabbedViewsContainerShortcuts(tabbedContainer);
    }

    protected void initTabbedViewsContainerShortcuts(TabbedViewsContainer<?> tabbedContainer) {
        TabbedModeProperties properties = applicationContext.getBean(TabbedModeProperties.class);

        KeyCombination nextTabShortcut = KeyCombination.create(properties.getOpenNextTabShortcut());
        if (nextTabShortcut != null) {
            nextTabShortcutRegistration = Shortcuts.addShortcutListener((Component) tabbedContainer,
                    this::switchToNextTab,
                    nextTabShortcut.getKey(),
                    nextTabShortcut.getKeyModifiers());
        }

        KeyCombination previousTabShortcut = KeyCombination.create(properties.getOpenPreviousTabShortcut());
        if (previousTabShortcut != null) {
            previousTabShortcutRegistration = Shortcuts.addShortcutListener((Component) tabbedContainer,
                    this::switchToPreviousTab,
                    previousTabShortcut.getKey(),
                    previousTabShortcut.getKeyModifiers());
        }
    }

    protected void switchToNextTab() {
        switchTab((selectedIndex, tabsNumber) ->
                (selectedIndex + 1) % tabsNumber);
    }

    protected void switchToPreviousTab() {
        switchTab((selectedIndex, tabsNumber) ->
                (tabsNumber + selectedIndex - 1) % tabsNumber);
    }

    protected void switchTab(IntBinaryOperator newIndexCalculator) {
        TabbedViewsContainer<?> tabbedContainer = getTabbedViewsContainer();
        int tabsNumber = Math.toIntExact(tabbedContainer.getTabsStream().count());
        if (tabsNumber <= 1
                || hasModalWindows()) {
            return;
        }

        int selectedIndex = tabbedContainer.getSelectedIndex();
        int newIndex = newIndexCalculator.applyAsInt(selectedIndex, tabsNumber);
        tabbedContainer.setSelectedIndex(newIndex);

        findViewInfo(tabbedContainer.getTabAt(newIndex))
                .ifPresent(viewInfo -> requestFocus(viewInfo.view()));
    }

    protected boolean hasModalWindows() {
        return getUI()
                .map(UI::hasModalComponent)
                .orElse(false);
    }

    protected void requestFocus(View<?> view) {
        UiComponentUtils.findFocusComponent(view)
                .ifPresent(focusable -> {
                    focusable.focus();
                    Element element = focusable.getElement();
                    element.executeJs("setTimeout(function(){$0.setAttribute('focus-ring', '')},0)", element);
                });
    }

    protected void onSelectedTabChanged(TabbedViewsContainer.SelectedChangeEvent<?> event) {
        Tab selectedTab = event.getSelectedTab();
        if (selectedTab == null) {
            return;
        }

        findViewInfo(selectedTab).ifPresent(currentViewInfo -> {
            updatePageTitle(currentViewInfo.view());
            updateUrl(currentViewInfo.location());
        });
    }

    protected Optional<ViewBreadcrumbs.ViewInfo> findViewInfo(Tab tab) {
        Component tabComponent = getTabbedViewsContainer().findComponent(tab).orElse(null);
        if (!(tabComponent instanceof ViewContainer viewContainer)) {
            return Optional.empty();
        }

        ViewBreadcrumbs breadcrumbs = viewContainer.getBreadcrumbs();
        return breadcrumbs != null
                ? Optional.ofNullable(breadcrumbs.getCurrentViewInfo())
                : Optional.empty();
    }

    protected void updateUrl(Location resolvedLocation) {
        UI ui = getUI().orElse(UI.getCurrent());
        routeSupport().setLocation(ui, resolvedLocation);
    }

    protected void updatePageTitle(View<?> view) {
        getUI().ifPresent(ui -> {
            Page page = ui.getPage();
            page.setTitle(ViewControllerUtils.getPageTitle(view));
        });
    }

    /**
     * Returns the initial layout of this {@link WorkArea}. The initial layout
     * is displayed when no tabs are opened in the {@link TabbedViewsContainer}.
     *
     * @return the initial layout instance
     * @throws IllegalStateException if the initial layout is not initialized
     */
    public VerticalLayout getInitialLayout() {
        checkState(initialLayout != null, "Initial layout is not initialized");
        return initialLayout;
    }

    /**
     * Sets the initial layout for this {@link WorkArea}. Initial layout is displayed
     * when no tabs are opened in the {@link TabbedViewsContainer}.
     *
     * @param initialLayout the new initial layout to be set
     * @throws IllegalArgumentException if {@code initialLayout} is null
     */
    public void setInitialLayout(VerticalLayout initialLayout) {
        Preconditions.checkNotNullArgument(initialLayout);

        if (this.initialLayout != null) {
            this.initialLayout.removeFromParent();
        }

        this.initialLayout = initialLayout;

        if (state == State.INITIAL_LAYOUT) {
            getElement().removeAllChildren();
            getElement().appendChild(initialLayout.getElement());
        }
    }

    /**
     * Returns the current {@link ViewContainer} instance associated with the selected tab
     * in the {@link TabbedViewsContainer}. If no tab is selected, returns {@code null}.
     * If the content of the selected tab is not a valid {@link ViewContainer}, an exception
     * is thrown.
     *
     * @return the current {@link ViewContainer} associated with the selected tab,
     * or {@code null} if no tab is selected
     * @throws IllegalStateException if the content of the selected tab is not an
     *                               instance of {@link ViewContainer}
     */
    @Nullable
    public ViewContainer getCurrentViewContainer() {
        TabbedViewsContainer<?> tabbedContainer = getTabbedViewsContainer();
        Tab selectedTab = tabbedContainer.getSelectedTab();
        if (selectedTab == null) {
            return null;
        }

        Component component = tabbedContainer.getComponent(selectedTab);
        if (component instanceof ViewContainer viewContainer) {
            return viewContainer;
        } else {
            throw new IllegalStateException("Tab content '%s' is not a %s"
                    .formatted(component, ViewContainer.class.getSimpleName()));
        }
    }

    /**
     * Returns the count of opened tabs within the {@link TabbedViewsContainer}.
     *
     * @return the total number of opened tabs
     */
    public int getOpenedTabCount() {
        return Math.toIntExact(getTabbedViewsContainer().getTabsStream().count());
    }

    @Override
    public Optional<Component> findOwnComponent(String id) {
        return getOwnComponents().stream()
                .filter(component -> sameId(component, id))
                .findAny();
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return List.of(((Component) getTabbedViewsContainer()), getInitialLayout());
    }

    /**
     * Adds a listener that will be notified when a work area state is changed.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    public Registration addStateChangeListener(ComponentEventListener<StateChangeEvent> listener) {
        return addListener(StateChangeEvent.class, listener);
    }

    /**
     * Event that is fired when {@link WorkArea} changed its state.
     */
    public static class StateChangeEvent extends ComponentEvent<WorkArea> {

        protected final State state;

        public StateChangeEvent(WorkArea source, State state) {
            super(source, false);
            this.state = state;
        }

        /**
         * Returns the current state of the {@link WorkArea}.
         *
         * @return the current {@link State} of the work area
         */
        public State getState() {
            return state;
        }
    }

    /**
     * {@link WorkArea} state.
     */
    public enum State {

        /**
         * {@link WorkArea} does not contain other views.
         */
        INITIAL_LAYOUT,

        /**
         * {@link WorkArea} contains at least one view.
         */
        VIEW_CONTAINER
    }

    protected RouteSupport routeSupport() {
        if (routeSupport == null) {
            routeSupport = applicationContext.getBean(RouteSupport.class);
        }

        return routeSupport;
    }

    protected ViewRegistry viewRegistry() {
        if (viewRegistry == null) {
            viewRegistry = applicationContext.getBean(ViewRegistry.class);
        }

        return viewRegistry;
    }
}
