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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.tabbedmode.action.tabsheet.CloseAllTabsAction;
import io.jmix.tabbedmode.action.tabsheet.CloseOthersTabsAction;
import io.jmix.tabbedmode.action.tabsheet.CloseThisTabAction;
import io.jmix.tabbedmode.component.breadcrumbs.ViewBreadcrumbs;
import io.jmix.tabbedmode.component.tabsheet.JmixMainTabSheet;
import io.jmix.tabbedmode.component.tabsheet.MainTabSheetUtils;
import io.jmix.tabbedmode.component.viewcontainer.TabViewContainer;
import io.jmix.tabbedmode.component.viewcontainer.ViewContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

// TODO: gg, create Web Component
@Tag(Tag.DIV)
public class AppWorkArea extends Component implements HasSize, ApplicationContextAware, InitializingBean {

    // TODO: gg, create Web Component
    public static final String WORK_AREA_CLASS_NAME = "jmix-app-workarea";

    // TODO: gg, replace with state attributes
    public static final String TABBED_CONTAINER_CLASS_NAME = "jmix-main-tabsheet";
    public static final String INITIAL_LAYOUT_CLASS_NAME = "jmix-initial-layout";

    protected Actions actions;
    protected RouteSupport routeSupport;
    protected UiComponents uiComponents;

    protected State state = State.INITIAL_LAYOUT;

    protected TabbedViewsContainer<?> tabbedViewsContainer;
    protected Component initialLayout;

    public AppWorkArea() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        uiComponents = applicationContext.getBean(UiComponents.class);
        routeSupport = applicationContext.getBean(RouteSupport.class);
        actions = applicationContext.getBean(Actions.class);
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    private void initComponent() {
        setClassName(WORK_AREA_CLASS_NAME);
        // TODO: gg, use beans
        tabbedViewsContainer = createTabbedViewsContainer();
        Component initialLayout = createInitialLayout();
        setInitialLayout(initialLayout);
    }

    protected TabbedViewsContainer<?> createTabbedViewsContainer() {
        JmixMainTabSheet tabSheet = uiComponents.create(JmixMainTabSheet.class);
        tabSheet.setSizeFull();
        tabSheet.setClassName(TABBED_CONTAINER_CLASS_NAME);

        tabSheet.addSelectedChangeListener(this::onSelectedTabChanged);

        // TODO: gg, provider like Generic Filter
        tabSheet.addAction(actions.create(CloseThisTabAction.ID));
        tabSheet.addAction(actions.create(CloseOthersTabsAction.ID));
        tabSheet.addAction(actions.create(CloseAllTabsAction.ID));

        // TODO: gg, init close handlers, etc.

        return tabSheet;
    }

    protected Component createInitialLayout() {
        return uiComponents.create(VerticalLayout.class);
    }

    public Component getInitialLayout() {
        return initialLayout;
    }

    public void setInitialLayout(Component initialLayout) {
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
        // TODO: gg, why?
        /*if (!event.isFromClient()) {
            return;
        }*/

        Tab selectedTab = event.getSelectedTab();
        if (selectedTab == null) {
            return;
        }

        // TODO: gg, get?
        TabViewContainer tabViewContainer = (TabViewContainer) tabbedViewsContainer.findComponent(selectedTab).orElse(null);
        if (tabViewContainer == null) {
            return;
        }

        ViewBreadcrumbs breadcrumbs = tabViewContainer.getBreadcrumbs();
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
                getElement().appendChild(tabbedViewsContainer.getElement());
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
        TabbedViewsContainer<?> tabbedViewsContainer = getTabbedViewsContainer();

        return tabbedViewsContainer.getTabComponentsStream()
                .flatMap(component -> {
                    ViewContainer viewContainer = MainTabSheetUtils.asViewContainer(component);
                    ViewBreadcrumbs breadcrumbs = viewContainer.getBreadcrumbs();
                    return breadcrumbs != null
                            ? breadcrumbs.getViews().stream()
                            : Stream.empty();
                }).toList();
    }

    public Collection<View<?>> getActiveWorkAreaViews() {
        TabbedViewsContainer<?> tabbedViewsContainer = getTabbedViewsContainer();
        return tabbedViewsContainer.getTabComponentsStream()
                .map(component -> {
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
                }).toList();
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

    // TODO: gg, interface?
    public TabbedViewsContainer<?> getTabbedViewsContainer() {
        return tabbedViewsContainer;
    }

    @Nullable
    public ViewContainer getCurrentViewContainer() {
        TabbedViewsContainer<?> tabbedViewsContainer = getTabbedViewsContainer();
        Tab selectedTab = tabbedViewsContainer.getSelectedTab();
        if (selectedTab == null) {
            return null;
        }

        Component component = tabbedViewsContainer.getComponent(selectedTab);
        if (component instanceof ViewContainer viewContainer) {
            return viewContainer;
        } else {
            throw new IllegalStateException("Tab content '%s' is not a %s"
                    .formatted(component, ViewContainer.class.getSimpleName()));
        }
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

    public int getOpenedTabCount() {
        return getTabbedViewsContainer().getTabs().size();
    }

    /**
     * Event that is fired when work area changed its state.
     */
    public static class StateChangeEvent extends ComponentEvent<AppWorkArea> {

        protected final State state;

        public StateChangeEvent(AppWorkArea source, State state) {
            // TODO: gg, from client?
            super(source, false);
            this.state = state;
        }

        public State getState() {
            return state;
        }
    }


    /**
     * Work area state
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

//    WorkAreaTabChangedEvent
}
