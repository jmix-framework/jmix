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
import com.vaadin.flow.router.Location;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.ComponentContainer;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.tabbedmode.component.breadcrumbs.ViewBreadcrumbs;
import io.jmix.tabbedmode.component.tabsheet.JmixMainTabSheet;
import io.jmix.tabbedmode.component.tabsheet.MainTabSheetUtils;
import io.jmix.tabbedmode.component.viewcontainer.ViewContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

@Tag("jmix-work-area")
@JsModule("./src/workarea/jmix-work-area.js")
public class WorkArea extends Component implements HasSize, ComponentContainer, ApplicationContextAware, InitializingBean {

    public static final String TABBED_CONTAINER_CLASS_NAME = "jmix-main-tabsheet";
    public static final String INITIAL_LAYOUT_CLASS_NAME = "jmix-initial-layout";

    protected RouteSupport routeSupport;
    protected UiComponents uiComponents;
    protected WorkAreaSupport workAreaSupport;

    protected State state = State.INITIAL_LAYOUT;

    protected TabbedViewsContainer<?> tabbedContainer;
    protected VerticalLayout initialLayout;

    public WorkArea() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        uiComponents = applicationContext.getBean(UiComponents.class);
        routeSupport = applicationContext.getBean(RouteSupport.class);
        workAreaSupport = applicationContext.getBean(WorkAreaSupport.class);
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    private void initComponent() {
        tabbedContainer = createTabbedViewsContainer();
    }

    protected TabbedViewsContainer<?> createTabbedViewsContainer() {
        JmixMainTabSheet tabSheet = uiComponents.create(JmixMainTabSheet.class);
        tabSheet.setSizeFull();
        tabSheet.setClassName(TABBED_CONTAINER_CLASS_NAME);

        tabSheet.addSelectedChangeListener(this::onSelectedTabChanged);

        workAreaSupport.getDefaultActions()
                .forEach(tabSheet::addAction);

        return tabSheet;
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

        initialLayout.addClassName(INITIAL_LAYOUT_CLASS_NAME);

        if (state == State.INITIAL_LAYOUT) {
            getElement().removeAllChildren();
            getElement().appendChild(initialLayout.getElement());
        }
    }

    protected void onSelectedTabChanged(TabbedViewsContainer.SelectedChangeEvent<?> event) {
        Tab selectedTab = event.getSelectedTab();
        if (selectedTab == null) {
            return;
        }

        Component tabComponent = tabbedContainer.findComponent(selectedTab).orElse(null);
        if (!(tabComponent instanceof ViewContainer viewContainer)) {
            return;
        }

        ViewBreadcrumbs breadcrumbs = viewContainer.getBreadcrumbs();
        if (breadcrumbs != null && breadcrumbs.getCurrentViewInfo() != null) {
            ViewBreadcrumbs.ViewInfo currentViewInfo = breadcrumbs.getCurrentViewInfo();
            updatePageTitle(currentViewInfo.view());
            // TODO: gg, update tab title here?

            if (currentViewInfo.location() != null) {
                updateUrl(currentViewInfo.location());
            }
        }
    }

    protected void updateUrl(Location resolvedLocation) {
        getUI().ifPresent(ui ->
                routeSupport.setLocation(ui, resolvedLocation));
    }

    protected void updatePageTitle(View<?> view) {
        getUI().ifPresent(ui -> {
            Page page = ui.getPage();
            page.setTitle(ViewControllerUtils.getPageTitle(view));
        });
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

        // TODO: gg, implement
        // init global tab shortcuts
        /*if (!this.shortcutsInitialized
                && getState() == State.WINDOW_CONTAINER) {
            initTabShortcuts();

            this.shortcutsInitialized = true;
        }*/

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
        return tabbedContainer;
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
        return Optional.ofNullable(state == State.INITIAL_LAYOUT
                && initialLayout != null
                && UiComponentUtils.sameId(initialLayout, id)
                ? initialLayout
                : null);
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return state == State.INITIAL_LAYOUT && initialLayout != null
                ? Collections.singleton(initialLayout)
                : Collections.emptyList();
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
}
