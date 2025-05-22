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

package io.jmix.tabbedmode.component.tabsheet;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.ComponentContainer;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.HasSubParts;
import io.jmix.tabbedmode.component.workarea.TabbedViewsContainer;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Stream;

import static io.jmix.flowui.component.UiComponentUtils.sameId;

@Tag("jmix-main-tabsheet")
@JsModule("./src/tabsheet/jmix-main-tabsheet.js")
@JsModule("./src/tabsheet/mainTabSheetConnector.ts")
public class JmixMainTabSheet extends Component implements TabbedViewsContainer<JmixMainTabSheet>,
        HasActions, ComponentContainer, HasSubParts,
        HasStyle, HasSize, HasThemeVariant<JmixMainTabSheetVariant>, HasPrefix, HasSuffix {

    protected static final String GENERATED_TAB_ID_PREFIX = "tabsheet-tab-";

    protected MainTabSheetActionsSupport actionsSupport;

    protected Tabs tabs;
    protected Map<Tab, Component> tabToContent = new HashMap<>();

    protected ContentSwitchMode contentSwitchMode = ContentSwitchMode.UNLOAD;

    public JmixMainTabSheet() {
        initComponent();
    }

    protected void initComponent() {
        tabs = createTabsComponent();
        SlotUtils.addToSlot(this, "tabs", tabs);

        addSelectedChangeListener(e -> {
            getElement().setProperty("selected", tabs.getSelectedIndex());
            updateContent();
        });

        addListener(MainTabsheetDropEvent.class, this::onDrop);
    }

    protected Tabs createTabsComponent() {
        return new Tabs();
    }

    @Override
    public Tab add(String tabText, Component content) {
        return add(new Tab(tabText), content);
    }

    @Override
    public Tab add(Component tabContent, Component content) {
        return add(new Tab(tabContent), content);
    }

    @Override
    public Tab add(Tab tab, Component content) {
        return add(tab, content, -1);
    }

    @Override
    public Tab add(Tab tab, Component content, int position) {
        Preconditions.checkNotNullArgument(tab, "The tab to be added cannot be null");
        Preconditions.checkNotNullArgument(content, "The content to be added cannot be null");

        if (content instanceof Text) {
            throw new IllegalArgumentException(
                    "Text as content is not supported. Consider wrapping the Text inside a Div.");
        }

        if (position < 0) {
            tabs.add(tab);
        } else {
            tabs.addTabAtIndex(position, tab);
        }

        if (tab instanceof DragSource<?> dragSource) {
            dragSource.setDraggable(isTabsDraggable());
        }

        updateTabContent(tab, content);

        fireEvent(new TabsCollectionChangeEvent<>(this, false,
                TabsCollectionChangeType.ADD,
                Collections.singleton(tab)));

        return tab;
    }

    @Override
    public void remove(Tab tab) {
        Preconditions.checkNotNullArgument(tab, "The tab to be removed cannot be null");

        Element content = tabToContent.remove(tab).getElement();
        content.removeFromParent();

        tabs.remove(tab);

        fireEvent(new TabsCollectionChangeEvent<>(this, false,
                TabsCollectionChangeType.REMOVE,
                Collections.singleton(tab)));
    }

    @Override
    public void remove(Component content) {
        Preconditions.checkNotNullArgument(content, "The content of the tab to be removed cannot be null");

        if (content instanceof Text) {
            throw new IllegalArgumentException(
                    "Text as content is not supported.");
        }

        Optional<Tab> tab = findTab(content);
        tab.ifPresent(this::remove);
    }

    @Override
    public void remove(int position) {
        remove(getTabAt(position));
    }

    @Override
    public int getSelectedIndex() {
        return tabs.getSelectedIndex();
    }

    @Override
    public void setSelectedIndex(int selectedIndex) {
        tabs.setSelectedIndex(selectedIndex);
    }

    @Nullable
    @Override
    public Tab getSelectedTab() {
        return tabs.getSelectedTab();
    }

    @Override
    public void setSelectedTab(@Nullable Tab selectedTab) {
        tabs.setSelectedTab(selectedTab);
    }

    @Override
    public int getTabCount() {
        return tabs.getTabCount();
    }

    @Override
    public Stream<Tab> getTabsStream() {
        return getChildren()
                .sequential()
                .map(component -> ((Tab) component));
    }

    @Override
    public Tab getTabAt(int position) {
        return tabs.getTabAt(position);
    }

    @Override
    public int getIndexOf(Tab tab) {
        return tabs.indexOf(tab);
    }

    @Override
    public Optional<Tab> findTab(Component content) {
        Preconditions.checkNotNullArgument(content,
                "The component to look for the tab cannot be null");

        return tabToContent.entrySet().stream()
                .filter(entry -> entry.getValue().equals(content))
                .map(Map.Entry::getKey).findFirst();
    }

    @Override
    public Optional<Tab> findTab(String id) {
        return getTabsStream()
                .filter(tab -> sameId(tab, id))
                .findAny();
    }

    @Override
    public Component getComponent(Tab tab) {
        Preconditions.checkNotNullArgument(tab,
                "The tab to look for the component cannot be null");

        return findComponent(tab)
                .orElseThrow(() ->
                        new IllegalArgumentException("Not found component associated with the given tab: '%s'"
                                .formatted(tab.getId().orElse(""))));
    }

    @Override
    public Optional<Component> findComponent(Tab tab) {
        Preconditions.checkNotNullArgument(tab,
                "The tab to look for the component cannot be null");

        return Optional.ofNullable(tabToContent.get(tab));
    }

    @Override
    public Stream<Component> getTabComponentsStream() {
        return getTabsStream()
                .map(this::getComponent);
    }

    @Override
    public ContentSwitchMode getContentSwitchMode() {
        return contentSwitchMode;
    }

    @Override
    public void setContentSwitchMode(ContentSwitchMode mode) {
        this.contentSwitchMode = mode;
    }

    @Override
    public Registration addSelectedChangeListener(
            ComponentEventListener<SelectedChangeEvent<JmixMainTabSheet>> listener) {
        return tabs.addSelectedChangeListener(event ->
                listener.onComponentEvent(new SelectedChangeEvent<>(this,
                        event.getPreviousTab(), event.isFromClient(),
                        event.isInitialSelection())));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addTabsCollectionChangeListener(
            ComponentEventListener<TabsCollectionChangeEvent<JmixMainTabSheet>> listener) {
        return addListener(TabsCollectionChangeEvent.class, ((ComponentEventListener) listener));
    }

    @Override
    public Optional<Component> findOwnComponent(String id) {
        return getOwnComponents().stream()
                .filter(component -> sameId(component, id))
                .findAny();
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return getTabComponentsStream()
                .toList();
    }

    /**
     * Gets the child components of tab sheet in {@link Element} tree.
     *
     * @return the stream of tab sheet tab
     */
    @Override
    public Stream<Component> getChildren() {
        return tabs.getChildren();
    }

    @Nullable
    @Override
    public Object getSubPart(String name) {
        return getChildren()
                .filter(tab -> sameId(tab, name))
                .findAny()
                .orElse(null);
    }

    @Override
    public void addAction(Action action) {
        getActionsSupport().addAction(action);
    }

    @Override
    public void addAction(Action action, int index) {
        getActionsSupport().addAction(action, index);
    }

    @Override
    public void removeAction(Action action) {
        getActionsSupport().removeAction(action);
    }

    @Override
    public Collection<Action> getActions() {
        return getActionsSupport().getActions();
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return getActionsSupport().getAction(id).orElse(null);
    }

    public boolean isTabsDraggable() {
        return getElement().getProperty("tabsDraggable", false);
    }

    public void setTabsDraggable(boolean tabsDraggable) {
        getElement().setProperty("tabsDraggable", tabsDraggable);

        getDraggabdleTabsStream().forEach(draggable ->
                draggable.setDraggable(isTabsDraggable()));
    }

    protected Stream<? extends DragSource<?>> getDraggabdleTabsStream() {
        return getTabsStream()
                .filter(tab -> tab instanceof DragSource<?>)
                .map(tab -> (DragSource<?>) tab);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        initConnector();
    }

    protected MainTabSheetActionsSupport getActionsSupport() {
        if (actionsSupport == null) {
            actionsSupport = createActionsSupport();
        }

        return actionsSupport;
    }

    protected MainTabSheetActionsSupport createActionsSupport() {
        return new MainTabSheetActionsSupport(this);
    }

    /**
     * Marks the content related to the selected tab as enabled and adds it to
     * the component if it is not already added. All the other content panels
     * are disabled, so they can't be interacted with.
     */
    protected void updateContent() {
        for (Map.Entry<Tab, Component> entry : tabToContent.entrySet()) {
            Tab tab = entry.getKey();
            Element content = entry.getValue().getElement();

            if (tab.equals(tabs.getSelectedTab())) {
                if (content.getParent() == null) {
                    getElement().appendChild(content);
                }

                content.setEnabled(true);
            } else {
                // Can't use setEnabled(false) because it would also mark the
                // elements as disabled in the DOM. Navigating between tabs
                // would then briefly show the content as disabled.
                content.getNode().setEnabled(false);

                if (contentSwitchMode == ContentSwitchMode.UNLOAD) {
                    content.removeFromParent();
                }
            }
        }
    }

    /**
     * Remove old tab content if exists and set new one
     *
     * @param tab     updated tab
     * @param content new content
     */
    protected void updateTabContent(Tab tab, Component content) {
        // Make sure possible old content related to the same tab gets removed
        if (tabToContent.containsKey(tab)) {
            tabToContent.get(tab).getElement().removeFromParent();
        }

        linkTabToContent(tab, content);

        tabToContent.put(tab, content);

        updateContent();
    }

    protected void linkTabToContent(Tab tab, Component content) {
        runBeforeClientResponse(ui -> {
            // On the client, content is associated with a tab by id
            String id = tab.getId()
                    .orElse(generateTabId());
            tab.setId(id);
            content.getElement().setAttribute("tab", id);
        });
    }

    protected void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    protected String generateTabId() {
        return GENERATED_TAB_ID_PREFIX + RandomStringUtils.secure().nextAlphanumeric(8);
    }

    protected void initConnector() {
        getElement().executeJs(
                "window.Vaadin.Flow.mainTabSheetConnector.initLazy(this)");
    }

    protected void onDrop(MainTabsheetDropEvent event) {
        if (ignoreDrop(event)) {
            return;
        }

        int index = calculateDropIndex(event);
        if (index == -1) {
            return;
        }

        Tab tab = event.getDragSource();
        tabs.addTabAtIndex(index, tab);
        tabs.setSelectedTab(tab);
    }

    protected boolean ignoreDrop(MainTabsheetDropEvent event) {
        return !isTabsDraggable()
                || event.getDragSource() == null
                || event.getDropTarget() == null
                || event.getDropLocation() == MainTabsheetDropLocation.EMPTY;
    }

    protected int calculateDropIndex(MainTabsheetDropEvent event) {
        int index = tabs.indexOf(event.getDropTarget());
        if (index == -1) {
            return index;
        }

        if (event.getDropLocation() == MainTabsheetDropLocation.AFTER) {
            index++;
        }

        if (index > tabs.getTabCount()) {
            index = tabs.getTabCount();
        }

        return index;
    }

    @ClientCallable
    private void updateContextMenuTargetTab(String tabId) {
        getElement().setProperty("_contextMenuTargetTabId", tabId);
    }
}
