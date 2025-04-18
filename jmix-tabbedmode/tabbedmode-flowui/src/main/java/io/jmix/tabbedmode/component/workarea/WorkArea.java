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
     * @return a state
     */
    public State getState() {
        return state;
    }

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

    public Collection<View<?>> getActiveWorkAreaViews() {
        TabbedViewsContainer<?> tabbedContainer = getTabbedViewsContainer();
        return tabbedContainer.getTabComponentsStream()
                .map(this::getViewFromContent)
                .collect(Collectors.toList());
    }

    protected View<?> getViewFromContent(Component component) {
        ViewContainer viewContainer = MainTabSheetUtils.asViewContainer(component);
        ViewBreadcrumbs breadcrumbs = viewContainer.getBreadcrumbs();
        if (breadcrumbs != null) {
            ViewBreadcrumbs.ViewInfo viewInfo = breadcrumbs.getCurrentViewInfo();
            if (viewInfo != null) {
                return viewInfo.view();
            } else {
                throw new IllegalStateException("Tab does not contain a %s"
                        .formatted(View.class.getSimpleName()));
            }
        } else if (viewContainer.getView() != null) {
            return viewContainer.getView();
        } else {
            throw new IllegalStateException("Tab does not contain a %s"
                    .formatted(View.class.getSimpleName()));
        }
    }

    public Collection<View<?>> getCurrentBreadcrumbs() {
        ViewContainer viewContainer = getCurrentViewContainer();
        if (viewContainer == null) {
            return Collections.emptyList();
        }

        ViewBreadcrumbs breadcrumbs = viewContainer.getBreadcrumbs();
        if (breadcrumbs == null) {
            return Collections.emptyList();
        }

        List<View<?>> views = new ArrayList<>(breadcrumbs.getViews().size());
        breadcrumbs.getViews().descendingIterator().forEachRemaining(views::add);

        return views;
    }

    public TabbedViewsContainer<?> getTabbedViewsContainer() {
        checkState(tabbedContainer != null, "%s is not initialized"
                .formatted(TabbedViewsContainer.class.getSimpleName()));

        return tabbedContainer;
    }

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
        int tabsNumber = tabbedContainer.getTabs().size();
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
        getUI().ifPresent(ui ->
                routeSupport().setLocation(ui, resolvedLocation));
    }

    protected void updatePageTitle(View<?> view) {
        getUI().ifPresent(ui -> {
            Page page = ui.getPage();
            page.setTitle(ViewControllerUtils.getPageTitle(view));
        });
    }

    public VerticalLayout getInitialLayout() {
        checkState(initialLayout != null, "Initial layout is not initialized");
        return initialLayout;
    }

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

    public int getOpenedTabCount() {
        return getTabbedViewsContainer().getTabs().size();
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
     * Event that is fired when work area changed its state.
     */
    public static class StateChangeEvent extends ComponentEvent<WorkArea> {

        protected final State state;

        public StateChangeEvent(WorkArea source, State state) {
            super(source, false);
            this.state = state;
        }

        public State getState() {
            return state;
        }
    }

    /**
     * App Work Area state.
     */
    public enum State {

        /**
         * If the work area is in the INITIAL_LAYOUT state, the work area does not contain other screens.
         */
        INITIAL_LAYOUT,

        /**
         * If the work area is in the WINDOW_CONTAINER state, the work area contains at least one screen.
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
