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
import com.vaadin.event.SerializableEventListener;
import io.jmix.ui.AppUI;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.LayoutDragMode;
import com.vaadin.event.Action;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;
import com.vaadin.v7.event.FieldEvents;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.jmix.ui.widget.ContentSwitchMode.HIDE;
import static io.jmix.ui.widget.ContentSwitchMode.UNLOAD;

public class JmixManagedTabSheet extends JmixTabSheetCssLayout
        implements Component.Focusable, FieldEvents.FocusNotifier, FieldEvents.BlurNotifier, HasTabSheetBehaviour {

    private static final long serialVersionUID = 1425920612516520121L;

    protected static final String MANAGED_TABSHEET_STYLENAME = "jmix-managed-tabsheet";
    protected static final String HIDDEN_TAB = "hidden-tab";
    protected static final String VISIBLE_TAB = "visible-tab";

    protected static final Method SELECTED_TAB_CHANGE_METHOD;

    protected Mode tabSheetMode;

    static {
        try {
            SELECTED_TAB_CHANGE_METHOD = SelectedTabChangeListener.class.getDeclaredMethod("selectedTabChange", SelectedTabChangeEvent.class);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException("Internal error finding methods in JmixManagedTabSheet");
        }
    }

    protected final JmixMainTabSheet tabbedHeader;

    protected final TabSheetBehaviour behaviour;

    // tabbedHeader Label to TabImpl
    protected final BiMap<Component, Component> tabToContentMap = HashBiMap.create();

    // Content CssLayout to TabImpl
    protected final Map<Component, Tab> tabs = new HashMap<>();

    protected final Map<String, Tab> tabIds = new HashMap<>();

    protected final List<Component> tabComponents = new ArrayList<>();

    protected Component selected = null;

    protected CloseHandler closeHandler;

    public JmixManagedTabSheet() {
        setPrimaryStyleName(MANAGED_TABSHEET_STYLENAME);
        setSizeFull();

        closeHandler = (tabSheet1, tabContent) ->
                _closeTab(tabContent);

        tabbedHeader = new JmixMainTabSheet() {
            @Override
            protected Component getActionTarget(TabSheet.Tab tab) {
                return getContentTab(tab.getComponent()).getComponent();
            }

            @Override
            public void setTabPosition(TabSheet.Tab tab, int position) {
                super.setTabPosition(tab, position);

                TabImpl tabImpl = (TabImpl) tabToContentMap.get(tab.getComponent());
                if (tabImpl != null) {
                    Component tabComponent = tabImpl.getComponent();

                    tabComponents.remove(tabComponent);
                    tabComponents.add(position, tabComponent);
                }
            }
        };
        tabbedHeader.setVisible(false);
        tabbedHeader.addStyleName("framed padded-tabbar");

        tabbedHeader.setCloseHandler((tabsheet, tabContent) -> {
            AppUI ui = (AppUI) tabbedHeader.getUI();
            if (!ui.isAccessibleForUser(tabbedHeader)) {
                LoggerFactory.getLogger(JmixTabSheet.class)
                        .debug("Ignore close tab attempt because TabSheet is inaccessible for user");
                return;
            }

            closeHandler.onTabClose(JmixManagedTabSheet.this, getContentTab(tabContent).getComponent());
        });

        tabbedHeader.addSelectedTabChangeListener(event -> {
            setSelected(tabToContentMap.get(tabbedHeader.getSelectedTab()));
            fireSelectedTabChange(event.isUserOriginated());
        });
        addComponent(tabbedHeader);

        behaviour = new TabSheetBehaviourImpl(this);
    }

    public Mode getMode() {
        return tabSheetMode;
    }

    public void setMode(Mode tabSheetMode) {
        this.tabSheetMode = tabSheetMode;
    }

    @Override
    public void removeComponent(Component c) {
        Tab tab = tabs.get(c);

        //noinspection RedundantCast
        Component tabComponent = getTabComponent((Component) tab);
        tabToContentMap.inverse().remove(tab);

        tabIds.remove(tab.getId());
        tabs.remove(c);

        tabbedHeader.removeComponent(tabComponent);

        tabComponents.remove(c);

        ((TabImpl) tab).setCloseHandler(null);

        super.removeComponent(((TabImpl) tab));
    }

    @Nullable
    protected TabImpl getContentTab(Component tabContent) {
        return (TabImpl) tabToContentMap.get(tabContent);
    }

    protected Component getTabComponent(Component contentComponent) {
        return tabToContentMap.inverse().get(contentComponent);
    }

    protected void _closeTab(Component tabContent) {
        CloseHandler closeHandler = ((TabImpl) tabs.get(tabContent)).getCloseHandler();
        if (closeHandler != null) {
            closeHandler.onTabClose(this, tabContent);
        }
    }

    @Override
    public TabSheetBehaviour getTabSheetBehaviour() {
        return behaviour;
    }

    public Tab addTab(Component c, @Nullable String caption, @Nullable Resource icon) {
        return addTab(c, caption, icon, tabComponents.size());
    }

    public Tab addTab(Component tabComponent, @Nullable String caption, @Nullable Resource icon, int position) {

        TabImpl tab = new TabImpl(tabComponent);

        Label tabbarTabComponent = new Label();
        tabToContentMap.put(tabbarTabComponent, tab);
        tabs.put(tabComponent, tab);

        TabSheet.Tab tabbarTab = tabbedHeader.addTab(tabbarTabComponent, caption, icon, position);
        tab.setTabbarTab(tabbarTab);

        addComponent(tab);

        tabbedHeader.setVisible(true);

        if (tabComponents.isEmpty())
            setSelected(tab);

        tabComponents.add(position, tabComponent);

        return tab;
    }

    protected void setSelected(@Nullable Component component) {
        if (component == null || component == selected)
            return;

        if (selected != null)
            unselectTab(selected);

        selected = component;

        selectTab(component);

        tabbedHeader.setSelectedTab(getTabComponent(component));
    }

    protected void unselectTab(Component component) {
        ContentSwitchMode switchMode = ((TabImpl) component).getContentSwitchMode();

        if (switchMode == ContentSwitchMode.HIDE) {
            hideTabContent(component);
            return;
        } else if (switchMode == ContentSwitchMode.UNLOAD) {
            unloadTabContent(component);
            return;
        }

        if (tabSheetMode == Mode.HIDE_TABS) {
            hideTabContent(component);
        } else {
            unloadTabContent(component);
        }
    }

    protected void unloadTabContent(Component component) {
        super.removeComponent(component);
    }

    protected void hideTabContent(Component component) {
        component.removeStyleName(VISIBLE_TAB);
        component.addStyleName(HIDDEN_TAB);
    }

    protected void selectTab(Component component) {
        ContentSwitchMode contentSwitchMode = ((TabImpl) component).getContentSwitchMode();

        if (contentSwitchMode == HIDE) {
            showTabContent(component);
            return;
        } else if (contentSwitchMode == UNLOAD) {
            loadTabContent(component);
            return;
        }

        if (tabSheetMode == Mode.HIDE_TABS) {
            showTabContent(component);
        } else {
            loadTabContent(component);
        }
    }

    protected void loadTabContent(Component component) {
        // in case of new tab it will be added later
        if (!components.contains(component) &&
                tabComponents.contains(((TabImpl) component).getComponent())) {
            addComponent(component);
        }
    }

    protected void showTabContent(Component component) {
        component.removeStyleName(HIDDEN_TAB);
        component.addStyleName(VISIBLE_TAB);
    }

    public void setSelectedTab(@Nullable Tab tab) {
        if (tab != null) {
            setSelected((Component) tab);
        }
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
        Tab tab = tabs.get(oldComponent);
        if (tab != null) {
            ((TabImpl) tab).setComponent(newComponent);
        }
    }

    public void closeTab(Component tab) {
        if (closeHandler != null) {
            closeHandler.onTabClose(this, tab);
        }
    }

    public Consumer<ComponentContainer> getCloseOthersHandler() {
        return tabbedHeader.getCloseOthersHandler();
    }

    public void setCloseOthersHandler(Consumer<ComponentContainer> closeOthersHandler) {
        tabbedHeader.setCloseOthersHandler(closeOthersHandler);
    }

    public Consumer<ComponentContainer> getCloseAllTabsHandler() {
        return tabbedHeader.getCloseAllTabsHandler();
    }

    public void setCloseAllTabsHandler(Consumer<ComponentContainer> closeAllTabsHandler) {
        tabbedHeader.setCloseAllTabsHandler(closeAllTabsHandler);
    }

    public void closeOtherTabs(Component currentTab) {
        tabbedHeader.closeOtherTabs(currentTab);
    }

    public void closeAllTabs() {
        tabbedHeader.closeAllTabs();
    }

    @Override
    public void setStyleName(String style) {
        tabbedHeader.addStyleName(style);
    }

    public void setDragMode(LayoutDragMode mode) {
        tabbedHeader.setDragMode(mode);
    }

    public void setDropHandler(DropHandler dropHandler) {
        tabbedHeader.setDropHandler(dropHandler);
    }

    public void addActionHandler(Action.Handler actionHandler) {
        tabbedHeader.addActionHandler(actionHandler);
    }

    public static class SelectedTabChangeEvent extends Component.Event {

        protected boolean userOriginated;

        public SelectedTabChangeEvent(Component source, boolean userOriginated) {
            super(source);
            this.userOriginated = userOriginated;
        }

        public JmixManagedTabSheet getTabSheet() {
            return (JmixManagedTabSheet) getSource();
        }

        public boolean isUserOriginated() {
            return userOriginated;
        }
    }

    public interface SelectedTabChangeListener extends SerializableEventListener {

        void selectedTabChange(JmixManagedTabSheet.SelectedTabChangeEvent event);
    }

    public void addSelectedTabChangeListener(JmixManagedTabSheet.SelectedTabChangeListener listener) {
        addListener(JmixManagedTabSheet.SelectedTabChangeEvent.class, listener, SELECTED_TAB_CHANGE_METHOD);
    }

    @Deprecated
    public void addListener(JmixManagedTabSheet.SelectedTabChangeListener listener) {
        addSelectedTabChangeListener(listener);
    }

    public void removeSelectedTabChangeListener(JmixManagedTabSheet.SelectedTabChangeListener listener) {
        removeListener(JmixManagedTabSheet.SelectedTabChangeEvent.class, listener, SELECTED_TAB_CHANGE_METHOD);
    }

    @Deprecated
    public void removeListener(JmixManagedTabSheet.SelectedTabChangeListener listener) {
        removeSelectedTabChangeListener(listener);
    }

    protected void fireSelectedTabChange(boolean userOriginated) {
        fireEvent(new JmixManagedTabSheet.SelectedTabChangeEvent(this, userOriginated));
    }

    public interface CloseHandler extends Serializable {
        void onTabClose(JmixManagedTabSheet tabSheet, Component tabContent);
    }

    @Override
    public void addBlurListener(com.vaadin.event.FieldEvents.BlurListener listener) {
        addListener(com.vaadin.event.FieldEvents.BlurEvent.EVENT_ID,
                com.vaadin.event.FieldEvents.BlurEvent.class, listener,
                com.vaadin.event.FieldEvents.BlurListener.blurMethod);
    }

    @Override
    public void removeBlurListener(com.vaadin.event.FieldEvents.BlurListener listener) {
        removeListener(com.vaadin.event.FieldEvents.BlurEvent.EVENT_ID, com.vaadin.event.FieldEvents.BlurEvent.class, listener);
    }

    @Override
    public void addFocusListener(com.vaadin.event.FieldEvents.FocusListener listener) {
        addListener(com.vaadin.event.FieldEvents.FocusEvent.EVENT_ID, com.vaadin.event.FieldEvents.FocusEvent.class, listener,
                com.vaadin.event.FieldEvents.FocusListener.focusMethod);
    }

    @Override
    public void removeFocusListener(com.vaadin.event.FieldEvents.FocusListener listener) {
        removeListener(com.vaadin.event.FieldEvents.FocusEvent.EVENT_ID, com.vaadin.event.FieldEvents.FocusEvent.class, listener);
    }

    @Override
    public void focus() {
        tabbedHeader.focus();
    }

    @Override
    public int getTabIndex() {
        return tabbedHeader.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        tabbedHeader.setTabIndex(tabIndex);
    }

    public interface Tab extends Serializable {

        void setId(String id);

        String getId();

        @Nullable
        Resource getIcon();

        void setIcon(@Nullable Resource icon);

        String getDescription();

        void setDescription(String description);

        String getCaption();

        void setCaption(String caption);

        boolean isEnabled();

        void setEnabled(boolean enabled);

        boolean isVisible();

        void setVisible(boolean visible);

        boolean isClosable();

        void setClosable(boolean closable);

        String getStyleName();

        void setStyleName(String styleName);

        void setComponentError(ErrorMessage componentError);

        ErrorMessage getComponentError();

        Component getComponent();
    }

    public static class TabImpl extends CssLayout implements Tab {

        protected String id;

        protected static final String MANAGED_TAB_STYLENAME = "jmix-managed-tab";

        protected CloseHandler closeHandler;

        protected boolean closable;

        protected Component component;
        protected TabSheet.Tab tabbarTab;
        protected ContentSwitchMode contentSwitchMode = ContentSwitchMode.DEFAULT;

        protected TabImpl(Component component) {
            this.component = component;
            addComponent(component);
            setSizeFull();

            setPrimaryStyleName(MANAGED_TAB_STYLENAME);
        }

        @Nullable
        protected CloseHandler getCloseHandler() {
            return closeHandler;
        }

        protected void setCloseHandler(@Nullable CloseHandler closeHandler) {
            this.closeHandler = closeHandler;
        }

        @Override
        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setEnabled(boolean enabled) {
            tabbarTab.setEnabled(enabled);
        }

        @Override
        public boolean isEnabled() {
            return tabbarTab.isEnabled();
        }

        @Override
        public boolean isVisible() {
            return tabbarTab.isVisible();
        }

        @Override
        public void setVisible(boolean visible) {
            tabbarTab.setVisible(visible);
        }

        @Override
        public void setCaption(String caption) {
            tabbarTab.setCaption(caption);
        }

        @Override
        public String getCaption() {
            return tabbarTab.getCaption();
        }

        protected TabSheet.Tab getTabbarTab() {
            return tabbarTab;
        }

        protected void setTabbarTab(TabSheet.Tab tabbarTab) {
            this.tabbarTab = tabbarTab;
        }

        @Override
        public boolean isClosable() {
            return closable;
        }

        @Override
        public void setClosable(boolean closable) {
            this.closable = closable;
            tabbarTab.setClosable(closable);
        }

        @Override
        public void setStyleName(String style) {
            super.setStyleName(style);

            tabbarTab.setStyleName(style);
        }

        @Override
        public void setIcon(@Nullable Resource icon) {
            tabbarTab.setIcon(icon);
        }

        @Nullable
        @Override
        public Resource getIcon() {
            return tabbarTab.getIcon();
        }

        @Override
        public Component getComponent() {
            return component;
        }

        protected void setComponent(Component component) {
            this.removeComponent(this.component);

            this.component = component;

            addComponent(component);
        }

        protected void setContentSwitchMode(ContentSwitchMode contentSwitchMode) {
            this.contentSwitchMode = contentSwitchMode;
        }

        protected ContentSwitchMode getContentSwitchMode() {
            return contentSwitchMode;
        }
    }

    protected static class TabSheetBehaviourImpl implements TabSheetBehaviour {

        protected final JmixManagedTabSheet tabSheet;

        public TabSheetBehaviourImpl(JmixManagedTabSheet tabSheet) {
            this.tabSheet = tabSheet;
        }

        @Override
        public void setTabCaption(String tabId, String caption) {
            getTabNN(tabId).setCaption(caption);
        }

        protected Tab getTabNN(String tabId) {
            Tab tab = tabSheet.tabIds.get(tabId);
            if (tab == null) {
                throw new IllegalStateException("TabSheet does not contain tab with id: " + tabId);
            }
            return tab;
        }

        @Override
        public void setTabDescription(String tabId, @Nullable String description) {
            Tab tab = getTabNN(tabId);
            ((TabImpl) tab).getTabbarTab()
                    .setDescription(description);
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

        @Override
        public void setContentSwitchMode(String tabId, ContentSwitchMode contentSwitchMode) {
            ((TabImpl) getTabNN(tabId)).setContentSwitchMode(contentSwitchMode);
        }

        @Override
        public void setSelectedTab(String tabId) {
            tabSheet.setSelectedTab(getTabNN(tabId));
        }

        @Override
        public void addTab(Component component, String tabId) {
            Tab tab = tabSheet.addTab(component, null, null);

            tab.setId(tabId);

            tabSheet.tabIds.put(tabId, tab);
        }

        @Override
        public String getTab(Component component) {
            TabImpl tab = (TabImpl) tabSheet.tabs.get(component);
            return tab.getId();
        }

        @Override
        public void closeTab(Component target) {
            AppUI ui = (AppUI) tabSheet.getUI();
            if (!ui.isAccessibleForUser(tabSheet)) {
                LoggerFactory.getLogger(JmixTabSheet.class)
                        .debug("Ignore close tab attempt because TabSheet is inaccessible for user");
                return;
            }

            tabSheet.closeTab(target);
        }

        @Override
        public void closeOtherTabs(Component target) {
            AppUI ui = (AppUI) tabSheet.getUI();
            if (!ui.isAccessibleForUser(tabSheet)) {
                LoggerFactory.getLogger(JmixTabSheet.class)
                        .debug("Ignore close tab attempt because TabSheet is inaccessible for user");
                return;
            }

            tabSheet.closeOtherTabs(target);
        }

        @Override
        public void closeAllTabs() {
            AppUI ui = (AppUI) tabSheet.getUI();
            if (!ui.isAccessibleForUser(tabSheet)) {
                LoggerFactory.getLogger(JmixTabSheet.class)
                        .debug("Ignore close tab attempt because TabSheet is inaccessible for user");
                return;
            }

            tabSheet.closeAllTabs();
        }

        @Override
        public String getTab(int position) {
            return tabSheet.tabbedHeader.getTab(position).getId();
        }

        @Nullable
        @Override
        public Component getSelectedTab() {
            return tabSheet.selected != null ?
                    ((TabImpl) tabSheet.selected).getComponent()
                    : null;
        }

        @Override
        public Iterator<Component> getTabComponents() {
            return tabSheet.tabComponents.iterator();
        }

        @Override
        public Stream<Component> getTabComponentsStream() {
            return tabSheet.tabComponents.stream();
        }

        @Override
        public void setSelectedTab(Component component) {
            tabSheet.setSelectedTab(tabSheet.tabs.get(component));
        }

        @Override
        public void replaceComponent(Component oldComponent, Component newComponent) {
            tabSheet.tabbedHeader.replaceComponent(oldComponent, newComponent);
        }

        @Override
        public void removeComponent(Component component) {
            tabSheet.removeComponent(component);
        }

        @Nullable
        @Override
        public Component getPreviousTab(Component tab) {
            return tabSheet.tabbedHeader.getPreviousTab(tab);
        }

        @Override
        public void setTabTestId(String tabId, String testId) {
            TabImpl tabImpl = (TabImpl) getTabNN(tabId);
            tabSheet.tabbedHeader.setTestId(tabImpl.getTabbarTab(), testId);
        }

        @Override
        public void setTabJmixId(String tabId, String id) {
            Tab tab = getTabNN(tabId);
            tabSheet.tabbedHeader.setJTestId(((TabImpl) tab).getTabbarTab(), id);
        }

        @Override
        public void setTabCloseHandler(Component tabContent, BiConsumer<HasTabSheetBehaviour, Component> closeHandler) {
            Tab tab = tabSheet.tabs.get(tabContent);
            if (tab != null) {
                ((TabImpl) tab).setCloseHandler(closeHandler::accept);
            }
        }

        @Override
        public int getTabPosition(String tabId) {
            Tab tab = getTabNN(tabId);
            return tabSheet.tabbedHeader.getTabPosition(((TabImpl) tab).getTabbarTab());
        }

        @Override
        public int getComponentCount() {
            return tabSheet.tabbedHeader.getComponentCount();
        }

        @Override
        public void moveTab(Component c, int position) {
            Component tabComponent = tabSheet.getTabComponent((Component) tabSheet.tabs.get(c));
            tabSheet.tabbedHeader.moveTab(tabComponent, position);
        }

        @Override
        public void focus() {
            tabSheet.tabbedHeader.focus();
        }

        @Override
        public void silentCloseTabAndSelectPrevious(Component tab) {
            Component tabComponent = tabSheet.getTabComponent((Component) tabSheet.tabs.get(tab));
            tabSheet.tabbedHeader.silentCloseTabAndSelectPrevious(tabComponent);
        }
    }

    public enum Mode {
        HIDE_TABS,
        UNLOAD_TABS
    }
}
