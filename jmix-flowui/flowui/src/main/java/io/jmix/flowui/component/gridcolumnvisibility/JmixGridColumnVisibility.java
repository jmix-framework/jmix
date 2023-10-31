/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.component.gridcolumnvisibility;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AttachNotifier;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachNotifier;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.shared.HasOverlayClassName;
import com.vaadin.flow.component.shared.HasThemeVariant;
import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.dropdownbutton.AbstractDropdownButton;
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;
import io.jmix.flowui.kit.component.menubar.JmixSubMenu;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class JmixGridColumnVisibility extends Composite<JmixMenuBar>
        implements AttachNotifier, DetachNotifier, ApplicationContextAware, InitializingBean,
        HasTitle, HasSize, HasThemeVariant<GridColumnVisibilityVariant>, HasEnabled, HasStyle, HasOverlayClassName,
        HasText, Focusable<AbstractDropdownButton> {

    protected ApplicationContext applicationContext;
    protected Messages messages;

    protected static final String ATTRIBUTE_JMIX_ROLE_NAME = "jmix-role";
    protected static final String ATTRIBUTE_JMIX_ROLE_VALUE = "jmix-grid-column-visibility";

    protected JmixMenuItem dropdownItem;
    protected Icon icon;

    protected Grid<?> grid;

    protected Header header;
    protected Map<String, JmixMenuItem> columnItems = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
    }

    protected void autowireDependencies() {
        messages = applicationContext.getBean(Messages.class);
    }

    @Override
    protected JmixMenuBar initContent() {
        JmixMenuBar menuBar = super.initContent();
        menuBar.getElement().setAttribute(ATTRIBUTE_JMIX_ROLE_NAME, ATTRIBUTE_JMIX_ROLE_VALUE);
        initDropdownItem(menuBar);
        return menuBar;
    }

    protected void initDropdownItem(JmixMenuBar menuBar) {
        dropdownItem = menuBar.addItem("");
        header = new Header();
        header.setShowAllEnabled(true);
        header.setHideAllEnabled(true);
    }

    public void setIcon(@Nullable Icon icon) {
        if (this.icon != null) {
            dropdownItem.remove(this.icon);
        }
        this.icon = icon;

        updateIconSlot();
    }

    protected void updateIconSlot() {
        if (icon != null) {
            dropdownItem.addComponentAsFirst(icon);
        }
    }

    @Nullable
    public Icon getIcon() {
        return icon;
    }

    public void setGrid(Grid<?> grid) {
        Preconditions.checkNotNullArgument(grid);

        this.grid = grid;
    }

    public Grid<?> getGrid() {
        return grid;
    }

    public void addColumnItem(Grid.Column<?> column) {
        Preconditions.checkNotNullArgument(column);
        checkColumnOwner(column);

        String text = getColumnHeaderText(column);
        addColumnItem(column, text);
    }

    protected void checkColumnOwner(Grid.Column<?> column) {
        if (!column.getGrid().equals(grid)) {
            throw new IllegalArgumentException("Column '%s' doesn't belong to specified grid"
                    .formatted(column.getKey()));
        }
    }

    protected String getColumnHeaderText(Grid.Column<?> column) {
        String headerText = column.getHeaderText();
        if (!Strings.isNullOrEmpty(headerText)) {
            return headerText;
        } else {
            Component headerComponent = column.getHeaderComponent();
            if (headerComponent instanceof HasText hasText) {
                headerText = hasText.getText();
            }
            return Strings.nullToEmpty(headerText);
        }
    }

    public void addColumnItem(Grid.Column<?> column, String text) {
        Preconditions.checkNotNullArgument(column);
        Preconditions.checkNotNullArgument(text);
        checkColumnOwner(column);

        String columnKey = column.getKey();
        JmixMenuItem item = dropdownItem.getSubMenu().addItem(text, e -> onColumnItemClick(e, columnKey));
        addColumnItemInternal(column, text, item);
    }

    protected void onColumnItemClick(ClickEvent<MenuItem> event, String columnKey) {
        //set column visibility by key to handle cases when the column was removed
        //from the grid after initialization of column visibility item
        setColumnVisible(columnKey, event.getSource().isChecked());
    }

    protected void setColumnVisible(String columnKey, boolean visible) {
        Grid.Column<?> column = getColumnByKey(columnKey);
        column.setVisible(visible);
    }

    protected void addColumnItemInternal(Grid.Column<?> column, String text, JmixMenuItem item) {
        Preconditions.checkNotNullArgument(column);
        Preconditions.checkNotNullArgument(text);
        checkColumnOwner(column);

        item.setCheckable(true);
        item.setChecked(column.isVisible());
        //todo: yuriy-khrebtov - uncomment when update to Vaadin 24.2
//        item.setKeepOpen(true);

        //todo: add new events to grid column
//        column.addVisibleChangeListener(e -> item.setChecked(e.getSource().isVisible()));
//        column.addHeaderTextChangeListener(e -> item.setText(e.getText()));

        columnItems.put(column.getKey(), item);

        header.refresh();
    }

    protected Grid.Column<?> getColumnByKey(String columnKey) {
        if (grid == null) {
            throw new IllegalStateException("No grid specified");
        }
        Grid.Column<?> column = grid.getColumnByKey(columnKey);
        if (column == null) {
            throw new IllegalArgumentException("Failed to find column with key '%s' in the grid".formatted(columnKey));
        }
        return column;
    }

    public void addColumnItemAtIndex(Grid.Column<?> column, int index) {
        Preconditions.checkNotNullArgument(column);
        checkColumnOwner(column);

        String text = getColumnHeaderText(column);
        addColumnItemAtIndex(column, text, index);
    }

    public void addColumnItemAtIndex(Grid.Column<?> column, String text, int index) {
        Preconditions.checkNotNullArgument(column);
        Preconditions.checkNotNullArgument(text);
        checkColumnOwner(column);

        int resultIndex = header.getHeaderItemCount() + index;
        String columnKey = column.getKey();
        JmixMenuItem item = dropdownItem.getSubMenu().addItemAtIndex(resultIndex, text, e ->
                onColumnItemClick(e, columnKey));

        addColumnItemInternal(column, text, item);
    }

    public void addColumnItem(String columnKey) {
        Preconditions.checkNotNullArgument(columnKey);

        Grid.Column<?> column = getColumnByKey(columnKey);
        addColumnItem(column);
    }

    public void addColumnItem(String columnKey, String text) {
        Preconditions.checkNotNullArgument(columnKey);
        Preconditions.checkNotNullArgument(text);

        Grid.Column<?> column = getColumnByKey(columnKey);
        addColumnItem(column, text);
    }

    public void addColumnItemAtIndex(String columnKey, int index) {
        Preconditions.checkNotNullArgument(columnKey);

        Grid.Column<?> column = getColumnByKey(columnKey);
        addColumnItemAtIndex(column, index);
    }

    public void addColumnItemAtIndex(String columnKey, String text, int index) {
        Preconditions.checkNotNullArgument(columnKey);
        Preconditions.checkNotNullArgument(text);

        Grid.Column<?> column = getColumnByKey(columnKey);
        addColumnItemAtIndex(column, text, index);
    }

    public void setShowAllEnabled(boolean enabled) {
        header.setShowAllEnabled(enabled);
    }

    public void removeColumnItem(String columnKey) {
        Preconditions.checkNotNullArgument(columnKey);

        JmixMenuItem item = columnItems.get(columnKey);
        if (item == null) {
            throw new IllegalArgumentException("Column item for column '%s' doesn't exist".formatted(columnKey));
        }
        columnItems.remove(columnKey);
        dropdownItem.getSubMenu().remove(item);
        header.refresh();
    }

    public void removeColumnItem(Grid.Column<?> column) {
        Preconditions.checkNotNullArgument(column);
        checkColumnOwner(column);

        removeColumnItem(column.getKey());
    }

    public boolean isShowAllEnabled() {
        return header.isShowAllEnabled();
    }

    public void setHideAllEnabled(boolean enabled) {
        header.setHideAllEnabled(enabled);
    }

    public boolean isHideAllEnabled() {
        return header.isHideAllEnabled();
    }

    public void removeAllColumnItems() {
        JmixSubMenu subMenu = dropdownItem.getSubMenu();
        columnItems.values().forEach(subMenu::remove);
        columnItems.clear();
        header.refresh();
    }

    @Override
    public void setText(String text) {
        dropdownItem.setText(text);
        updateIconSlot();
    }

    @Override
    public String getText() {
        return dropdownItem.getText();
    }

    @Override
    public void setWhiteSpace(WhiteSpace value) {
        dropdownItem.setWhiteSpace(value);
    }

    @Override
    public WhiteSpace getWhiteSpace() {
        return dropdownItem.getWhiteSpace();
    }

    protected class Header {

        protected JmixMenuItem showAllItem;
        protected JmixMenuItem hideAllItem;
        protected Component separatorItem;
        protected boolean showAllEnabled = false;
        protected boolean hideAllEnabled = false;

        public int getHeaderItemCount() {
            int count = 0;
            if (showAllItem != null) {
                count++;
            }
            if (hideAllItem != null) {
                count++;
            }
            if (separatorItem != null) {
                count++;
            }
            return count;
        }

        protected void addShowAllItem() {
            String showAllText = messages.getMessage(getClass(), "showAllItem.text");

            showAllItem = dropdownItem.getSubMenu().addItemAtIndex(0, showAllText, this::onShowAllClick);
            refresh();
        }

        protected void onShowAllClick(ClickEvent<?> event) {
            for (Map.Entry<String, JmixMenuItem> columnKeyItemEntry : columnItems.entrySet()) {
                Grid.Column<?> column = getColumnByKey(columnKeyItemEntry.getKey());
                column.setVisible(true);
                columnKeyItemEntry.getValue().setChecked(true);
            }
        }

        public void refresh() {
            if (showAllItem == null && hideAllItem == null || columnItems.isEmpty()) {
                if (separatorItem != null) {
                    dropdownItem.getSubMenu().remove(separatorItem);
                    separatorItem = null;
                }
            } else {
                if (separatorItem == null) {
                    int index = getHeaderItemCount();
                    separatorItem = new Hr();
                    dropdownItem.getSubMenu().addComponentAtIndex(index, separatorItem);
                }
            }
        }

        protected void addHideAllItem() {
            String hideAllText = messages.getMessage(getClass(), "hideAllItem.text");
            //add first if showAllItem is disabled, after showAllItem (second) otherwise
            int index = showAllItem != null ? 1 : 0;
            hideAllItem = dropdownItem.getSubMenu().addItemAtIndex(index, hideAllText, this::onHideAllClick);
            refresh();
        }

        protected void onHideAllClick(ClickEvent<?> event) {
            for (Map.Entry<String, JmixMenuItem> columnKeyItemEntry : columnItems.entrySet()) {
                Grid.Column<?> column = getColumnByKey(columnKeyItemEntry.getKey());
                column.setVisible(false);
                columnKeyItemEntry.getValue().setChecked(false);
            }
        }

        public void setShowAllEnabled(boolean enabled) {
            showAllEnabled = enabled;
            if (showAllEnabled && showAllItem == null) {
                addShowAllItem();
            } else if (!showAllEnabled && showAllItem != null) {
                removeShowAllItem();
            }
        }

        protected void removeShowAllItem() {
            dropdownItem.remove(showAllItem);
            showAllItem = null;
            refresh();
        }

        public boolean isShowAllEnabled() {
            return showAllEnabled;
        }

        public void setHideAllEnabled(boolean enabled) {
            hideAllEnabled = enabled;
            if (hideAllEnabled && hideAllItem == null) {
                addHideAllItem();
            } else if (!hideAllEnabled && hideAllItem != null) {
                removeHideAllItem();
            }
        }

        protected void removeHideAllItem() {
            dropdownItem.remove(hideAllItem);
            hideAllItem = null;
            refresh();
        }

        public boolean isHideAllEnabled() {
            return hideAllEnabled;
        }
    }
}
