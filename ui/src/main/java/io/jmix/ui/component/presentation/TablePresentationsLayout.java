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
package io.jmix.ui.component.presentation;

import com.vaadin.ui.*;
import org.springframework.context.ApplicationContext;
import io.jmix.core.Messages;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.AppUI;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.presentation.action.PresentationActionsBuilder;
import io.jmix.ui.presentation.PresentationsChangeListener;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import io.jmix.ui.sys.TestIdManager;
import io.jmix.ui.widget.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.defaultString;

public class TablePresentationsLayout extends VerticalLayout {

    public static final String CUSTOM_STYLE_NAME_PREFIX = "cs";

    protected static final String CURRENT_MENUITEM_STYLENAME = "jmix-table-prefs-menuitem-current";
    protected static final String DEFAULT_MENUITEM_STYLENAME = "jmix-table-prefs-menuitem-default";
    protected static final String TABLE_PREFS_STYLENAME = "jmix-table-prefs";

    protected static final String CONTEXT_MENU_BUTTON_STYLENAME = "jmix-cm-button";

    protected JmixMenuBar menuBar;
    protected JmixPopupButton button;
    protected CheckBox textSelectionCheckBox;

    protected Table table;
    protected JmixEnhancedTable tableImpl;
    protected ComponentSettingsBinder settingsBinder;

    protected Map<Object, MenuBar.MenuItem> presentationsMenuMap;

    protected Messages messages;
    protected ApplicationContext applicationContext;

    protected PresentationActionsBuilder presentationActionsBuilder;

