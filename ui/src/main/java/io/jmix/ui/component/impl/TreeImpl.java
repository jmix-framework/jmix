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

package io.jmix.ui.component.impl;

import com.vaadin.data.HasValue;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.selection.MultiSelectionEvent;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.StyleGenerator;
import com.vaadin.ui.components.grid.MultiSelectionModel;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.Actions;
import io.jmix.ui.AppUI;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.LookupComponent.LookupSelectionChangeNotifier;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.TreeItems;
import io.jmix.ui.component.data.meta.EntityTreeItems;
import io.jmix.ui.component.tree.TreeDataProvider;
import io.jmix.ui.component.tree.TreeSourceEventsDelegate;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.sys.ShortcutsDelegate;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import io.jmix.ui.widget.JmixCssActionsLayout;
import io.jmix.ui.widget.JmixTree;
import io.jmix.ui.widget.ShortcutListenerDelegate;
import io.jmix.ui.widget.addon.contextmenu.TreeContextMenu;
import io.jmix.ui.widget.grid.JmixMultiSelectionModel;
import io.jmix.ui.widget.grid.JmixSingleSelectionModel;
import io.jmix.ui.widget.tree.EnhancedTreeDataProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.component.ComponentsHelper.findActionById;
import static io.jmix.ui.screen.LookupScreen.LOOKUP_ITEM_CLICK_ACTION_ID;

