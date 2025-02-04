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
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.tabbedmode.component.breadcrumbs.ViewBreadcrumbs;
import io.jmix.tabbedmode.component.tabsheet.JmixMainTabSheet;
import io.jmix.tabbedmode.component.tabsheet.TabViewContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

// TODO: gg, create Web Component
@Tag(Tag.DIV)
public class AppWorkArea extends Component implements HasSize, ApplicationContextAware, InitializingBean {

    // TODO: gg, create Web Component
    public static final String WORK_AREA_CLASS_NAME = "jmix-app-workarea";

    // TODO: gg, replace with state attributes
    public static final String TABBED_CONTAINER_CLASS_NAME = "jmix-main-tabsheet";
    public static final String INITIAL_LAYOUT_CLASS_NAME = "jmix-initial-layout";

    protected RouteSupport routeSupport;
    protected UiComponents uiComponents;

    protected State state = State.INITIAL_LAYOUT;

    protected JmixMainTabSheet mainTabSheet;
    protected Component initialLayout;

    public AppWorkArea() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        uiComponents = applicationContext.getBean(UiComponents.class);
        routeSupport = applicationContext.getBean(RouteSupport.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initComponent();
    }

    private void initComponent() {
        setClassName(WORK_AREA_CLASS_NAME);
        // TODO: gg, use beans
        mainTabSheet = createMainTabSheet();
        Component initialLayout = createInitialLayout();
        setInitialLayout(initialLayout);
    }

    protected JmixMainTabSheet createMainTabSheet() {
        JmixMainTabSheet tabSheet = uiComponents.create(JmixMainTabSheet.class);
        tabSheet.setSizeFull();
        tabSheet.setClassName(TABBED_CONTAINER_CLASS_NAME);

        tabSheet.addSelectedChangeListener(this::onSelectedTabChanged);

        // TODO: gg, init close handlers, etc.

        return tabSheet;
    }

    protected Component createInitialLayout() {
        return new VerticalLayout();
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

    protected void onSelectedTabChanged(JmixTabSheet.SelectedChangeEvent event) {
        // TODO: gg, why?
        /*if (!event.isFromClient()) {
            return;
        }*/

        Tab selectedTab = event.getSelectedTab();
        if (selectedTab == null) {
            return;
        }

        TabViewContainer tabViewContainer = (TabViewContainer) mainTabSheet.getContentByTab(selectedTab);
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
    State getState() {
        return state;
    }

    @Internal
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
                getElement().appendChild(mainTabSheet.getElement());
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

    Stream<View<?>> getOpenedWorkAreaScreensStream() {
        // TODO: gg, implement
        return Stream.empty();
    }

    /**
     * Returns all active screens that are inside the work area.
     *
     * @return active screens stream
     **/
    Stream<View<?>> getActiveWorkAreaScreensStream() {
        // TODO: gg, implement
        return Stream.empty();
    }


    Collection<View<?>> getCurrentBreadcrumbs() {
        // TODO: gg, implement
        return Collections.emptyList();
    }

    /**
     * Adds a listener that will be notified when a work area state is changed.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Registration addStateChangeListener(ComponentEventListener<StateChangeEvent> listener) {
        return addListener(StateChangeEvent.class, listener);
    }

    // TODO: gg, interface?
    public JmixMainTabSheet getTabbedWindowContainer() {
        return mainTabSheet;
    }

    public int getOpenedTabCount() {
        return getTabbedWindowContainer().getTabs().size();
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