    public TablePresentationsLayout(Table component, ComponentSettingsBinder settingsBinder, ApplicationContext applicationContext) {
        this.table = component;
        this.applicationContext = applicationContext;
        this.messages = applicationContext.getBean(Messages.class);

        this.tableImpl = table.unwrapOrNull(JmixEnhancedTable.class);

        this.settingsBinder = settingsBinder;

        setMargin(false);

        setSizeUndefined();
        setStyleName(TABLE_PREFS_STYLENAME);
        setParent(component.unwrap(HasComponents.class));

        initLayout();

        table.getPresentations().addListener(new PresentationsChangeListener() {
            @Override
            public void currentPresentationChanged(io.jmix.ui.presentation.TablePresentations presentations, @Nullable Object oldPresentationId) {
                table.getPresentations().commit();
                if (presentationsMenuMap != null) {
                    // simple change current item
                    if (oldPresentationId != null) {
                        if (oldPresentationId instanceof TablePresentation)
                            oldPresentationId = EntityValues.<UUID>getId(((TablePresentation) oldPresentationId));

                        MenuBar.MenuItem lastMenuItem = presentationsMenuMap.get(oldPresentationId);
                        if (lastMenuItem != null)
                            removeCurrentItemStyle(lastMenuItem);
                    }

                    TablePresentation current = presentations.getCurrent();
                    if (current != null) {
                        MenuBar.MenuItem menuItem = presentationsMenuMap.get(EntityValues.<UUID>getId(current));
                        if (menuItem != null)
                            setCurrentItemStyle(menuItem);
                    }

                    buildActions();
                }
            }

            @Override
            public void presentationsSetChanged(io.jmix.ui.presentation.TablePresentations presentations) {
                build();
            }

            @Override
            public void defaultPresentationChanged(io.jmix.ui.presentation.TablePresentations presentations, @Nullable Object oldPresentationId) {
                if (presentationsMenuMap != null) {
                    if (oldPresentationId != null) {
                        if (oldPresentationId instanceof TablePresentation)
                            oldPresentationId = EntityValues.<UUID>getId(((TablePresentation) oldPresentationId));

                        MenuBar.MenuItem lastMenuItem = presentationsMenuMap.get(oldPresentationId);
                        if (lastMenuItem != null)
                            removeDefaultItemStyle(lastMenuItem);
                    }

                    TablePresentation defaultPresentation = presentations.getDefault();
                    if (defaultPresentation != null) {
                        MenuBar.MenuItem menuItem = presentationsMenuMap.get(EntityValues.<UUID>getId(defaultPresentation));
                        if (menuItem != null)
                            setDefaultItemStyle(menuItem);
                    }
                }
            }
        });

        build();
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(TABLE_PREFS_STYLENAME, ""));
    }

    protected void removeCurrentItemStyle(MenuBar.MenuItem item) {
        removeStyleForItem(item, CURRENT_MENUITEM_STYLENAME);
    }

    protected void setCurrentItemStyle(MenuBar.MenuItem item) {
        addStyleForItem(item, CURRENT_MENUITEM_STYLENAME);
    }

    protected void removeDefaultItemStyle(MenuBar.MenuItem item) {
        removeStyleForItem(item, DEFAULT_MENUITEM_STYLENAME);
    }

    protected void setDefaultItemStyle(MenuBar.MenuItem item) {
        addStyleForItem(item, DEFAULT_MENUITEM_STYLENAME);
        item.setDescription(messages.getMessage("PresentationsPopup.defaultPresentation"));
    }

    protected void addStyleForItem(MenuBar.MenuItem item, String styleName) {
        List<String> styles = new ArrayList<>();
        String style = item.getStyleName();
        if (style != null) {
            CollectionUtils.addAll(styles, style.split(" "));
        }
        if (!styles.contains(styleName)) {
            styles.add(styleName);
        }
        applyStylesForItem(item, styles);
    }

    protected void removeStyleForItem(MenuBar.MenuItem item, String styleName) {
        String style = item.getStyleName();
        if (style != null) {
            List<String> styles = new ArrayList<>();
            CollectionUtils.addAll(styles, style.split(" "));
            styles.remove(styleName);
            applyStylesForItem(item, styles);
        }
    }

    protected void applyStylesForItem(MenuBar.MenuItem item, List<String> styles) {
        styles.remove(CUSTOM_STYLE_NAME_PREFIX);
        StringBuilder joinedStyle = new StringBuilder(CUSTOM_STYLE_NAME_PREFIX);
        for (String style : styles) {
            joinedStyle.append(" ").append(style);
        }
        item.setStyleName(joinedStyle.toString());
    }

    protected void initLayout() {
        setSpacing(true);

        Label titleLabel = new Label(messages.getMessage("PresentationsPopup.title"));
        titleLabel.setStyleName("jmix-table-prefs-title");
        titleLabel.setWidthUndefined();
        addComponent(titleLabel);
        setComponentAlignment(titleLabel, Alignment.MIDDLE_CENTER);

        menuBar = new JmixMenuBar();
        menuBar.setStyleName("jmix-table-prefs-list");
        menuBar.setWidth(100, Unit.PERCENTAGE);
        menuBar.setHeightUndefined();
        menuBar.setVertical(true);
        addComponent(menuBar);

        button = new JmixPopupButton();
        button.setCaption(messages.getMessage("PresentationsPopup.actions"));
        addComponent(button);
        setComponentAlignment(button, Alignment.MIDDLE_CENTER);

        textSelectionCheckBox = new CheckBox();
        textSelectionCheckBox.setCaption(messages.getMessage("PresentationsPopup.textSelection"));
        addComponent(textSelectionCheckBox);

        textSelectionCheckBox.setValue(tableImpl.isTextSelectionEnabled());
        textSelectionCheckBox.addValueChangeListener(e -> tableImpl.setTextSelectionEnabled(e.getValue()));
    }

    public void build() {
        button.setPopupVisible(false);
        buildPresentationsList();
        buildActions();
    }

    public void updateTextSelection() {
        textSelectionCheckBox.setValue(tableImpl.isTextSelectionEnabled());
    }

    protected void buildPresentationsList() {
        menuBar.removeItems();
        presentationsMenuMap = new HashMap<>();

        io.jmix.ui.presentation.TablePresentations p = table.getPresentations();

        for (Object presId : p.getPresentationIds()) {
            MenuBar.MenuItem item = menuBar.addItem(
                    defaultString(p.getCaption(presId)),
                    selectedItem -> table.applyPresentation(presId)
            );
            TablePresentation current = p.getCurrent();
            if (current != null && presId.equals(EntityValues.<UUID>getId(current))) {
                setCurrentItemStyle(item);
            }
            TablePresentation defaultPresentation = p.getDefault();
            if (defaultPresentation != null && presId.equals(EntityValues.<UUID>getId(defaultPresentation))) {
                setDefaultItemStyle(item);
            }
            presentationsMenuMap.put(presId, item);
        }
    }

    protected void buildActions() {
        JmixPopupButtonLayout actionsContainer = new JmixPopupButtonLayout();

        PresentationActionsBuilder presentationActionsBuilder = getPresentationActionsBuilder();
        if (presentationActionsBuilder != null) {
            for (Action action : presentationActionsBuilder.build()) {
                actionsContainer.addComponent(createActionButton(action));
            }
        }

        button.setContent(actionsContainer);
    }

    protected JmixButton createActionButton(Action action) {
        JmixButton actionBtn = new JmixButton();

        actionBtn.setWidth("100%");
        actionBtn.setPrimaryStyleName(CONTEXT_MENU_BUTTON_STYLENAME);

        setPopupButtonAction(actionBtn, action);

        AppUI ui = AppUI.getCurrent();
        if (ui != null) {
            if (ui.isTestMode()) {
                actionBtn.setJTestId(action.getId());
            }

            if (ui.isPerformanceTestMode()) {
                String debugId = getDebugId();
                if (debugId != null) {
                    TestIdManager testIdManager = ui.getTestIdManager();
                    actionBtn.setId(testIdManager.getTestId(debugId + "_" + action.getId()));
                }
            }
        }

        return actionBtn;
    }

    protected void setPopupButtonAction(JmixButton actionBtn, Action action) {
        actionBtn.setCaption(action.getCaption());

        String description = action.getDescription();
        if (description == null && action.getShortcutCombination() != null) {
            description = action.getShortcutCombination().format();
        }
        if (description != null) {
            actionBtn.setDescription(description);
        }

        actionBtn.setEnabled(action.isEnabled());
        actionBtn.setVisible(action.isVisible());

        actionBtn.setClickHandler(mouseEventDetails -> {
            this.focus();

            if (button.isAutoClose()) {
                button.setPopupVisible(false);
            }

            action.actionPerform(null);
        });
    }

    @Nullable
    protected PresentationActionsBuilder getPresentationActionsBuilder() {
        if (presentationActionsBuilder == null)
            presentationActionsBuilder = applicationContext.getBean(PresentationActionsBuilder.class, table, settingsBinder);
        return presentationActionsBuilder;
    }

    public void setPresentationActionsBuilder(PresentationActionsBuilder presentationActionsBuilder) {
        this.presentationActionsBuilder = presentationActionsBuilder;
    }

    protected String getMessage(String key) {
        return messages.getMessage(getClass(), key);
    }
}
