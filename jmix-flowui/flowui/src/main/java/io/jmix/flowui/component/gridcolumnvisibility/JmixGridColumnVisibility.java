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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.shared.HasOverlayClassName;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.dropdownbutton.AbstractDropdownButton;
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A component which allows to manage visibility of data grid columns
 */
public class JmixGridColumnVisibility extends Composite<JmixMenuBar>
        implements AttachNotifier, DetachNotifier, ApplicationContextAware, InitializingBean,
        HasTitle, HasSize, HasThemeVariant<GridColumnVisibilityVariant>, HasEnabled, HasStyle, HasOverlayClassName,
        HasText, Focusable<AbstractDropdownButton> {

    protected static final String ATTRIBUTE_JMIX_ROLE_NAME = "jmix-role";
    protected static final String ATTRIBUTE_JMIX_ROLE_VALUE = "jmix-grid-column-visibility";

    protected ApplicationContext applicationContext;
    protected Messages messages;

    protected JmixMenuItem dropdownItem;
    protected Icon icon;

    protected Grid<?> grid;

    protected Header header;
    protected List<ColumnItemImpl> columnItems = new ArrayList<>();

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

    /**
     * Sets component icon.
     *
     * @param icon icon to set
     */
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

    /**
     * @return icon of the component
     */
    @Nullable
    public Icon getIcon() {
        return icon;
    }

    /**
     * Sets a grid which columns will be managed by this component.
     * The grid must be an instance of DataGrid or TreeDataGrid.
     *
     * @param grid the grid to set
     */
    public void setGrid(Grid<?> grid) {
        Preconditions.checkNotNullArgument(grid);
        checkGridType(grid);

        removeAllColumnItems();
        this.grid = grid;
    }

    protected void checkGridType(Grid<?> grid) {
        if (!(grid instanceof DataGrid<?>) && !(grid instanceof TreeDataGrid<?>)) {
            throw new IllegalArgumentException("The grid must be an instance of DataGrid or TreeDataGrid");
        }
    }

    /**
     * @return grid instance which columns are managed by this component
     */
    public Grid<?> getGrid() {
        return grid;
    }

    /**
     * Adds new column item, which allows to toggle visibility of specified column, to the end of dropdown menu.
     * Item text will be copied from column header text.
     *
     * @param column column to manage
     */
    public void addColumnItem(DataGridColumn<?> column) {
        Preconditions.checkNotNullArgument(column);
        checkColumnOwner(column);

        String text = getColumnHeaderText(column);
        addColumnItemInternal(column, text, null);
    }

    protected void checkColumnOwner(DataGridColumn<?> column) {
        if (!column.getGrid().equals(grid)) {
            throw new IllegalArgumentException("Column '%s' doesn't belong to specified grid"
                    .formatted(column.getKey()));
        }
    }

    protected String getColumnHeaderText(DataGridColumn<?> column) {
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

    protected void addColumnItemInternal(DataGridColumn<?> column, String text, @Nullable Integer index) {
        JmixMenuItem item;
        if (index == null) {
            item = dropdownItem.getSubMenu().addItem(text);
        } else {
            int menuItemIndex = header.getHeaderItemCount() + index;
            item = dropdownItem.getSubMenu().addItemAtIndex(menuItemIndex, text);
        }

        item.setCheckable(true);
        item.setKeepOpen(true);

        ColumnItemImpl columnItem = new ColumnItemImpl(column, item);
        if (index == null) {
            columnItems.add(columnItem);
        } else {
            columnItems.add(index, columnItem);
        }

        header.refresh();
    }

    /**
     * Adds new column item, which allows to toggle visibility of specified column, to the end of dropdown menu.
     *
     * @param column column to manage
     * @param text   item text
     */
    public void addColumnItem(DataGridColumn<?> column, String text) {
        Preconditions.checkNotNullArgument(column);
        Preconditions.checkNotNullArgument(text);
        checkColumnOwner(column);

        addColumnItemInternal(column, text, null);
    }

    /**
     * Adds new column item, which allows to toggle visibility of specified column, to dropdown menu.
     * The item will be placed using specified index. Item text will be copied from column header text.
     *
     * @param column column to manage
     * @param index  index of new item
     */
    public void addColumnItemAtIndex(DataGridColumn<?> column, int index) {
        Preconditions.checkNotNullArgument(column);
        checkColumnOwner(column);

        String text = getColumnHeaderText(column);
        addColumnItemInternal(column, text, index);
    }

    /**
     * Adds new column item, which allows to toggle visibility of specified column, to dropdown menu.
     * The item will be placed using specified index.
     *
     * @param column column to manage
     * @param index  index of new item
     * @param text   item text
     */
    public void addColumnItemAtIndex(DataGridColumn<?> column, String text, int index) {
        Preconditions.checkNotNullArgument(column);
        Preconditions.checkNotNullArgument(text);
        checkColumnOwner(column);

        addColumnItemInternal(column, text, index);
    }

    /**
     * Adds new column item, which allows to toggle visibility of the column with specified key,
     * to the end of dropdown menu. Item text will be copied from column header text.
     *
     * @param columnKey key of a column to manage
     */
    public void addColumnItem(String columnKey) {
        Preconditions.checkNotNullArgument(columnKey);

        DataGridColumn<?> column = getColumnByKey(columnKey);
        addColumnItem(column);
    }

    protected DataGridColumn<?> getColumnByKey(String columnKey) {
        if (grid == null) {
            throw new IllegalStateException("No grid specified");
        }
        DataGridColumn<?> column = (DataGridColumn<?>) grid.getColumnByKey(columnKey);
        if (column == null) {
            throw new IllegalArgumentException("Failed to find column with key '%s' in the grid".formatted(columnKey));
        }
        return column;
    }

    /**
     * Adds new column item, which allows to toggle visibility of the column with specified key,
     * to the end of dropdown menu.
     *
     * @param columnKey key of a column to manage
     * @param text      item text
     */
    public void addColumnItem(String columnKey, String text) {
        Preconditions.checkNotNullArgument(columnKey);
        Preconditions.checkNotNullArgument(text);

        DataGridColumn<?> column = getColumnByKey(columnKey);
        addColumnItem(column, text);
    }

    /**
     * Adds new column item, which allows to toggle visibility of the column with specified key, to dropdown menu.
     * The item will be placed using specified index. Item text will be copied from column header text.
     *
     * @param columnKey key of a column to manage
     * @param index     index of new item
     */
    public void addColumnItemAtIndex(String columnKey, int index) {
        Preconditions.checkNotNullArgument(columnKey);

        DataGridColumn<?> column = getColumnByKey(columnKey);
        addColumnItemAtIndex(column, index);
    }

    /**
     * Adds new column item, which allows to toggle visibility of the column with specified key, to dropdown menu.
     * The item will be placed using specified index.
     *
     * @param columnKey key of a column to manage
     * @param text      item text
     * @param index     index of new item
     */
    public void addColumnItemAtIndex(String columnKey, String text, int index) {
        Preconditions.checkNotNullArgument(columnKey);
        Preconditions.checkNotNullArgument(text);

        DataGridColumn<?> column = getColumnByKey(columnKey);
        addColumnItemAtIndex(column, text, index);
    }

    /**
     * Sets whether "Show all" item should be visible.
     *
     * @param enabled flag indicating whether "Show all" item should be visible
     */
    public void setShowAllEnabled(boolean enabled) {
        header.setShowAllEnabled(enabled);
    }

    /**
     * @return true/false if "Show all" item is visible/not visible
     */
    public boolean isShowAllEnabled() {
        return header.isShowAllEnabled();
    }

    /**
     * Sets whether "Hide all" item should be visible.
     *
     * @param enabled flag indicating whether "Hide all" item should be visible
     */
    public void setHideAllEnabled(boolean enabled) {
        header.setHideAllEnabled(enabled);
    }

    /**
     * @return true/false if "Hide all" item is visible/not visible
     */
    public boolean isHideAllEnabled() {
        return header.isHideAllEnabled();
    }

    /**
     * Removes column visibility toggle item from dropdown menu by column key of referenced column.
     *
     * @param columnKey column key of data grid column
     */
    public void removeColumnItem(String columnKey) {
        Preconditions.checkNotNullArgument(columnKey);

        int removeItemIndex = getColumnItemIndexByColumnKey(columnKey);

        if (removeItemIndex == -1) {
            throw new IllegalArgumentException("Column item for column '%s' doesn't exist".formatted(columnKey));
        }
        ColumnItemImpl columnItem = columnItems.get(removeItemIndex);
        removeColumnItemFromMenu(columnItem);
        columnItems.remove(removeItemIndex);
        header.refresh();
    }

    protected void removeColumnItemFromMenu(ColumnItemImpl columnItem) {
        columnItem.removeColumnListeners();
        dropdownItem.getSubMenu().remove(columnItem.getMenuItem());
    }

    /**
     * @return index of column item or -1 if no item was found for the column key
     */
    protected int getColumnItemIndexByColumnKey(String columnKey) {
        return IntStream.range(0, columnItems.size())
                .filter(i -> columnItems.get(i).getColumn().getKey().equals(columnKey))
                .findAny()
                .orElse(-1);
    }

    /**
     * Removes column visibility toggle item from dropdown menu for specified referenced column.
     *
     * @param column referenced data grid column
     */
    public void removeColumnItem(DataGridColumn<?> column) {
        Preconditions.checkNotNullArgument(column);
        checkColumnOwner(column);

        removeColumnItem(column.getKey());
    }

    /**
     * @param columnKey column key of referenced data grid column
     * @return column visibility toggle item by column key of referenced data grid column
     */
    @Nullable
    public ColumnItem getColumnItem(String columnKey) {
        int columnItemIndex = getColumnItemIndexByColumnKey(columnKey);
        return columnItemIndex != -1 ? columnItems.get(columnItemIndex) : null;
    }

    /**
     * @return all column visibility toggle items
     */
    public List<ColumnItem> getColumnItems() {
        return Collections.unmodifiableList(columnItems);
    }

    /**
     * Removes all column visibility toggle items
     */
    public void removeAllColumnItems() {
        columnItems.forEach(this::removeColumnItemFromMenu);
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
            for (ColumnItemImpl columnItem : columnItems) {
                DataGridColumn<?> column = columnItem.getColumn();
                column.setVisible(true);
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

        public void setShowAllEnabled(boolean enabled) {
            showAllEnabled = enabled;
            if (showAllEnabled && showAllItem == null) {
                addShowAllItem();
            } else if (!showAllEnabled && showAllItem != null) {
                removeShowAllItem();
            }
        }

        protected void removeShowAllItem() {
            dropdownItem.getSubMenu().remove(showAllItem);
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

        protected void addHideAllItem() {
            String hideAllText = messages.getMessage(getClass(), "hideAllItem.text");
            //add first if showAllItem is disabled, otherwise - after showAllItem (second)
            int index = showAllItem != null ? 1 : 0;
            hideAllItem = dropdownItem.getSubMenu().addItemAtIndex(index, hideAllText, this::onHideAllClick);
            refresh();
        }

        protected void onHideAllClick(ClickEvent<?> event) {
            for (ColumnItemImpl columnItem : columnItems) {
                DataGridColumn<?> column = columnItem.getColumn();
                column.setVisible(false);
            }
        }

        protected void removeHideAllItem() {
            dropdownItem.getSubMenu().remove(hideAllItem);
            hideAllItem = null;
            refresh();
        }

        public boolean isHideAllEnabled() {
            return hideAllEnabled;
        }
    }

    /**
     * Represents an item which allows to toggle column visibility.
     */
    public interface ColumnItem {

        /**
         * @return referenced data grid column
         */
        DataGridColumn<?> getColumn();

        /**
         * @return whether the item is checked
         */
        boolean isChecked();

        /**
         * Sets item text.
         *
         * @param text item text
         */
        void setText(String text);

        /**
         * @return item text
         */
        String getText();

        /**
         * Toggles visibility of referenced data grid column.
         */
        void toggleVisibility();
    }

    protected static class ColumnItemImpl implements ColumnItem {

        protected DataGridColumn<?> column;
        protected JmixMenuItem menuItem;
        protected Registration columnVisibilityChangedRegistration;

        protected ColumnItemImpl(DataGridColumn<?> column, JmixMenuItem menuItem) {
            this.column = column;
            this.columnVisibilityChangedRegistration =
                    column.addColumnVisibilityChangedListener(e -> setChecked(e.isVisible()));
            menuItem.setChecked(column.isVisible());
            menuItem.addClickListener(e -> toggleVisibility());
            this.menuItem = menuItem;
        }

        @Override
        public DataGridColumn<?> getColumn() {
            return column;
        }

        public void setChecked(boolean checked) {
            menuItem.setChecked(checked);
        }

        @Override
        public boolean isChecked() {
            return menuItem.isChecked();
        }

        @Override
        public void setText(String text) {
            menuItem.setText(text);
        }

        @Override
        public String getText() {
            return menuItem.getText();
        }

        public JmixMenuItem getMenuItem() {
            return menuItem;
        }

        public void removeColumnListeners() {
            if (columnVisibilityChangedRegistration != null) {
                columnVisibilityChangedRegistration.remove();
                columnVisibilityChangedRegistration = null;
            }
        }

        @Override
        public void toggleVisibility() {
            column.setVisible(!column.isVisible());
        }
    }
}
