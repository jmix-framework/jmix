/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.widget;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.jmix.ui.AppUI;
import io.jmix.ui.widget.addon.dragdroplayouts.DDTabSheet;
import io.jmix.ui.widget.client.tabsheet.ClientAction;
import io.jmix.ui.widget.client.tabsheet.JmixMainTabSheetClientRpc;
import io.jmix.ui.widget.client.tabsheet.JmixMainTabSheetServerRpc;
import io.jmix.ui.widget.client.tabsheet.JmixMainTabSheetState;
import com.vaadin.event.Action;
import com.vaadin.server.KeyMapper;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.TabSheet;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class JmixMainTabSheet extends DDTabSheet implements Action.Container, HasTabSheetBehaviour {

    private static final long serialVersionUID = -2956008661221108673L;

    protected Stack<Component> openedComponents = new Stack<>();

    protected Set<Action.Handler> actionHandlers = null; // lazily initialized

    protected KeyMapper<Action> actionMapper = null;

    protected Map<Component, TabCloseHandler> closeHandlers = null;

    protected BiMap<String, TabSheet.Tab> tabIds = HashBiMap.create();

    protected TabSheetBehaviour behaviour;

    protected Consumer<ComponentContainer> closeOthersHandler;

    protected Consumer<ComponentContainer> closeAllTabsHandler;

    protected JmixMainTabSheetServerRpc rpc = new JmixMainTabSheetServerRpc() {
        @Override
        public void onTabContextMenu(int tabIndex) {
            TabSheet.Tab tab = getTab(tabIndex);
            if (tab != null) {
                Set<Action> actions = getActions(JmixMainTabSheet.this.getActionTarget(tab));

                if (!actions.isEmpty()) {
                    actionMapper = new KeyMapper<>();

                    List<ClientAction> actionsList = new ArrayList<>(actions.size());
                    for (Action action : actions) {
                        ClientAction clientAction = new ClientAction(action.getCaption());
                        clientAction.setActionId(actionMapper.key(action));
                        actionsList.add(clientAction);
                    }

                    ClientAction[] clientActions = actionsList.toArray(new ClientAction[actions.size()]);

                    getRpcProxy(JmixMainTabSheetClientRpc.class).showTabContextMenu(tabIndex, clientActions);
                }
            }
        }

        @Override
        public void performAction(int tabIndex, String actionKey) {
            TabSheet.Tab tab = getTab(tabIndex);
            if (tab != null) {
                if (actionMapper != null) {
                    Action action = actionMapper.get(actionKey);
                    Action.Handler[] handlers;

                    if (actionHandlers != null) {
                        handlers = actionHandlers.toArray(new Action.Handler[0]);
                    } else {
                        handlers = new Action.Handler[0];
                    }

                    for (Action.Handler handler : handlers) {
                        handler.handleAction(action, this, JmixMainTabSheet.this.getActionTarget(tab));
                    }

                    // forget all painted actions after perform one
                    actionMapper = null;
                }
            }
        }
    };

    protected Component getActionTarget(TabSheet.Tab tab) {
        return tab.getComponent();
    }

    public JmixMainTabSheet() {
        registerRpc(rpc);

        setShim(false);

        setCloseHandler((tabsheet, tabContent) -> {
            AppUI ui = (AppUI) tabsheet.getUI();
            if (!ui.isAccessibleForUser(this)) {
                LoggerFactory.getLogger(JmixMainTabSheet.class)
                        .debug("Ignore close tab attempt because tab is inaccessible for user");
                return;
            }

            if (closeHandlers != null) {
                TabCloseHandler closeHandler = closeHandlers.get(tabContent);
                if (closeHandler != null) {
                    closeHandler.onClose(JmixMainTabSheet.this, tabContent);
                }
            }
        });

        behaviour = new TabSheetBehaviourImpl(this);
    }

    protected Set<Action> getActions(Component actionTarget) {
        Set<Action> actions = new LinkedHashSet<>();
        if (actionHandlers != null) {
            for (Action.Handler handler : actionHandlers) {
                Action[] as = handler.getActions(actionTarget, this);
                if (as != null) {
                    Collections.addAll(actions, as);
                }
            }
        }
        return actions;
    }

    @Override
    public JmixMainTabSheetState getState() {
        return (JmixMainTabSheetState) super.getState();
    }

    @Override
    protected JmixMainTabSheetState getState(boolean markAsDirty) {
        return (JmixMainTabSheetState) super.getState(markAsDirty);
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        getState().hasActionsHandlers = actionHandlers != null && !actionHandlers.isEmpty();
    }

    @Nullable
    public Component getPreviousTab(Component tab) {
        if ((!openedComponents.empty()) && (getSelectedTab().equals(tab))) {
            Component c = openedComponents.pop();
            while (!_components().contains(c) && !openedComponents.isEmpty()) {
                c = openedComponents.pop();
            }
            return c;
        }
        return null;
    }

    public void silentCloseTabAndSelectPrevious(Component tab) {
        while (openedComponents.removeElement(tab)) {
            openedComponents.removeElement(tab);
        }
        if ((!openedComponents.empty()) && (_selected().equals(tab))) {
            Component c = openedComponents.pop();
            while (!_components().contains(c) && !openedComponents.isEmpty()) {
                c = openedComponents.pop();
            }
            setSelectedTab(c);
        }
    }

    @Override
    public void setSelectedTab(Component c, boolean userOriginated) {
        if (c != null && _components().contains(c) && !c.equals(_selected())) {
            openedComponents.push(c);
            super.setSelectedTab(c, userOriginated);
        }
    }

    @Override
    public void removeTab(TabSheet.Tab tab) {
        super.removeTab(tab);

        //noinspection StatementWithEmptyBody
        while (openedComponents.removeElement(tab)) {
        }
    }

    public void closeTab(Component tab) {
        if (_closeHandler() != null) {
            _closeHandler().onTabClose(this, tab);
        }
    }

    public void setTestId(TabSheet.Tab tab, String testId) {
        int tabPosition = getTabPosition(tab);
        getState(true).tabs.get(tabPosition).id = testId;
    }

    public void setJTestId(TabSheet.Tab tab, String id) {
        int tabPosition = getTabPosition(tab);
        getState(true).tabs.get(tabPosition).jTestId = id;
    }

    public String getJTestId(TabSheet.Tab tab) {
        int tabPosition = getTabPosition(tab);
        return getState(true).tabs.get(tabPosition).jTestId;
    }

    @Override
    public void removeComponent(Component c) {
        TabSheet.Tab tab = _tabs().get(c);

        super.removeComponent(c);

        tabIds.inverse().remove(tab);

        if (c != null && closeHandlers != null) {
            closeHandlers.remove(c);
            if (closeHandlers.isEmpty()) {
                closeHandlers = null;
            }
        }
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
        TabSheet.Tab oldTab = _tabs().get(oldComponent);

        super.replaceComponent(oldComponent, newComponent);

        TabSheet.Tab newTab = _tabs().get(newComponent);

        String oldTabId = tabIds.inverse().get(oldTab);
        String newTabId = tabIds.inverse().get(newTab);

        if (oldTabId != null) {
            tabIds.remove(oldTabId);
            if (newTab != null) {
                tabIds.put(oldTabId, newTab);
            }
        }
        if (newTabId != null) {
            tabIds.remove(newTabId);
            if (oldTab != null) {
                tabIds.put(newTabId, oldTab);
            }
        }
    }

    @Override
    public void addActionHandler(Action.Handler actionHandler) {
        if (actionHandlers == null) {
            actionHandlers = new LinkedHashSet<>();
        }
        actionHandlers.add(actionHandler);
    }

    @Override
    public void removeActionHandler(Action.Handler actionHandler) {
        if (actionHandlers != null) {
            actionHandlers.remove(actionHandler);
        }
    }

    public void moveTab(Component c, int position) {
        TabSheet.Tab oldTab = getTab(c);
        String tabId = tabIds.inverse().get(oldTab);

        String tabJmixId = getJTestId(oldTab);

        // do not detach close handler
        // call super
        super.removeComponent(oldTab.getComponent());

        TabSheet.Tab newTab = addTab(c, position);

        newTab.setCaption(oldTab.getCaption());
        newTab.setDescription(oldTab.getDescription());
        newTab.setClosable(oldTab.isClosable());
        newTab.setEnabled(oldTab.isEnabled());
        newTab.setVisible(oldTab.isVisible());
        newTab.setIcon(oldTab.getIcon());
        newTab.setStyleName(oldTab.getStyleName());

        setJTestId(newTab, tabJmixId);

        if (tabId != null) {
            tabIds.remove(tabId);
            tabIds.put(tabId, newTab);
        }
    }

    public void setTabCloseHandler(Component tabContent, TabCloseHandler closeHandler) {
        if (closeHandlers == null) {
            closeHandlers = new LinkedHashMap<>();
        }
        closeHandlers.put(tabContent, closeHandler);
    }

    @Override
    public TabSheetBehaviour getTabSheetBehaviour() {
        return behaviour;
    }

    public interface TabCloseHandler {
        void onClose(JmixMainTabSheet tabSheet, Component tabContent);
    }

    public Consumer<ComponentContainer> getCloseOthersHandler() {
        return closeOthersHandler;
    }

    public void setCloseOthersHandler(Consumer<ComponentContainer> closeOthersHandler) {
        this.closeOthersHandler = closeOthersHandler;
    }

    public Consumer<ComponentContainer> getCloseAllTabsHandler() {
        return closeAllTabsHandler;
    }

    public void setCloseAllTabsHandler(Consumer<ComponentContainer> closeAllTabsHandler) {
        this.closeAllTabsHandler = closeAllTabsHandler;
    }

    public void closeOtherTabs(Component currentTab) {
        if (closeOthersHandler == null) {
            throw new IllegalStateException("JmixTabSheet closeOthersHandler is not set");
        }

        closeOthersHandler.accept((ComponentContainer) currentTab);
    }

    public void closeAllTabs() {
        if (closeAllTabsHandler == null) {
            throw new IllegalStateException("JmixTabSheet closeAllTabs is not set");
        }

        closeAllTabsHandler.accept(null);
    }

    /**
     * Important! If Tab in TabSheet contains components that provide drag and drop features, users may face UI freezes
     * in case of dragging elements to TabSheet or Tab layout.
     *
     * @param enable enable html5 drag and drop
     */
    public void setDDHtml5Enabled(boolean enable) {
        getState().ddHtmlEnable = enable;
    }

    /**
     * @return true if html5 drag and drop is enabled
     */
    public boolean isDDHtml5Enabled() {
        return getState(false).ddHtmlEnable;
    }

    protected static class TabSheetBehaviourImpl implements TabSheetBehaviour {

        protected final JmixMainTabSheet tabSheet;

        public TabSheetBehaviourImpl(JmixMainTabSheet tabSheet) {
            this.tabSheet = tabSheet;
        }

        @Override
        public void setTabCaption(String tabId, String caption) {
            getTabNN(tabId).setCaption(caption);
        }

        @Override
        public void setTabDescription(String tabId, @Nullable String description) {
            getTabNN(tabId).setDescription(description);
        }

        @Override
        public void closeTab(Component target) {
            AppUI ui = (AppUI) tabSheet.getUI();
            if (!ui.isAccessibleForUser(tabSheet)) {
                LoggerFactory.getLogger(JmixMainTabSheet.class)
                        .debug("Ignore close tab attempt because TabSheet is inaccessible for user");
                return;
            }

            tabSheet.closeTab(target);
        }

        @Override
        public void closeOtherTabs(Component target) {
            AppUI ui = (AppUI) tabSheet.getUI();
            if (!ui.isAccessibleForUser(tabSheet)) {
                LoggerFactory.getLogger(JmixMainTabSheet.class)
                        .debug("Ignore close tab attempt because TabSheet is inaccessible for user");
                return;
            }

            tabSheet.closeOtherTabs(target);
        }

        @Override
        public void closeAllTabs() {
            AppUI ui = (AppUI) tabSheet.getUI();
            if (!ui.isAccessibleForUser(tabSheet)) {
                LoggerFactory.getLogger(JmixMainTabSheet.class)
                        .debug("Ignore close tab attempt because TabSheet is inaccessible for user");
                return;
            }

            tabSheet.closeAllTabs();
        }

        @Override
        public Component getTabComponent(String tabId) {
            return getTabNN(tabId).getComponent();
        }

        @Override
        public void setTabIcon(String tabId, @Nullable Resource icon) {
            getTabNN(tabId).setIcon(icon);
        }

        @Override
        public void setTabClosable(String tabId, boolean closable) {
            getTabNN(tabId).setClosable(closable);
        }

        protected TabSheet.Tab getTabNN(String tabId) {
            TabSheet.Tab tab = tabSheet.tabIds.get(tabId);
            if (tab == null) {
                throw new IllegalStateException("TabSheet does not contain tab with id: " + tabId);
            }
            return tab;
        }

        // just stub
        @Override
        public void setContentSwitchMode(String tabId, ContentSwitchMode contentSwitchMode) {
        }

        @Override
        public void setSelectedTab(String tabId) {
            tabSheet.setSelectedTab(getTabNN(tabId));
        }

        @Override
        public void addTab(Component component, String tabId) {
            TabSheet.Tab tab = tabSheet.addTab(component);

            tabSheet.tabIds.put(tabId, tab);
        }

        @Override
        public String getTab(Component component) {
            TabSheet.Tab tab = tabSheet.getTab(component);
            return tabSheet.tabIds.inverse().get(tab);
        }

        @Override
        public String getTab(int position) {
            TabSheet.Tab tab = tabSheet.getTab(position);
            return tabSheet.tabIds.inverse().get(tab);
        }

        @Nullable
        @Override
        public Component getSelectedTab() {
            return tabSheet.getSelectedTab();
        }

        @Override
        public Iterator<Component> getTabComponents() {
            return tabSheet.iterator();
        }

        @Override
        public Stream<Component> getTabComponentsStream() {
            return tabSheet._components().stream();
        }

        @Override
        public void setSelectedTab(Component component) {
            tabSheet.setSelectedTab(component);
        }

        @Override
        public void replaceComponent(Component oldComponent, Component newComponent) {
            tabSheet.replaceComponent(oldComponent, newComponent);
        }

        @Override
        public void removeComponent(Component component) {
            tabSheet.removeComponent(component);
        }

        @Nullable
        @Override
        public Component getPreviousTab(Component tab) {
            return tabSheet.getPreviousTab(tab);
        }

        @Override
        public void setTabTestId(String tabId, String testId) {
            tabSheet.setTestId(getTabNN(tabId), testId);
        }

        @Override
        public void setTabJmixId(String tabId, String id) {
            tabSheet.setJTestId(getTabNN(tabId), id);
        }

        @Override
        public void setTabCloseHandler(Component tabContent, BiConsumer<HasTabSheetBehaviour, Component> closeHandler) {
            tabSheet.setTabCloseHandler(tabContent, closeHandler::accept);
        }

        @Override
        public int getTabPosition(String tabId) {
            return tabSheet.getTabPosition(getTabNN(tabId));
        }

        @Override
        public int getComponentCount() {
            return tabSheet.getComponentCount();
        }

        @Override
        public void moveTab(Component c, int position) {
            tabSheet.moveTab(c, position);
        }

        @Override
        public void focus() {
            tabSheet.focus();
        }

        @Override
        public void silentCloseTabAndSelectPrevious(Component tab) {
            tabSheet.silentCloseTabAndSelectPrevious(tab);
        }
    }
}