public class TreeImpl<E>
        extends AbstractComponent<JmixTree<E>>
        implements Tree<E>, LookupSelectionChangeNotifier<E>, SecuredActionsHolder,
        HasInnerComponents, InitializingBean, TreeSourceEventsDelegate<E> {

    private static final String HAS_TOP_PANEL_STYLENAME = "has-top-panel";

    // Style names used by tree itself
    protected List<String> internalStyles = new ArrayList<>(2);

    protected List<Function<? super E, String>> styleProviders; // lazily initialized List
    protected StyleGenerator<E> styleGenerator;    // lazily initialized field

    protected TreeContextMenu<E> contextMenu;
    protected final List<AbstractDataGrid.ActionMenuItemWrapper> contextMenuItems = new ArrayList<>();

    protected ButtonsPanel buttonsPanel;
    protected HorizontalLayout topPanel;
    protected TreeComposition componentComposition;
    protected Action enterPressAction;
    protected Function<? super E, String> iconProvider;

    /* Beans */
    protected Metadata metadata;
    protected AccessManager accessManager;
    protected IconResolver iconResolver;
    protected MetadataTools metadataTools;
    protected Actions actions;
    protected UiTestIdsSupport uiTestIdsSupport;

    protected SelectionMode selectionMode;

    protected Action doubleClickAction;
    protected Registration itemClickListener;

    /* SecuredActionsHolder */
    protected final List<Action> actionList = new ArrayList<>();
    protected final ShortcutsDelegate<ShortcutListener> shortcutsDelegate;
    protected final ActionsPermissions actionsPermissions = new ActionsPermissions(this);

    protected boolean showIconsForPopupMenuActions;

    protected String hierarchyProperty;
    protected TreeDataProvider<E> dataBinding;
    protected Function<? super E, String> itemCaptionProvider;
    protected Function<? super E, String> descriptionProvider;
    protected Function<E, Component> detailsGenerator;
    protected Registration expandListener;
    protected Registration collapseListener;

    public TreeImpl() {
        component = createComponent();
        componentComposition = createComponentComposition();
        shortcutsDelegate = createShortcutsDelegate();
    }

    protected JmixTree<E> createComponent() {
        return new JmixTree<>();
    }

    protected ShortcutsDelegate<ShortcutListener> createShortcutsDelegate() {
        return new ShortcutsDelegate<ShortcutListener>() {
            @Override
            protected ShortcutListener attachShortcut(String actionId, KeyCombination keyCombination) {
                ShortcutListener shortcut =
                        new ShortcutListenerDelegate(actionId, keyCombination.getKey().getCode(),
                                KeyCombination.Modifier.codes(keyCombination.getModifiers())
                        ).withHandler((sender, target) -> {
                            if (sender == componentComposition) {
                                Action action = getAction(actionId);
                                if (action != null && action.isEnabled() && action.isVisible()) {
                                    action.actionPerform(TreeImpl.this);
                                }
                            }
                        });

                componentComposition.addShortcutListener(shortcut);
                return shortcut;
            }

            @Override
            protected void detachShortcut(Action action, ShortcutListener shortcutDescriptor) {
                componentComposition.removeShortcutListener(shortcutDescriptor);
            }

            @Override
            protected Collection<Action> getActions() {
                return TreeImpl.this.getActions();
            }
        };
    }

    @Override
    public void afterPropertiesSet() {
        initComponentComposition(componentComposition);
        initComponent(component);

        initContextMenu();
    }

    protected void initComponentComposition(TreeComposition composition) {
        composition.setPrimaryStyleName("jmix-tree-composition");
        composition.setTree(component);
        composition.setWidthUndefined();

        composition.addShortcutListener(createEnterShortcutListener());
    }

    protected void initComponent(JmixTree<E> component) {
        component.setItemCaptionGenerator(this::generateItemCaption);

        component.setSizeUndefined();
        component.getCompositionRoot().setHeightMode(HeightMode.UNDEFINED);

        setSelectionMode(SelectionMode.SINGLE);
    }

    protected TreeComposition createComponentComposition() {
        return new TreeComposition();
    }

    protected ShortcutListenerDelegate createEnterShortcutListener() {
        return new ShortcutListenerDelegate("treeEnter", KeyCode.ENTER, null)
                .withHandler((sender, target) -> {
                    if (sender == componentComposition) {
                        AppUI ui = (AppUI) componentComposition.getUI();
                        if (!ui.isAccessibleForUser(componentComposition)) {
                            LoggerFactory.getLogger(TreeImpl.class)
                                    .debug("Ignore click attempt because Tree is inaccessible for user");
                            return;
                        }

                        if (enterPressAction != null) {
                            enterPressAction.actionPerform(this);
                        } else {
                            handleClickAction();
                        }
                    }
                });
    }

    protected String generateItemCaption(E item) {
        if (itemCaptionProvider != null) {
            return itemCaptionProvider.apply(item);
        }

        return metadataTools.getInstanceName(item);
    }

    protected void initContextMenu() {
        contextMenu = new TreeContextMenu<>(component);

        contextMenu.addTreeContextMenuListener(event -> {
            if (!component.getSelectedItems().contains(event.getItem())) {
                // In the multi select model 'setSelected' adds item to selected set,
                // but, in case of context click, we want to have a single selected item,
                // if it isn't in a set of already selected items
                if (isMultiSelect()) {
                    component.deselectAll();
                }
                setSelected(event.getItem());
            }
        });
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Autowired
    public void setIconResolver(IconResolver iconResolver) {
        this.iconResolver = iconResolver;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setThemeConstantsManager(ThemeConstantsManager themeConstantsManager) {
        ThemeConstants theme = themeConstantsManager.getConstants();
        this.showIconsForPopupMenuActions = theme.getBoolean("jmix.ui.showIconsForPopupMenuActions", false);
    }

    @Autowired
    public void setActions(Actions actions) {
        this.actions = actions;
    }

    @Autowired
    public void setUiTestIdsSupport(UiTestIdsSupport uiTestIdsSupport) {
        this.uiTestIdsSupport = uiTestIdsSupport;
    }

    @Nullable
    @Override
    public TreeItems<E> getItems() {
        return this.dataBinding != null ? this.dataBinding.getTreeItems() : null;
    }

    @Override
    public void setItems(@Nullable TreeItems<E> treeItems) {
        if (this.dataBinding != null) {
            this.dataBinding.unbind();
            this.dataBinding = null;

            this.component.setDataProvider(createEmptyDataProvider());
        }

        if (treeItems != null) {
            this.dataBinding = createDataGridDataProvider(treeItems);
            this.hierarchyProperty = treeItems.getHierarchyPropertyName();

            this.component.setDataProvider(this.dataBinding);

            refreshActionsState();

            setUiTestId(treeItems);
        }
    }

    protected void setUiTestId(TreeItems<E> items) {
        AppUI ui = AppUI.getCurrent();

        if (ui != null && ui.isTestMode()
                && getComponent().getJTestId() == null) {

            String testId = uiTestIdsSupport.getInferredTestId(items, "Tree");
            if (testId != null) {
                getComponent().setJTestId(testId);
                componentComposition.setJTestId(testId + "_composition");
            }
        }
    }

    @Override
    public void setDebugId(@Nullable String id) {
        super.setDebugId(id);

        AppUI ui = AppUI.getCurrent();
        if (id != null
                && ui != null
                && ui.isPerformanceTestMode()) {
            //noinspection unchecked
            TreeComposition composition = (TreeComposition) getComposition();
            composition.getTree().setId("jmixTree_" + ui.getTestIdManager().getTestId(id));
        }
    }

    @Override
    public void setId(@Nullable String id) {
        super.setId(id);

        AppUI ui = AppUI.getCurrent();
        if (id != null
                && ui != null
                && ui.isTestMode()) {
            componentComposition.setJTestId(id + "_composition");
        }
    }

    protected DataProvider<E, ?> createEmptyDataProvider() {
        return new EmptyTreeDataProvider<>();
    }

    @Override
    public String getHierarchyProperty() {
        return hierarchyProperty;
    }

    @Nullable
    @Override
    public Action getItemClickAction() {
        return doubleClickAction;
    }

    @Override
    public void setItemClickAction(@Nullable Action action) {
        if (doubleClickAction != null) {
            removeAction(doubleClickAction);
        }

        if (!getActions().contains(action)) {
            addAction(action);
        }

        if (this.doubleClickAction != action) {
            if (action != null) {
                if (itemClickListener == null) {
                    itemClickListener = component.addItemClickListener(this::onItemClick);
                }
            } else if (itemClickListener != null) {
                itemClickListener.remove();
                itemClickListener = null;
            }

            this.doubleClickAction = action;
        }
    }

    protected void onItemClick(com.vaadin.ui.Tree.ItemClick<E> event) {
        if (event.getMouseEventDetails().isDoubleClick()) {
            AppUI ui = (AppUI) component.getUI();
            if (!ui.isAccessibleForUser(component)) {
                LoggerFactory.getLogger(TreeImpl.class)
                        .debug("Ignore click attempt because Tree is inaccessible for user");
                return;
            }

            if (doubleClickAction != null) {
                doubleClickAction.actionPerform(TreeImpl.this);
            }
        }
    }

    @Override
    public void setItemCaptionProvider(@Nullable Function<? super E, String> itemCaptionProvider) {
        if (this.itemCaptionProvider != itemCaptionProvider) {
            this.itemCaptionProvider = itemCaptionProvider;

            component.setItemCaptionGenerator(this::generateItemCaption);
        }
    }

    @Nullable
    @Override
    public Function<? super E, String> getItemCaptionProvider() {
        return itemCaptionProvider;
    }

    protected void refreshActionsState() {
        for (Action action : getActions()) {
            action.refreshState();
        }
    }

    protected TreeDataProvider<E> createDataGridDataProvider(TreeItems<E> treeItems) {
        return new TreeDataProvider<>(treeItems, this);
    }

    @Override
    public void addAction(Action action) {
        int index = findActionById(actionList, action.getId());
        if (index < 0) {
            index = actionList.size();
        }

        addAction(action, index);
    }

    @Override
    public void addAction(Action action, int index) {
        checkNotNullArgument(action, "Action must be non null");

        int oldIndex = findActionById(actionList, action.getId());
        if (oldIndex >= 0) {
            removeAction(actionList.get(oldIndex));
            if (index > oldIndex) {
                index--;
            }
        }

        if (StringUtils.isNotEmpty(action.getCaption())) {
            AbstractDataGrid.ActionMenuItemWrapper menuItemWrapper = createContextMenuItem(action);
            menuItemWrapper.setAction(action);
            contextMenuItems.add(menuItemWrapper);
        }

        actionList.add(index, action);
        shortcutsDelegate.addAction(null, action);
        attachAction(action);
        actionsPermissions.apply(action);
    }

    protected AbstractDataGrid.ActionMenuItemWrapper createContextMenuItem(Action action) {
        MenuItem menuItem = contextMenu.addItem(action.getCaption(), null);
        menuItem.setStyleName("jmix-cm-item");

        return new AbstractDataGrid.ActionMenuItemWrapper(menuItem, showIconsForPopupMenuActions, iconResolver) {
            @Override
            public void performAction(Action action) {
                action.actionPerform(TreeImpl.this);
            }
        };
    }

    protected void attachAction(Action action) {
        if (action instanceof Action.HasTarget) {
            ((Action.HasTarget) action).setTarget(this);
        }

        action.refreshState();
    }

    @Override
    public void removeAction(Action action) {
        if (actionList.remove(action)) {
            AbstractDataGrid.ActionMenuItemWrapper menuItemWrapper = null;
            for (AbstractDataGrid.ActionMenuItemWrapper menuItem : contextMenuItems) {
                if (menuItem.getAction() == action) {
                    menuItemWrapper = menuItem;
                    break;
                }
            }

            if (menuItemWrapper != null) {
                menuItemWrapper.setAction(null);
                contextMenu.removeItem(menuItemWrapper.getMenuItem());
            }

            shortcutsDelegate.removeAction(action);
        }
    }

    @Override
    public void removeAction(String id) {
        Action action = getAction(id);
        if (action != null) {
            removeAction(action);
        }
    }

    @Override
    public void removeAllActions() {
        for (Action action : actionList.toArray(new Action[0])) {
            removeAction(action);
        }
    }

    @Override
    public Collection<Action> getActions() {
        return Collections.unmodifiableCollection(actionList);
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        for (Action action : getActions()) {
            if (Objects.equals(action.getId(), id)) {
                return action;
            }
        }
        return null;
    }

    @Override
    public ActionsPermissions getActionsPermissions() {
        return actionsPermissions;
    }

    @Override
    public void treeSourceItemSetChanged(TreeItems.ItemSetChangeEvent<E> event) {
        // #PL-2035, reload selection from ds
        Set<E> selectedItems = getSelected();
        Set<E> newSelection = new HashSet<>();
        TreeItems<E> source = event.getSource();
        for (E item : selectedItems) {
            if (source.containsItem(item)) {
                newSelection.add(source.getItem(EntityValues.getId(item)));
            }
        }

        if (source.getState() == BindingState.ACTIVE
                && source instanceof EntityTreeItems
                && ((EntityTreeItems<E>) source).getSelectedItem() != null) {
            newSelection.add(((EntityTreeItems<E>) source).getSelectedItem());
        }

        if (newSelection.isEmpty()) {
            setSelected((E) null);
        } else {
            // Workaround for the MultiSelect model.
            // Set the selected items only if the previous selection is different
            // Otherwise, the tree rows will display the values before editing
            if (isMultiSelect() && !selectedItems.equals(newSelection)) {
                setSelectedInternal(newSelection);
            }
        }

        refreshActionsState();
    }

    @Override
    public void treeSourcePropertyValueChanged(TreeItems.ValueChangeEvent<E> event) {
        refreshActionsState();
    }

    @Override
    public void treeSourceStateChanged(TreeItems.StateChangeEvent event) {
        refreshActionsState();
    }

    @Override
    public void treeSourceSelectedItemChanged(TreeItems.SelectedItemChangeEvent<E> event) {
        refreshActionsState();
    }

    @Override
    public void collapseTree() {
        component.collapseAll();
    }

    @Override
    public void expandTree() {
        component.expandAll();
    }

    @Override
    public void collapse(E item) {
        component.collapseItemWithChildren(item);
    }

    @Override
    public void expand(E item) {
        component.expandItemWithParents(item);
    }

    @Override
    public void expandUpTo(int level) {
        component.expandUpTo(level);
    }

    @Override
    public boolean isExpanded(Object itemId) {
        return getItems() != null
                && component.isExpanded(getItems().getItem(itemId));
    }

    @Nullable
    @Override
    public String getCaption() {
        return getComposition().getCaption();
    }

    @Override
    public void setCaption(@Nullable String caption) {
        getComposition().setCaption(caption);
    }

    @Override
    public boolean isCaptionAsHtml() {
        return ((com.vaadin.ui.AbstractComponent) getComposition()).isCaptionAsHtml();
    }

    @Override
    public void setCaptionAsHtml(boolean captionAsHtml) {
        ((com.vaadin.ui.AbstractComponent) getComposition()).setCaptionAsHtml(captionAsHtml);
    }

    @Nullable
    @Override
    public String getDescription() {
        return getComposition().getDescription();
    }

    @Override
    public void setDescription(@Nullable String description) {
        if (getComposition() instanceof com.vaadin.ui.AbstractComponent) {
            ((com.vaadin.ui.AbstractComponent) getComposition()).setDescription(description);
        }
    }

    @Override
    public Collection<io.jmix.ui.component.Component> getInnerComponents() {
        if (buttonsPanel != null) {
            return Collections.singletonList(buttonsPanel);
        }
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public ButtonsPanel getButtonsPanel() {
        return buttonsPanel;
    }

    @Override
    public com.vaadin.ui.Component getComposition() {
        return componentComposition;
    }

    @Override
    public void setButtonsPanel(@Nullable ButtonsPanel panel) {
        if (buttonsPanel != null && topPanel != null) {
            topPanel.removeComponent(buttonsPanel.unwrap(com.vaadin.ui.Component.class));
            buttonsPanel.setParent(null);
        }
        buttonsPanel = panel;
        if (panel != null) {
            if (panel.getParent() != null && panel.getParent() != this) {
                throw new IllegalStateException("Component already has parent");
            }

            if (topPanel == null) {
                topPanel = createTopPanel();
                topPanel.setWidth(100, Sizeable.Unit.PERCENTAGE);
                componentComposition.addComponentAsFirst(topPanel);
            }
            topPanel.addComponent(panel.unwrap(com.vaadin.ui.Component.class));
            if (panel instanceof VisibilityChangeNotifier) {
                ((VisibilityChangeNotifier) panel).addVisibilityChangeListener(event ->
                        updateCompositionStylesTopPanelVisible()
                );
            }
            panel.setParent(this);
        }

        updateCompositionStylesTopPanelVisible();
    }

    protected HorizontalLayout createTopPanel() {
        HorizontalLayout topPanel = new HorizontalLayout();
        topPanel.setSpacing(false);
        topPanel.setMargin(false);
        topPanel.setStyleName("jmix-tree-top");
        return topPanel;
    }

    // if buttons panel becomes hidden we need to set top panel height to 0
    protected void updateCompositionStylesTopPanelVisible() {
        if (topPanel != null) {
            boolean hasChildren = topPanel.getComponentCount() > 0;
            boolean anyChildVisible = false;
            for (com.vaadin.ui.Component childComponent : topPanel) {
                if (childComponent.isVisible()) {
                    anyChildVisible = true;
                    break;
                }
            }
            boolean topPanelVisible = hasChildren && anyChildVisible;

            if (!topPanelVisible) {
                componentComposition.removeStyleName(HAS_TOP_PANEL_STYLENAME);

                internalStyles.remove(HAS_TOP_PANEL_STYLENAME);
            } else {
                componentComposition.addStyleName(HAS_TOP_PANEL_STYLENAME);

                if (!internalStyles.contains(HAS_TOP_PANEL_STYLENAME)) {
                    internalStyles.add(HAS_TOP_PANEL_STYLENAME);
                }
            }
        }
    }

    protected void handleClickAction() {
        Action action = getItemClickAction();
        if (action == null) {
            action = getEnterPressAction();
            if (action == null) {
                action = getAction("edit");
                if (action == null) {
                    action = getAction("view");
                }
            }
        }

        if (action != null && action.isEnabled()) {
            action.actionPerform(this);
        }
    }

    @Override
    public void setLookupSelectHandler(Consumer<Collection<E>> selectHandler) {
        Consumer<Action.ActionPerformedEvent> actionHandler = event -> {
            Set<E> selected = getSelected();
            selectHandler.accept(selected);
        };

        setItemClickAction(new BaseAction(LOOKUP_ITEM_CLICK_ACTION_ID)
                .withHandler(actionHandler)
        );

        if (buttonsPanel != null && !buttonsPanel.isAlwaysVisible()) {
            buttonsPanel.setVisible(false);
            setContextMenuEnabled(false);
        }
    }

    @Override
    public Collection<E> getLookupSelectedItems() {
        return getSelected();
    }

    @Override
    public void setStyleName(@Nullable String name) {
        super.setStyleName(name);

        for (String internalStyle : internalStyles) {
            componentComposition.addStyleName(internalStyle);
        }
    }

    @Override
    public String getStyleName() {
        String styleName = super.getStyleName();
        for (String internalStyle : internalStyles) {
            styleName = styleName.replace(internalStyle, "");
        }
        return StringUtils.normalizeSpace(styleName);
    }

    @Override
    public void setStyleProvider(@Nullable Function<? super E, String> styleProvider) {
        if (styleProvider != null) {
            if (this.styleProviders == null) {
                this.styleProviders = new LinkedList<>();
            } else {
                this.styleProviders.clear();
            }

            this.styleProviders.add(styleProvider);
        } else {
            this.styleProviders = null;
        }

        updateStyleGenerator();
    }

    @Override
    public void addStyleProvider(Function<? super E, String> styleProvider) {
        if (this.styleProviders == null) {
            this.styleProviders = new LinkedList<>();
        }

        if (!this.styleProviders.contains(styleProvider)) {
            this.styleProviders.add(styleProvider);

            updateStyleGenerator();
        }
    }

    @Override
    public void removeStyleProvider(Function<? super E, String> styleProvider) {
        if (this.styleProviders != null) {
            if (this.styleProviders.remove(styleProvider)) {
                component.markAsDirty();
            }
        }
    }

    protected void updateStyleGenerator() {
        if (this.styleGenerator == null) {
            this.styleGenerator = this::getGeneratedStyle;
            component.setStyleGenerator(this.styleGenerator);
        } else {
            component.markAsDirty();
        }
    }

    @Nullable
    protected String getGeneratedStyle(E item) {
        if (styleProviders == null) {
            return null;
        }

        StringBuilder joinedStyle = null;
        for (Function<? super E, String> styleProvider : styleProviders) {
            String styleName = styleProvider.apply(item);
            if (styleName != null) {
                if (joinedStyle == null) {
                    joinedStyle = new StringBuilder(styleName);
                } else {
                    joinedStyle.append(" ").append(styleName);
                }
            }
        }

        return joinedStyle != null ? joinedStyle.toString() : null;
    }

    @Override
    public void repaint() {
        component.markAsDirty();
    }

    @Override
    public void setIconProvider(@Nullable Function<? super E, String> iconProvider) {
        if (this.iconProvider != iconProvider) {
            this.iconProvider = iconProvider;

            if (iconProvider == null) {
                component.setItemIconGenerator(item -> null);
            } else {
                component.setItemIconGenerator(this::getItemIcon);
            }
        }
    }

    @Nullable
    protected Resource getItemIcon(@Nullable E item) {
        if (item == null) {
            return null;
        }

        String resourceUrl = this.iconProvider.apply(item);
        return iconResolver.getIconResource(resourceUrl);
    }

    @Override
    public void setEnterPressAction(@Nullable Action action) {
        enterPressAction = action;
    }

    @Nullable
    @Override
    public Action getEnterPressAction() {
        return enterPressAction;
    }

    @Override
    public boolean isContextMenuEnabled() {
        return contextMenu.isEnabled();
    }

    @Override
    public void setContextMenuEnabled(boolean contextMenuEnabled) {
        contextMenu.setEnabled(contextMenuEnabled);
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @Override
    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    @Override
    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
        switch (selectionMode) {
            case SINGLE:
                component.setGridSelectionModel(new JmixSingleSelectionModel<>());
                break;
            case MULTI:
                component.setGridSelectionModel(new JmixMultiSelectionModel<>());
                break;
            case NONE:
                component.setSelectionMode(Grid.SelectionMode.NONE);
                return;
        }

        // Every time we change selection mode, the new selection model is set,
        // so we need to add selection listener again.
        component.addSelectionListener(this::onSelectionChange);
    }

    @Override
    public void setRowHeight(double rowHeight) {
        component.setRowHeight(rowHeight);
    }

    @Override
    public void setDescriptionProvider(@Nullable Function<? super E, String> provider) {
        this.setDescriptionProvider(provider, ContentMode.PREFORMATTED);
    }

    @Override
    public void setDescriptionProvider(@Nullable Function<? super E, String> provider, ContentMode contentMode) {
        descriptionProvider = provider;

        if (provider != null) {
            component.setItemDescriptionGenerator(this::getRowDescription,
                    WrapperUtils.toVaadinContentMode(contentMode));
        } else {
            component.setItemDescriptionGenerator(null);
        }
    }

    @Nullable
    protected String getRowDescription(E item) {
        String rowDescription = descriptionProvider.apply(item);
        return WrapperUtils.toContentMode(
                component.getCompositionRoot().getRowDescriptionContentMode()) == ContentMode.HTML
                ? sanitize(rowDescription)
                : rowDescription;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public Function<E, String> getDescriptionProvider() {
        return (Function<E, String>) descriptionProvider;
    }

    @Override
    public ContentMode getContentMode() {
        return WrapperUtils.toContentMode(component.getContentMode());
    }

    @Override
    public void setContentMode(ContentMode contentMode) {
        component.setContentMode(WrapperUtils.toVaadinContentMode(contentMode));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public Function<E, Component> getDetailsGenerator() {
        return detailsGenerator;
    }

    @Override
    public void setDetailsGenerator(@Nullable Function<E, Component> generator) {
        detailsGenerator = generator;
        component.getCompositionRoot().setDetailsGenerator(generator != null ? this::getItemDetails : null);
    }

    @Nullable
    protected com.vaadin.ui.Component getItemDetails(E entity) {
        Component detailsComponent = detailsGenerator.apply(entity);
        return detailsComponent != null ? detailsComponent.unwrapComposition(com.vaadin.ui.Component.class) : null;
    }

    @Override
    public boolean isDetailsVisible(E entity) {
        return component.getCompositionRoot().isDetailsVisible(entity);
    }

    @Override
    public void setDetailsVisible(E entity, boolean visible) {
        component.getCompositionRoot().setDetailsVisible(entity, visible);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addExpandListener(Consumer<ExpandEvent<E>> listener) {
        if (this.expandListener == null) {
            this.expandListener = component.addExpandListener(this::onItemExpand);
        }

        return getEventHub().subscribe(ExpandEvent.class, (Consumer) listener);
    }

    protected void onItemExpand(com.vaadin.event.ExpandEvent<E> e) {
        ExpandEvent<E> event = new ExpandEvent<>(TreeImpl.this, e.getExpandedItem(), e.isUserOriginated());
        publish(ExpandEvent.class, event);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addCollapseListener(Consumer<CollapseEvent<E>> listener) {
        if (this.collapseListener == null) {
            this.collapseListener = component.addCollapseListener(this::onItemCollapse);
        }

        return getEventHub().subscribe(CollapseEvent.class, (Consumer) listener);
    }

    protected void onItemCollapse(com.vaadin.event.CollapseEvent<E> e) {
        CollapseEvent<E> event = new CollapseEvent<>(TreeImpl.this, e.getCollapsedItem(), e.isUserOriginated());
        publish(CollapseEvent.class, event);
    }

    protected void onSelectionChange(com.vaadin.event.selection.SelectionEvent<E> event) {
        TreeItems<E> treeItems = getItems();

        if (treeItems == null
                || treeItems.getState() == BindingState.INACTIVE) {
            return;
        }

        Set<E> selected = getSelected();
        if (treeItems instanceof EntityTreeItems) {
            EntityTreeItems<E> entityTreeItems = (EntityTreeItems<E>) treeItems;

            if (selected.isEmpty()) {
                entityTreeItems.setSelectedItem(null);
            } else {
                // reset selection and select new item
                if (isMultiSelect()) {
                    entityTreeItems.setSelectedItem(null);
                }

                E newItem = selected.iterator().next();
                E dsItem = entityTreeItems.getSelectedItem();
                entityTreeItems.setSelectedItem(newItem);

                if (Objects.equals(dsItem, newItem)) {
                    // in this case item change event will not be generated
                    refreshActionsState();
                }
            }
        }

        fireSelectionEvent(event);

        LookupSelectionChangeEvent<E> selectionChangeEvent = new LookupSelectionChangeEvent<>(this);
        publish(LookupSelectionChangeEvent.class, selectionChangeEvent);
    }

    protected void fireSelectionEvent(com.vaadin.event.selection.SelectionEvent<E> e) {
        Set<E> oldSelection;
        if (e instanceof MultiSelectionEvent) {
            oldSelection = ((MultiSelectionEvent<E>) e).getOldSelection();
        } else {
            //noinspection unchecked
            E oldValue = ((HasValue.ValueChangeEvent<E>) e).getOldValue();
            oldSelection = oldValue != null ? Collections.singleton(oldValue) : Collections.emptySet();
        }

        SelectionEvent<E> event = new SelectionEvent<>(TreeImpl.this, oldSelection, e.isUserOriginated());
        publish(SelectionEvent.class, event);
    }

    @Override
    public boolean isMultiSelect() {
        return SelectionMode.MULTI.equals(selectionMode);
    }

    @Nullable
    @Override
    public E getSingleSelected() {
        Set<E> selectedItems = component.getSelectedItems();
        return CollectionUtils.isNotEmpty(selectedItems)
                ? selectedItems.iterator().next()
                : null;
    }

    @Override
    public Set<E> getSelected() {
        Set<E> selectedItems = component.getSelectedItems();
        return selectedItems != null
                ? selectedItems
                : Collections.emptySet();
    }

    @Override
    public void setSelected(@Nullable E item) {
        if (SelectionMode.NONE.equals(getSelectionMode())) {
            return;
        }

        if (item == null) {
            component.deselectAll();
        } else {
            setSelected(Collections.singletonList(item));
        }
    }

    @Override
    public void setSelected(Collection<E> items) {
        TreeItems<E> treeItems = getItems();

        boolean allMatch = items.stream()
                .allMatch(treeItems::containsItem);

        if (!allMatch) {
            throw new IllegalStateException("Datasource doesn't contain items");
        }

        setSelectedInternal(items);
    }

    @SuppressWarnings("unchecked")
    protected void setSelectedInternal(Collection<E> items) {
        switch (selectionMode) {
            case SINGLE:
                if (items.size() > 0) {
                    E item = items.iterator().next();
                    component.select(item);
                } else {
                    component.deselectAll();
                }
                break;
            case MULTI:
                component.deselectAll();
                ((MultiSelectionModel) component.getSelectionModel()).selectItems(items.toArray());
                break;

            default:
                throw new UnsupportedOperationException("Unsupported selection mode");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addSelectionListener(Consumer<SelectionEvent<E>> listener) {
        return getEventHub().subscribe(SelectionEvent.class, (Consumer) listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Subscription addLookupValueChangeListener(Consumer<LookupSelectionChangeEvent<E>> listener) {
        return getEventHub().subscribe(LookupSelectionChangeEvent.class, (Consumer) listener);
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public void attached() {
        super.attached();

        for (io.jmix.ui.component.Component component : getInnerComponents()) {
            ((AttachNotifier) component).attached();
        }
    }

    @Override
    public void detached() {
        super.detached();

        for (io.jmix.ui.component.Component component : getInnerComponents()) {
            ((AttachNotifier) component).detached();
        }
    }

    protected class EmptyTreeDataProvider<T>
            extends com.vaadin.data.provider.TreeDataProvider<T>
            implements EnhancedTreeDataProvider<T> {

        public EmptyTreeDataProvider() {
            super(new TreeData<>());
        }

        @Override
        public Stream<T> getItems() {
            return Stream.empty();
        }

        @Nullable
        @Override
        public T getParent(T item) {
            return null;
        }
    }

    protected class TreeComposition extends JmixCssActionsLayout {
        protected JmixTree<?> tree;
        protected com.vaadin.ui.CssLayout treeWrapper;

        public JmixTree<?> getTree() {
            return tree;
        }

        public void setTree(JmixTree<?> tree) {
            checkNotNullArgument(tree, "Tree can't be null");

            if (treeWrapper == null) {
                // Wrapper is needed in order to provide a border
                treeWrapper = createTreeWrapper();
                addComponent(treeWrapper);
            }

            if (this.tree != null) {
                treeWrapper.removeComponent(this.tree);
            }

            this.tree = tree;
            treeWrapper.addComponent(tree);

            updateTreeHeight();
            updateTreeWidth();
        }

        @Override
        public void setHeight(float height, Sizeable.Unit unit) {
            super.setHeight(height, unit);
            updateTreeHeight();
        }

        protected void updateTreeHeight() {
            if (getHeight() < 0) {
                treeWrapper.setHeightUndefined();

                tree.setHeightUndefined();
                tree.getCompositionRoot().setHeightMode(HeightMode.UNDEFINED);
            } else {
                treeWrapper.setHeight(100, Sizeable.Unit.PERCENTAGE);

                tree.setHeight(100, Sizeable.Unit.PERCENTAGE);
                tree.getCompositionRoot().setHeightMode(HeightMode.CSS);
            }
        }

        @Override
        public void setWidth(float width, Sizeable.Unit unit) {
            super.setWidth(width, unit);
            updateTreeWidth();
        }

        protected void updateTreeWidth() {
            if (getWidth() < 0) {
                treeWrapper.setWidthUndefined();
                tree.setWidthUndefined();
            } else {
                treeWrapper.setWidth(100, Sizeable.Unit.PERCENTAGE);
                tree.setWidth(100, Sizeable.Unit.PERCENTAGE);
            }
        }

        protected com.vaadin.ui.CssLayout createTreeWrapper() {
            com.vaadin.ui.CssLayout treeWrapper = new com.vaadin.ui.CssLayout();
            treeWrapper.setStyleName("jmix-tree-wrapper");
            return treeWrapper;
        }
    }
}
