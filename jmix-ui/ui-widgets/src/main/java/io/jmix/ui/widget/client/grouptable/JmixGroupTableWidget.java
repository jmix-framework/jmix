/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.widget.client.grouptable;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import io.jmix.ui.widget.client.Tools;
import io.jmix.ui.widget.client.aggregation.AggregationInputFieldInfo;
import io.jmix.ui.widget.client.aggregation.TableAggregationRow;
import io.jmix.ui.widget.client.table.JmixScrollTableWidget;
import com.vaadin.client.*;
import com.vaadin.v7.client.ui.VScrollTable;
import com.vaadin.v7.shared.ui.table.TableConstants;

import java.util.*;

public class JmixGroupTableWidget extends JmixScrollTableWidget {

    public static final String CLASSNAME = "jmix-grouptable";
    public static final String GROUP_DIVIDER_COLUMN_KEY = "-1";
    public static final int GROUP_DIVIDER_WIDTH = 16;

    protected Set<String> groupColumns;

    protected boolean isAggregationEditable = false;

    public JmixGroupTableWidget() {
        addStyleName("jmix-grouptable");
    }

    public void updateGroupColumns(Set<String> groupColumns) {
        this.groupColumns = groupColumns;
    }

    @Override
    public void setColWidth(int colIndex, int w, boolean isDefinedWidth) {
        if (GROUP_DIVIDER_COLUMN_KEY.equals(getVisibleColOrder()[colIndex])) {
            w = GROUP_DIVIDER_WIDTH;
            isDefinedWidth = true;
        }

        super.setColWidth(colIndex, w, isDefinedWidth);
    }

    private void addGroupColumn(String colKey) {
        if (groupColumns == null) {
            groupColumns = new HashSet<>();
        }
        groupColumns.add(colKey);
    }

    private void removeGroupColumn(String colKey) {
        if (groupColumns != null) {
            groupColumns.remove(colKey);
            if (groupColumns.size() == 0)
                groupColumns = null;
        }
    }

    protected boolean isGroupColumn(String cid) {
        return groupColumns != null && groupColumns.contains(cid);
    }

    @Override
    protected int getVisibleColsCount(String[] strings) {
        return strings.length + 1;
    }

    @Override
    protected void updateHeaderColumns(String[] strings, int colIndex) {
        boolean dividerPainted = false;

        int i;
        for (i = 0; i < strings.length; i++) {
            final String cid = strings[i];
            if (!isGroupColumn(cid) && !dividerPainted) {
                //paint group columns divider
                visibleColOrder[colIndex] = GROUP_DIVIDER_COLUMN_KEY;
                tHead.enableColumn(GROUP_DIVIDER_COLUMN_KEY, colIndex++);
                dividerPainted = true;
            }
            visibleColOrder[colIndex] = cid;
            tHead.enableColumn(cid, colIndex);
            colIndex++;
        }

        if (!dividerPainted) {
            visibleColOrder[colIndex] = GROUP_DIVIDER_COLUMN_KEY;
            tHead.enableColumn(GROUP_DIVIDER_COLUMN_KEY, colIndex);
        }
    }

    @Override
    protected boolean isAggregationEditable() {
        return isAggregationEditable || super.isAggregationEditable();
    }

    @Override
    public void updateBody(UIDL uidl, int firstRow, int reqRows) {
        isAggregationEditable = false;

        super.updateBody(uidl, firstRow, reqRows);
    }

    @Override
    protected void updateFooterColumns(String[] strings, int colIndex) {
        boolean dividerPainted = false;

        int i;
        for (i = 0; i < strings.length; i++) {
            final String cid = strings[i];
            if (!isGroupColumn(cid) && !dividerPainted) {
                //paint group columns divider
                tFoot.enableColumn(GROUP_DIVIDER_COLUMN_KEY, colIndex++);
                dividerPainted = true;
            }
            tFoot.enableColumn(cid, colIndex);
            colIndex++;
        }
        if (!dividerPainted) {
            tFoot.enableColumn(GROUP_DIVIDER_COLUMN_KEY, colIndex);
        }
    }

    @Override
    protected void reOrderColumn(String columnKey, int newIndex) {
        // CAUTION This method copied from VScrollTable
        // Added grouping support

        int oldIndex = getColIndexByKey(columnKey);

        // Change header order
        tHead.moveCell(oldIndex, newIndex);

        // Change body order
        scrollBody.moveCol(oldIndex, newIndex);

        // Change footer order
        tFoot.moveCell(oldIndex, newIndex);

        /*
         * Build new columnOrder and update it to server Note that columnOrder
         * also contains collapsed columns so we cannot directly build it from
         * cells vector Loop the old columnOrder and append in order to new
         * array unless on moved columnKey. On new index also put the moved key
         * i == index on columnOrder, j == index on newOrder
         */

        // Grouping support
        final int groupDividerIndex = getColIndexByKey(GROUP_DIVIDER_COLUMN_KEY);
        if (isGroupColumn(columnKey)) {
            if (newIndex > 0 && newIndex >= groupDividerIndex) {
                removeGroupColumn(columnKey);
                newIndex--;
            }
        } else {
            if (newIndex <= groupDividerIndex) {
                addGroupColumn(columnKey);
            }
            if (newIndex > 0 && newIndex > groupDividerIndex) {
                newIndex--;
            }
        }

        String oldKeyOnNewIndex = visibleColOrder[newIndex];
        if (showRowHeaders) {
            newIndex--; // columnOrder don't have rowHeader
        }

        if (!GROUP_DIVIDER_COLUMN_KEY.equals(oldKeyOnNewIndex)) {
            for (int i = 0; i < columnOrder.length; i++) {
                if (columnOrder[i].equals(oldKeyOnNewIndex)) {
                    break; // break loop at target
                }
                if (isCollapsedColumn(columnOrder[i])) {
                    newIndex++;
                }
            }
        }

        // finally we can build the new columnOrder for server
        String[] newOrder = new String[columnOrder.length];
        for (int i = 0, j = 0; j < newOrder.length; i++) {
            if (j == newIndex) {
                newOrder[j] = columnKey;
                j++;
            }
            if (i == columnOrder.length) {
                break;
            }
            if (columnOrder[i].equals(columnKey)) {
                continue;
            }
            newOrder[j] = columnOrder[i];
            j++;
        }
        columnOrder = newOrder;
        // also update visibleColumnOrder
        int colIndex = showRowHeaders ? 1 : 0;
        boolean dividerPainted = false;
        for (int j = 0; j < newOrder.length; j++) {
            final String cid = newOrder[j];
            if (!isGroupColumn(cid) && !dividerPainted) {
                //paint group columns divider
                visibleColOrder[colIndex++] = GROUP_DIVIDER_COLUMN_KEY;
                dividerPainted = true;
            }
            if (!isCollapsedColumn(cid)) {
                visibleColOrder[colIndex++] = cid;
            }
        }

        // Grouping support
        if (groupColumns != null) {
            // collect new grouped columns
            final String[] groupedColumns = new String[groupColumns.size()];
            for (int i = 0, j = 0; i < columnOrder.length; i++) {
                final String colKey = columnOrder[i];
                if (isGroupColumn(colKey)) {
                    groupedColumns[j++] = colKey;
                }
                if (j == groupedColumns.length) {
                    break;
                }
            }
            client.updateVariable(paintableId, "groupedcolumns", groupedColumns, false);
        }

        // CAUTION we use immediate update of column order
        client.updateVariable(paintableId, "columnorder", columnOrder, true);
        if (client.hasEventListeners(this,
                TableConstants.COLUMN_REORDER_EVENT_ID)) {
            client.sendPendingVariableChanges();
        }
    }

    @Override
    protected boolean checkColumnForUpdateWidth(VScrollTable.HeaderCell cell) {
        return !GROUP_DIVIDER_COLUMN_KEY.equals(cell.getColKey());
    }

    @Override
    protected VScrollTable.VScrollTableBody createScrollBody() {
        return new JmixGroupTableBody();
    }

    @Override
    protected VScrollTable.TableHead createTableHead() {
        return new GroupTableHead();
    }

    @Override
    protected VScrollTable.TableFooter createTableFooter() {
        return new GroupTableFooter();
    }

    @Override
    protected TableAggregationRow createAggregationRow() {
        return new TableAggregationRow(this) {
            protected boolean isDividerAdded = false;
            protected int dividerColumnIndex;

            @Override
            protected boolean addSpecificCell(String columnId, int colIndex) {
                if (GROUP_DIVIDER_COLUMN_KEY.equals(columnId)) {
                    addCell("", aligns[colIndex], CLASSNAME + "-group-divider", false);
                    this.isDividerAdded = true;
                    this.dividerColumnIndex = colIndex;
                    int w = JmixGroupTableWidget.this.getColWidth(getColKeyByIndex(colIndex));
                    super.setCellWidth(colIndex, w);
                    return true;
                }
                if (showRowHeaders && colIndex == 0) {
                    addCell("", aligns[colIndex], "", false);
                    int w = JmixGroupTableWidget.this.getColWidth(getColKeyByIndex(colIndex));
                    super.setCellWidth(colIndex, w);
                    return true;
                }

                return super.addSpecificCell(columnId, colIndex);
            }

            @Override
            protected boolean isAggregationEditable(UIDL uidl, int colIndex) {
                // we shouldn't create cell with field if it is group column
                if (isDividerAdded && colIndex <= dividerColumnIndex) {
                    return false;
                }

                return super.isAggregationEditable(uidl, colIndex) && this.isDividerAdded;
            }
        };
    }

    @Override
    protected boolean handleNavigation(int keycode, boolean ctrl, boolean shift) {
        if (focusedRow instanceof JmixGroupTableBody.JmixGroupTableGroupRow) {
            JmixGroupTableBody.JmixGroupTableGroupRow groupRow = (JmixGroupTableBody.JmixGroupTableGroupRow) focusedRow;

            if (keycode == getNavigationLeftKey()) {
                if (groupRow.expanded) {
                    client.updateVariable(paintableId, "collapse", groupRow.getGroupKey(), true);
                }
                return true;
            }
            if (keycode == getNavigationRightKey()) {
                if (!groupRow.expanded) {
                    client.updateVariable(paintableId, "expand", groupRow.getGroupKey(), true);
                }
                return true;
            }
        }
        return super.handleNavigation(keycode, ctrl, shift);
    }

    @Override
    public boolean isGenericRow(Widget rowWidget) {
        return super.isGenericRow(rowWidget) && !(rowWidget instanceof JmixGroupTableBody.JmixGroupTableGroupRow);
    }

    @Override
    public boolean isCustomColumn(int colIndex) {
        return GROUP_DIVIDER_COLUMN_KEY.equals(getColKeyByIndex(colIndex));
    }

    public JmixGroupTableBody.JmixGroupTableGroupRow getRenderedGroupRowByKey(String key) {
        if (scrollBody != null) {
            Iterator<Widget> it = scrollBody.iterator();
            JmixGroupTableBody.JmixGroupTableGroupRow row;
            while (it.hasNext()) {
                Widget widget = it.next();
                if (widget instanceof JmixGroupTableBody.JmixGroupTableGroupRow) {
                    row = (JmixGroupTableBody.JmixGroupTableGroupRow) widget;
                    if (row.getGroupKey().equals(key)) {
                        return row;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void updateTextSelection() {
        super.updateTextSelection();

        if (isAggregationEditable && scrollBody != null) {
            Iterator<Widget> it = scrollBody.iterator();

            while (it.hasNext()) {
                Widget widget = it.next();

                if (widget instanceof JmixGroupTableBody.JmixGroupTableGroupRow) {
                    JmixGroupTableBody.JmixGroupTableGroupRow row =
                            (JmixGroupTableBody.JmixGroupTableGroupRow) widget;

                    if (row.isAggregationInputEditable()) {
                        for (AggregationInputFieldInfo info : row.getInputsList()) {
                            Tools.textSelectionEnable(info.getTd(), true);
                        }
                    }
                }
            }
        }
    }

    public void updateGroupRowsWithAggregation(UIDL uidl) {
        for (int i = 0; i < uidl.getChildCount(); i++) {
            UIDL child = uidl.getChildUIDL(i); // walk on rows
            if (child != null) {

                String groupKey = child.getStringAttribute("groupKey");
                JmixGroupTableBody.JmixGroupTableGroupRow row = getRenderedGroupRowByKey(groupKey);

                if (row == null) {
                    continue;
                }

                TableRowElement rowElement = row.getElement().cast();
                NodeList<TableCellElement> list = rowElement.getCells();
                if (list.getLength() == 0) {
                    continue;
                }

                int columnIndex = visibleColOrder.length - 1;
                UIDL updateUIDL = child.getChildByTagName("updateAggregation");

                // reverse walk on cells
                for (int updInd = updateUIDL.getChildCount() - 1; updInd >= 0; updInd--, columnIndex--) {
                    String columnId = visibleColOrder[columnIndex];
                    if (GROUP_DIVIDER_COLUMN_KEY.equals(columnId)) {
                        continue;
                    }

                    // TODO: workaround to avoid NPE
                    TableCellElement cell = list.getItem(columnIndex);
                    if (cell == null) {
                        continue;
                    }

                    Element divWrapper = cell.getFirstChildElement();

                    // check for input in column
                    if (divWrapper.getChildCount() != 0) {
                        if (isAggregationEditable(uidl, columnIndex)) {
                            InputElement inputElement = divWrapper.getChild(0).cast();
                            inputElement.setValue(updateUIDL.getChildString(updInd));
                        } else {
                            divWrapper.setInnerText(updateUIDL.getChildString(updInd));
                        }
                    }
                }
            }
        }
    }

    protected boolean isAggregationEditable(UIDL uidl, int colIndex) {
        UIDL colUidl = uidl.getChildByTagName("editableAggregationColumns");
        if (colUidl == null) {
            return false;
        }
        String colKey = getColKeyByIndex(colIndex);
        Iterator iterator = colUidl.getChildIterator();
        while (iterator.hasNext()) {
            Object uidlKey = iterator.next();
            if (uidlKey.equals(colKey)) {
                return true;
            }
        }
        return false;
    }

    protected class GroupTableHead extends JmixScrollTableHead {
        public GroupTableHead() {
            availableCells.put(GROUP_DIVIDER_COLUMN_KEY, new GroupDividerHeaderCell());
        }

        @Override
        public void clear() {
            super.clear();

            availableCells.put(GROUP_DIVIDER_COLUMN_KEY, new GroupDividerHeaderCell());
        }

        @Override
        protected void fillAdditionalUpdatedCells(HashSet<String> updated) {
            updated.add(GROUP_DIVIDER_COLUMN_KEY);
        }
    }

    protected class GroupTableFooter extends VScrollTable.TableFooter {
        public GroupTableFooter() {
            availableCells.put(GROUP_DIVIDER_COLUMN_KEY, new GroupDividerFooterCell());
        }

        @Override
        public void clear() {
            super.clear();
            availableCells.put(GROUP_DIVIDER_COLUMN_KEY, new GroupDividerFooterCell());
        }

        @Override
        protected void fillAdditionalUpdatedCells(HashSet<String> updated) {
            updated.add(GROUP_DIVIDER_COLUMN_KEY);
        }
    }

    protected class JmixGroupTableBody extends JmixScrollTableBody {

        @Override
        protected VScrollTable.VScrollTableBody.VScrollTableRow createRow(UIDL uidl, char[] aligns2) {
            if (uidl.hasAttribute("groupKey")) {
                return new JmixGroupTableGroupRow(uidl, aligns2); //creates a group row
            } else
                return new JmixGroupTableRow(uidl, aligns2);
        }

        protected class JmixGroupTableRow extends JmixScrollTableRow {
            protected TableCellElement groupDividerCell;
            protected boolean isDividerAdded = false;

            public JmixGroupTableRow(UIDL uidl, char[] aligns) {
                super(uidl, aligns);
            }

            @Override
            protected boolean addSpecificCell(Object columnId, int colIndex, char[] aligns) {
                if (GROUP_DIVIDER_COLUMN_KEY.equals(columnId)) {
                    addDividerCell(aligns[colIndex]);
                    return true;
                }
                return false;
            }

            public void addDividerCell(char align) {
                // String only content is optimized by not using Label widget
                final TableCellElement td = DOM.createTD().cast();
                this.groupDividerCell = td;
                initCellWithText("", align, "", false, false, null, td);
                td.addClassName(CLASSNAME + "-group-divider");
                this.isDividerAdded = true;
            }

            @Override
            protected void updateStyleNames(String primaryStyleName) {
                super.updateStyleNames(primaryStyleName);

                if (groupDividerCell != null) {
                    groupDividerCell.addClassName(CLASSNAME + "-group-divider");
                }
            }
        }

        protected class JmixGroupTableGroupRow extends JmixGroupTableRow {
            /*
            * introduce this difference to check that width of displayed cell is nearly equal to its set width
            * and was calculated correctly
            */
            protected static final int MAX_ROUNDING_DIFF = 5;

            protected Integer groupColIndex;
            protected String groupKey;
            protected boolean expanded;
            protected Boolean hasCells;
            protected Element expander;

            protected List<AggregationInputFieldInfo> inputsList;

            public JmixGroupTableGroupRow(UIDL uidl, char[] aligns) {
                super(uidl, aligns);
                selectable = false;
                addStyleName("jmix-group-row");
            }

            @Override
            protected void addCellsFromUIDL(UIDL uidl, char[] aligns, int colIndex, int visibleColumnIndex) {
                this.groupKey = uidl.getStringAttribute("groupKey");
                this.expanded = uidl.hasAttribute("expanded") && uidl.getBooleanAttribute("expanded");

                if (expanded) {
                    addStyleName("v-expanded");
                }

                int currentColIndex = colIndex;
                if (currentColIndex < getVisibleColOrder().length) {
                    String colKey = uidl.getStringAttribute("colKey");
                    while (currentColIndex < getVisibleColOrder().length
                            && !getVisibleColOrder()[currentColIndex].equals(colKey)) {
                        //draw empty cells
                        Element td = DOM.createTD();

                        final TableCellElement tdCell = td.cast();
                        initCellWithText("", ALIGN_LEFT, "", false, true, null, tdCell);

                        td.setClassName(JmixGroupTableWidget.this.getStylePrimaryName() + "-cell-content");
                        td.addClassName(JmixGroupTableWidget.this.getStylePrimaryName() + "-cell-stub");
                        DOM.appendChild(getElement(), td);

                        currentColIndex++;
                    }
                } else {
                    throw new ArrayIndexOutOfBoundsException("Group rendering error");
                }

                //paint "+" and group caption
                this.groupColIndex = currentColIndex;

                addGroupCell(uidl.getStringAttribute("groupCaption"));

                currentColIndex++;

                if (uidl.getChildCount() > 0) {
                    Iterator cells = uidl.getChildIterator();
                    while (currentColIndex < getVisibleColOrder().length) {
                        String columnId = getVisibleColOrder()[currentColIndex];

                        if (GROUP_DIVIDER_COLUMN_KEY.equals(columnId)) { //paint cell for columns group divider
                            addDividerCell(aligns[currentColIndex]);
                        } else if (cells.hasNext()) {
                            final Object cell = cells.next();

                            String style = "";
                            if (uidl.hasAttribute("style-" + columnId)) {
                                style = uidl.getStringAttribute("style-" + columnId);
                            }

                            String description = null;
                            if (uidl.hasAttribute("descr-" + columnId)) {
                                description = uidl.getStringAttribute("descr-"
                                        + columnId);
                            }

                            boolean sorted = tHead.getHeaderCell(currentColIndex).isSorted();

                            if (isAggregationEditable(uidl, currentColIndex) && isDividerAdded) {
                                addCellWithField((String) cell, ALIGN_LEFT, currentColIndex);
                            } else if (cell instanceof String) {
                                addCell(uidl, cell.toString(), aligns[currentColIndex], style,
                                        isRenderHtmlInCells(), sorted, description);
                            } else {
                                final ComponentConnector cellContent = client
                                        .getPaintable((UIDL) cell);

                                addCell(uidl, cellContent.getWidget(), aligns[currentColIndex],
                                        style, sorted, description);
                            }
                        }

                        currentColIndex++;
                    }
                    hasCells = true;
                } else {
                    TableCellElement td = getElement().getLastChild().cast();
                    int colSpan = visibleColOrder.length - groupColIndex;
                    td.setColSpan(colSpan);
                    hasCells = false;
                }

                // set focus to input if we pressed `ENTER`
                String focusColumnKey = uidl.getStringAttribute("focusInput");
                if (focusColumnKey != null && inputsList != null) {
                    for (AggregationInputFieldInfo info : inputsList) {
                        if (info.getColumnKey().equals(focusColumnKey)) {
                            info.getInputElement().focus();
                            break;
                        }
                    }
                }
            }

            @Override
            protected void setCellWidth(int cellIx, int w) {
                if (hasCells) {
                    super.setCellWidth(cellIx, w);
                } else {
                    calcAndSetWidthForSpannedCell();
                }
            }

            @Override
            protected void initCellWidths() {
                if (!hasCells) {
                    setSpannedRowWidthAfterDOMFullyInited();
                } else {
                    super.initCellWidths();
                }
            }

            public boolean isAggregationInputEditable() {
                if (inputsList == null) {
                    return false;
                }
                return !inputsList.isEmpty();
            }

            public List<AggregationInputFieldInfo> getInputsList() {
                return inputsList;
            }

            private void setSpannedRowWidthAfterDOMFullyInited() {
                // Defer setting width on spanned columns to make sure that
                // they are added to the DOM before trying to calculate
                // widths.
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        calcAndSetWidthForSpannedCell();
                    }
                });
            }

            private void calcAndSetWidthForSpannedCell() {
                int cells = tHead.getVisibleCellCount();
                for (int i = 0; i < groupColIndex; i++) {
                    int w = JmixGroupTableWidget.this.getColWidth(getColKeyByIndex(i));
                    if (w < 0) {
                        w = 0;
                    }
                    super.setCellWidth(i, w);
                }

                Element tr = getElement();

                int totalSpannedWidth = 0;
                for (int i = groupColIndex; i < cells; i++) {
                    VScrollTable.HeaderCell headerCell = tHead.getHeaderCell(i);

                    int headerWidth = headerCell.getOffsetWidth() > headerCell.getWidth() + MAX_ROUNDING_DIFF
                            ? headerCell.getWidth()
                            : headerCell.getOffsetWidth();

                    totalSpannedWidth += headerWidth;
                }

                Element td = DOM.getChild(tr, DOM.getChildCount(tr) - 1);

                Style wrapperStyle = td.getFirstChildElement().getStyle();
                WidgetUtil.setWidthExcludingPaddingAndBorder(td, totalSpannedWidth, 13, false);

                int wrapperWidth;
                ComputedStyle style = new ComputedStyle(td);
                if (style.getPaddingWidth() > 1.0) {
                    // this is applied for havana theme, because it has vertical padding
                    // for cell-container and width of TD element must be less, then whole row
                    String tdWidthPx = td.getStyle().getWidth().replace("px", "");
                    wrapperWidth = Integer.parseInt(tdWidthPx);
                } else {
                    // this is applied for halo theme, because it hasn't vertical padding
                    // for cell-container and width of TD element must be equal to whole row - 1px
                    // 1px is the padding-left of :first-child
                    wrapperWidth = totalSpannedWidth - 1;
                }

                if (BrowserInfo.get().isWebkit() || BrowserInfo.get().isOpera10()) {
                    /*
                     * Some versions of Webkit and Opera ignore the width definition of zero width table cells.
                     * Instead, use 1px and compensate with a negative margin.
                     */
                    if (totalSpannedWidth == 0) {
                        wrapperWidth = 1;
                        wrapperStyle.setMarginRight(-1, Style.Unit.PX);
                    } else {
                        wrapperStyle.clearMarginRight();
                    }
                }

                wrapperStyle.setPropertyPx("width", wrapperWidth);
            }

            protected void addGroupCell(String text) {
                // String only content is optimized by not using Label widget
                Element tdElement = DOM.createTD();
                final TableCellElement td = tdElement.cast();
                initCellWithText(text, ALIGN_LEFT, "", false, true, null, td);

                // Enhance DOM for table cell
                Element container = (Element) td.getChild(0);
                String containerInnerHTML = container.getInnerHTML();

                container.setInnerHTML("");

                expander = DOM.createDiv();
                expander.setInnerHTML("&nbsp;");

                expander.setClassName(CLASSNAME + "-group-cell-expander");
                DOM.appendChild(container, expander);

                Element contentDiv = DOM.createDiv();
                contentDiv.setInnerHTML(containerInnerHTML);

                contentDiv.setClassName(CLASSNAME + "-float");
                DOM.appendChild(container, contentDiv);
            }

            protected void addCellWithField(String text, char align, int colIndex) {
                isAggregationEditable = true;

                final TableCellElement td = DOM.createTD().cast();
                initCellWithText(text, align, "", false, true, null, td);

                // Enhance DOM for table cell
                Element container = (Element) td.getChild(0);
                container.setInnerHTML("");

                InputElement inputElement = DOM.createInputText().cast();
                inputElement.setValue(text);
                inputElement.addClassName("v-textfield v-widget");
                inputElement.addClassName("jmix-group-aggregation-textfield");
                Style elemStyle = inputElement.getStyle();
                elemStyle.setWidth(100, Style.Unit.PCT);
                DOM.appendChild(container, inputElement);

                HeaderCell headerCell = getHead().getHeaderCell(colIndex);
                if (headerCell != null) {
                    String headerCellJTEstId = headerCell.getElement().getAttribute("j-test-id");
                    if (headerCellJTEstId != null && !headerCellJTEstId.isEmpty()) {
                        inputElement.setAttribute("j-test-id", "aggregationField");
                    }
                }

                if (inputsList == null) {
                    inputsList = new ArrayList<>();
                }
                inputsList.add(new AggregationInputFieldInfo(text, getColKeyByIndex(colIndex), inputElement, td));

                DOM.sinkEvents(inputElement, Event.ONCHANGE | Event.ONKEYDOWN);

                Tools.textSelectionEnable(td, true);
            }

            @Override
            public void onBrowserEvent(Event event) {
                final Element targetElement = DOM.eventGetTarget(event);
                switch (DOM.eventGetType(event)) {
                    case Event.ONCLICK:
                        if (BrowserInfo.get().getWebkitVersion() > 0
                                && targetElement.getPropertyBoolean("__cell")) {
                            scrollBodyPanel.setFocus(true);
                        }
                        setRowFocus(this);

                        // we shouldn't do expand or collapse by click on input field
                        Element inputElement = Element.as(event.getEventTarget());
                        AggregationInputFieldInfo inputInfo = getAggregationInputInfo(inputElement);
                        if (inputInfo != null && inputInfo.getColumnKey() != null) {
                            break;
                        }

                        if ((event.getCtrlKey() || event.getMetaKey())
                                && !event.getAltKey() && !event.getShiftKey()) {
                            handleRowCtrlClick(event);
                        } else {
                            if (event.getEventTarget().cast() != expander || isSingleSelectMode()) {
                                deselectAll();
                                sendSelectedRows(false);
                            }

                            handleRowClick(event);
                        }
                        break;
                    case Event.ONKEYDOWN:
                        if (event.getKeyCode() == KeyCodes.KEY_ENTER &&
                                _delegate.groupAggregationInputHandler != null) {
                            Element sourceElement = Element.as(event.getEventTarget());
                            AggregationInputFieldInfo info = getAggregationInputInfo(sourceElement);
                            if (info != null) {
                                String columnKey = info.getColumnKey();
                                String value = info.getInputElement().getValue();
                                info.setFocused(true);

                                if (columnKey != null) {
                                    _delegate.groupAggregationInputHandler.onInputChange(columnKey, getGroupKey(), value, true);
                                }
                            }
                        }
                        break;
                    case Event.ONCHANGE:
                        if (_delegate.groupAggregationInputHandler != null) {
                            Element sourceElement = Element.as(event.getEventTarget());
                            AggregationInputFieldInfo info = getAggregationInputInfo(sourceElement);
                            if (info != null) {
                                String columnKey = info.getColumnKey();
                                String value = info.getInputElement().getValue();
                                // do not send event, cause it was sent with `ENTER` key event
                                if (info.isFocused()) {
                                    info.setFocused(false);
                                    return;
                                }

                                if (columnKey != null) {
                                    _delegate.groupAggregationInputHandler.onInputChange(columnKey, getGroupKey(), value, false);
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            protected AggregationInputFieldInfo getAggregationInputInfo(Element input) {
                if (inputsList == null) {
                    return null;
                }

                for (AggregationInputFieldInfo info : inputsList) {
                    if (info.getInputElement().isOrHasChild(input)) {
                        return info;
                    }
                }
                return null;
            }

            protected void handleRowCtrlClick(Event event) {
                client.updateVariable(paintableId, "expandAllInGroup", getGroupKey(), true);
                DOM.eventCancelBubble(event, true);
            }

            protected void handleRowClick(Event event) {
                if (expanded) {
                    client.updateVariable(paintableId, "collapse", getGroupKey(), true);
                } else {
                    client.updateVariable(paintableId, "expand", getGroupKey(), true);
                }

                DOM.eventCancelBubble(event, true);
            }

            @Override
            protected boolean handleClickEvent(Event event, com.google.gwt.dom.client.Element targetTdOrTr,
                                               boolean immediate) {
                return false;
            }

            @Override
            public void showContextMenu(Event event) {
                //do nothing
            }

            public String getGroupKey() {
                return groupKey;
            }

            public boolean isExpanded() {
                return expanded;
            }
        }

        @Override
        public void moveCol(int oldIndex, int newIndex) {
            //do nothing, columns reordering will be process on the server
        }

        @Override
        public int getAvailableWidth() {
            // fix for Halo based themes #PL-4570
            // it does not set real width of group column to divider cell element

            int availableWidth = super.getAvailableWidth();
            VScrollTable.HeaderCell headerCell = tHead.getHeaderCell(GROUP_DIVIDER_COLUMN_KEY);
            int groupCellWidth = headerCell.getOffsetWidth();
            Style headCellStyle = headerCell.getElement().getStyle();

            if (!(groupCellWidth + "px").equals(headCellStyle.getWidth())) {
                if (availableWidth - groupCellWidth > 0) {
                    availableWidth -= groupCellWidth;
                }
            }

            return availableWidth;
        }

        @Override
        public boolean isGeneratedRow(Widget row) {
            return row instanceof JmixGroupTableGroupRow;
        }
    }

    protected class GroupDividerHeaderCell extends JmixScrollTableHeaderCell {
        public GroupDividerHeaderCell() {
            super(GROUP_DIVIDER_COLUMN_KEY, null);
            addStyleName(CLASSNAME + "-group-divider-header");
        }

        @Override
        protected void updateStyleNames(String primaryStyleName) {
            super.updateStyleNames(primaryStyleName);
            addStyleName(CLASSNAME + "-group-divider-header");
        }

        @Override
        public void onBrowserEvent(Event event) {
            //do nothing
        }

        @Override
        protected void handleCaptionEvent(Event event) {
            //do nothing
        }
    }

    protected class GroupDividerFooterCell extends VScrollTable.FooterCell {

        public GroupDividerFooterCell() {
            super(GROUP_DIVIDER_COLUMN_KEY, null);
            addStyleName(CLASSNAME + "-group-divider-footer");
        }

        @Override
        protected void updateStyleNames(String primaryStyleName) {
            super.updateStyleNames(primaryStyleName);
            addStyleName(CLASSNAME + "-group-divider-footer");
        }

        @Override
        public void onBrowserEvent(Event event) {
            //do nothing
        }

        @Override
        protected void handleCaptionEvent(Event event) {
            //do nothing
        }
    }
}
