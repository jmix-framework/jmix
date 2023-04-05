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

package io.jmix.ui.widget.client.table;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.ComputedStyle;
import com.vaadin.client.Focusable;
import com.vaadin.client.UIDL;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.ui.Action;
import com.vaadin.client.ui.ShortcutActionHandler;
import com.vaadin.client.ui.VEmbedded;
import com.vaadin.v7.client.ui.VLabel;
import com.vaadin.v7.client.ui.VScrollTable;
import com.vaadin.v7.client.ui.VTextField;
import com.vaadin.v7.shared.ui.table.TableConstants;
import io.jmix.ui.widget.client.Tools;
import io.jmix.ui.widget.client.aggregation.TableAggregationRow;
import io.jmix.ui.widget.client.image.JmixImageWidget;
import io.jmix.ui.widget.client.tableshared.TableEmptyState;
import io.jmix.ui.widget.client.tableshared.TableWidget;
import io.jmix.ui.widget.client.tableshared.TableWidgetDelegate;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.jmix.ui.widget.client.Tools.isAnyModifierKeyPressed;
import static io.jmix.ui.widget.client.tableshared.TableWidgetDelegate.TABLE_CLICKABLE_CELL_CLASSNAME;
import static io.jmix.ui.widget.client.tableshared.TableWidgetDelegate.TABLE_CLICKABLE_CELL_CONTENT_CLASSNAME;
import static io.jmix.ui.widget.client.tableshared.TableWidgetDelegate.TABLE_CLICKABLE_TEXT_CLASSNAME;
import static io.jmix.ui.widget.client.tableshared.TableWidgetDelegate.WIDGET_CELL_CLASSNAME;

public class JmixScrollTableWidget extends VScrollTable implements TableWidget {

    public TableWidgetDelegate _delegate = new TableWidgetDelegate(this, this);

    protected JmixScrollTableWidget() {
        // handle shortcuts
        DOM.sinkEvents(getElement(), Event.ONKEYDOWN);

        hideColumnControlAfterClick = false;
    }

    @Override
    public void sizeInit() {
        super.sizeInit();

        tHead.enableBrowserIntelligence();
    }

    /**
     * Adds right padding for header and aggregation row (if visible) to compensate
     * table body vertical scroll bar.
     *
     * @param willHaveScrollbar defines whether table body will have scroll bar
     */
    @SuppressWarnings("ConstantConditions")
    protected void toggleScrollBarSpacer(boolean willHaveScrollbar) {
        com.google.gwt.user.client.Element headerWrapper = tHead.getElement();
        Element header = headerWrapper.getFirstChildElement();

        com.google.gwt.user.client.Element aggregationRowWrapper = null;
        Element aggregationRow = null;

        if (_delegate.isAggregationVisible()) {
            aggregationRowWrapper = _delegate.aggregationRow.getElement();
            aggregationRow = aggregationRowWrapper.getFirstChildElement();
        }

        if (willHaveScrollbar) {
            String scrollBarWidth = WidgetUtil.getNativeScrollbarSize() + "px";

            String borderColor = new ComputedStyle(headerWrapper)
                    .getProperty("borderRightColor");
            String borderRightStyle = "1px solid " + borderColor;

            headerWrapper.getStyle()
                    .setProperty("paddingRight", scrollBarWidth);
            header.getStyle()
                    .setProperty("borderRight", borderRightStyle);

            if (_delegate.isAggregationVisible()) {
                aggregationRowWrapper.getStyle()
                        .setProperty("paddingRight", scrollBarWidth);
                aggregationRow.getStyle()
                        .setProperty("borderRight", borderRightStyle);
            }
        } else {
            headerWrapper.getStyle()
                    .setProperty("paddingRight", "0px");
            header.getStyle()
                    .setProperty("borderRight", "0px");

            if (_delegate.isAggregationVisible()) {
                aggregationRowWrapper.getStyle()
                        .setProperty("paddingRight", "0px");
                aggregationRow.getStyle()
                        .setProperty("borderRight", "0px");
            }
        }
    }

    @Override
    protected VScrollTableBody.VScrollTableRow getNextRowToFocus(VScrollTableBody.VScrollTableRow currentRow, int offset) {
        // Support select first N rows by Shift+Click #PL-3267
        if (focusedRow == currentRow && !focusedRow.isSelected()) {
            if (currentRow instanceof JmixScrollTableBody.JmixScrollTableRow) {
                JmixScrollTableBody.JmixScrollTableRow row = (JmixScrollTableBody.JmixScrollTableRow) currentRow;
                if (row.isSelectable()) {
                    return focusedRow;
                }
            }
        }

        return super.getNextRowToFocus(currentRow, offset);
    }

    @Override
    protected boolean needToSelectFocused(VScrollTableBody.VScrollTableRow currentRow) {
        // Support select first N rows by Shift+Click #PL-3267
        return currentRow == focusedRow && (!focusedRow.isSelected());
    }

    @Override
    public void scheduleLayoutForChildWidgets() {
        _delegate.scheduleLayoutForChildWidgets();
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);

        final int type = DOM.eventGetType(event);
        if (type == Event.ONKEYDOWN && _delegate.shortcutHandler != null) {
            _delegate.shortcutHandler.handleKeyboardEvent(event);
        }
    }

    public void setShortcutActionHandler(ShortcutActionHandler handler) {
        _delegate.shortcutHandler = handler;
    }

    @Override
    public ShortcutActionHandler getShortcutActionHandler() {
        return _delegate.shortcutHandler;
    }

    public void setPresentationsMenu(Widget presentationsMenu) {
        if (_delegate.presentationsMenu != presentationsMenu) {
            Style presentationsIconStyle = ((JmixScrollTableHead) tHead).presentationsEditIcon.getElement().getStyle();
            if (presentationsMenu == null) {
                presentationsIconStyle.setDisplay(Style.Display.NONE);
            } else {
                presentationsIconStyle.setDisplay(Style.Display.BLOCK);
            }
        }
        _delegate.presentationsMenu = presentationsMenu;
    }

    @Override
    protected VScrollTableBody createScrollBody() {
        return new JmixScrollTableBody();
    }

    @Override
    public boolean handleBodyContextMenu(int left, int top) {
        if (_delegate.contextMenuEnabled) {
            if (_delegate.customContextMenu == null) {
                return super.handleBodyContextMenu(left, top);
            } else if (enabled) {
                _delegate.showContextMenuPopup(left, top);

                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isShowBrowserContextMenu(Event event) {
        if (event != null) {
            EventTarget target = event.getEventTarget();
            if (Element.is(target)) {
                Element element = Element.as(target);
                Widget widget = WidgetUtil.findWidget(element);
                if (widget instanceof TextBoxBase) {
                    return true;
                }
            }
        }

        return super.isShowBrowserContextMenu(event);
    }

    @Override
    public void onFocus(FocusEvent event) {
        super.onFocus(event);

        addStyleDependentName("body-focus");
    }

    @Override
    public void onBlur(BlurEvent event) {
        super.onBlur(event);

        removeStyleDependentName("body-focus");
    }

    @Override
    protected boolean isAggregationEditable() {
        if (_delegate.aggregationRow != null) {
            return _delegate.aggregationRow.isAggregationRowEditable();
        }
        return false;
    }

    @Override
    protected int getDynamicBodyHeight() {
        if (totalRows <= 0) {
            return (int) Math.round(scrollBody.getRowHeight(true));
        }

        return (int) Math.round(totalRows * scrollBody.getRowHeight(true));
    }

    @Override
    public boolean isUseSimpleModeForTouchDevice() {
        return Tools.isUseSimpleMultiselectForTouchDevice();
    }

    @Override
    protected boolean isAllowSingleSelectToggle() {
        return BrowserInfo.get().isTouchDevice() && Tools.isUseSimpleMultiselectForTouchDevice();
    }

    @Override
    public TableHead getHead() {
        return tHead;
    }

    @Override
    public String[] getVisibleColOrder() {
        return visibleColOrder;
    }

    @Override
    public String getColKeyByIndex(int index) {
        return super.getColKeyByIndex(index);
    }

    @Override
    public int getColWidth(String colKey) {
        return super.getColWidth(colKey);
    }

    @Override
    public void setColWidth(int colIndex, int w, boolean isDefinedWidth) {
        if (_delegate.isAggregationVisible()
                && _delegate.aggregationRow.isInitialized()) {
            _delegate.aggregationRow.setCellWidth(colIndex, w);
        }

        super.setColWidth(colIndex, w, isDefinedWidth);
    }

    @Override
    public boolean isTextSelectionEnabled() {
        return _delegate.textSelectionEnabled;
    }

    @Override
    public List<Widget> getRenderedRows() {
        return ((JmixScrollTableBody) scrollBody).getRenderedRows();
    }

    @Override
    public void forceReassignColumnWidths() {
        toggleScrollBarSpacer(willHaveScrollbars());

        int visibleCellCount = tHead.getVisibleCellCount();
        for (int i = 0; i < visibleCellCount; i++) {
            HeaderCell hcell = tHead.getHeaderCell(i);
            reassignHeaderCellWidth(i, hcell, hcell.getMinWidth());
        }
    }

    @Override
    protected void reassignHeaderCellWidth(int colIndex, HeaderCell hcell, int minWidth) {
        // it means that column is not visible
        if (colIndex < 0)
            return;

        _delegate.reassignHeaderCellWidth(colIndex, hcell, minWidth);
    }

    protected void reassignAggregationColumnWidths() {
        if (_delegate.isAggregationVisible()) {
            for (int i = 0; i < visibleColOrder.length; i++) {
                int colWidth = getColWidth(visibleColOrder[i]);
                _delegate.aggregationRow.setCellWidth(i, colWidth);
            }
        }
    }

    @Override
    public boolean isCustomColumn(int colIndex) {
        return false;
    }

    @Override
    public boolean isGenericRow(Widget rowWidget) {
        return rowWidget instanceof VScrollTableBody.VScrollTableRow;
    }

    @Override
    public int getAdditionalRowsHeight() {
        if (_delegate.isAggregationVisible()) {
            return _delegate.aggregationRow.getOffsetHeight();
        }
        return 0;
    }

    @Override
    protected TableHead createTableHead() {
        return new JmixScrollTableHead();
    }

    public void updateTextSelection() {
        Tools.textSelectionEnable(scrollBody.getElement(), _delegate.textSelectionEnabled);
    }

    @Override
    protected void onDetach() {
        super.onDetach();

        if (_delegate.presentationsEditorPopup != null) {
            _delegate.presentationsEditorPopup.hide();
        }

        if (_delegate.customContextMenuPopup != null) {
            _delegate.customContextMenuPopup.hide();
        }
    }

    protected void updateAggregationRow(UIDL uidl) {
        if (_delegate.aggregationRow == null) {
            _delegate.aggregationRow = createAggregationRow();
            _delegate.aggregationRow.setTotalAggregationInputHandler(_delegate.totalAggregationInputHandler);
            insert(_delegate.aggregationRow, getWidgetIndex(scrollBodyPanel));
        }
        if (_delegate.isAggregationVisible()) {
            _delegate.aggregationRow.updateFromUIDL(uidl);
            _delegate.aggregationRow.setHorizontalScrollPosition(scrollLeft);

            reassignAggregationColumnWidths();
        }
    }

    public void setAggregationRowVisible(boolean visible) {
        if (_delegate.aggregationRow != null
                && _delegate.aggregationRow.isVisible() != visible) {
            _delegate.aggregationRow.setVisible(visible);

            forceReassignColumnWidths();
        }
    }

    protected void showEmptyState(boolean show) {
        if (show) {
            if (_delegate.tableEmptyState == null) {
                _delegate.tableEmptyState = new TableEmptyState();
            }

            Element emptyState = _delegate.tableEmptyState.getElement();
            if (!scrollBodyPanel.getElement().isOrHasChild(emptyState)) {
                scrollBodyPanel.getElement().appendChild(emptyState);
            }
        } else if (_delegate.tableEmptyState != null) {
            _delegate.tableEmptyState.getElement().removeFromParent();
            _delegate.tableEmptyState = null;
        }
    }

    protected TableAggregationRow createAggregationRow() {
        return new TableAggregationRow(this) {
            @Override
            protected boolean addSpecificCell(String columnId, int colIndex) {
                if (showRowHeaders && colIndex == 0) {
                    addCell("", aligns[colIndex], "", false);
                    int w = tableWidget.getColWidth(getColKeyByIndex(colIndex));
                    super.setCellWidth(colIndex, w);
                    return true;
                }

                return super.addSpecificCell(columnId, colIndex);
            }
        };
    }

    @Override
    public void onScroll(ScrollEvent event) {
        if (isLazyScrollerActive()) {
            return;
        }

        super.onScroll(event);

        if (_delegate.isAggregationVisible()) {
            _delegate.aggregationRow.setHorizontalScrollPosition(scrollLeft);
        }

        if (!enabled) {
            tHead.setHorizontalScrollPosition(scrollLeft);
            tFoot.setHorizontalScrollPosition(scrollLeft);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.isEnabled() != enabled) {
            this.enabled = enabled;
        }
    }

    @Override
    public String getSortDescendingLabel() {
        return _delegate.tableSortDescendingLabel;
    }

    @Override
    public String getSortAscendingLabel() {
        return _delegate.tableSortAscendingLabel;
    }

    @Override
    public String getSortResetLabel() {
        return _delegate.tableSortResetLabel;
    }

    @Override
    public Widget getOwner() {
        return JmixScrollTableWidget.this;
    }

    @Override
    public RowRequestHandler getRowRequestHandler() {
        return rowRequestHandler;
    }

    protected class JmixScrollTableHead extends TableHead {

        protected final SimplePanel presentationsEditIcon = GWT.create(SimplePanel.class);

        public JmixScrollTableHead() {
            Element iconElement = presentationsEditIcon.getElement();
            iconElement.setClassName("jmix-table-prefs-icon");
            iconElement.getStyle().setDisplay(Style.Display.NONE);

            Element columnSelector = (Element) getElement().getLastChild();
            DOM.insertChild(getElement(), iconElement, DOM.getChildIndex(getElement(), columnSelector));

            DOM.sinkEvents(iconElement, Event.ONCLICK);
        }

        protected Element getColumnSelector() {
            return columnSelector;
        }

        @Override
        public Action[] getActions() {
            Action[] tableActions = super.getActions();
            Action[] actions = new Action[tableActions.length + 2];
            actions[0] = new SelectAllAction(tableActions);
            actions[1] = new DeselectAllAction(tableActions);
            System.arraycopy(tableActions, 0, actions, 2, tableActions.length);
            return actions;
        }

        @Override
        protected int getIconsOffsetWidth() {
            Style presentationsIconStyle = presentationsEditIcon.getElement().getStyle();
            if ("none".equals(presentationsIconStyle.getDisplay())) {
                return super.getIconsOffsetWidth();
            }

            ComputedStyle cs = new ComputedStyle(presentationsEditIcon.getElement());
            double right = cs.getDoubleProperty("right");

            return (int) Math.ceil(right + cs.getWidth());
        }

        @Override
        protected int getOffsetForCaptionContainer(HeaderCell cell, int diff, int columnSelectorOffset) {
            int captionOffset;
            if (diff < columnSelectorOffset) {
                captionOffset = diff > 0 ?
                        columnSelectorOffset - diff :
                        columnSelectorOffset + diff;
            } else {
                captionOffset = columnSelectorOffset;
            }

            int cellWidth = cell.getOffsetWidth();
            int sortIndicatorWidth = cell.getSortIndicator().getOffsetWidth();
            int colResizeWidth = cell.getColResize().getOffsetWidth();

            // completed cell width that will be applied after setting offset
            int calculatedCellWidth = sortIndicatorWidth + colResizeWidth + captionOffset;

            // if caption and cell widths are not equal it means table fully loaded
            // after page refreshing and we should consider the caption width
            int captionWidth = cell.getCaptionContainer().getOffsetWidth();
            if (cellWidth != captionWidth) {
                calculatedCellWidth += captionWidth;
            }

            // if calculated width greater than exists, header cell will be broken
            // in this case we should reduce offset width;
            if (cellWidth < calculatedCellWidth) {
                captionOffset = captionOffset - (calculatedCellWidth - cellWidth);
            }

            return captionOffset;
        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);

            _delegate.showPresentationEditorPopup(event, presentationsEditIcon);
        }

        @Override
        public void setWidth(String width) {
            super.setWidth(width);

            /*
             * Have to set the same width to aggregation row when width is set in
             * com.vaadin.v7.client.ui.VScrollTable#setContentWidth
             */
            if (_delegate.isAggregationVisible()) {
                int offsetWidth = getElement().getOffsetWidth();
                if (offsetWidth > 0) {
                    _delegate.aggregationRow.setWidth(offsetWidth + "px");
                }
            }
        }

        @Override
        protected HeaderCell createHeaderCell(String cid, String caption) {
            return new JmixScrollTableHeaderCell(cid, caption);
        }

        @Override
        protected String getCustomHtmlAttributes(VisibleColumnAction action) {
            String colKey = action.getColKey();
            HeaderCell headerCell = getHeaderCell(colKey);
            if (headerCell != null) {
                String jmixId = headerCell.getElement().getAttribute("j-test-id");
                if (jmixId != null) {
                    return "j-test-id=\"cc_" + jmixId + "\"";
                }
            }

            return super.getCustomHtmlAttributes(action);
        }

        @Override
        protected boolean shouldRecalcColWidths(HeaderCell cell) {
            HeaderCell lastCell = getHeaderCell(tHead.getVisibleCellCount() - 1);
            return cell == lastCell && getIconsOffsetWidth() > 0;
        }

        protected class SelectAllAction extends Action {

            protected Action[] actions;

            public SelectAllAction(Action[] actions) {
                super(JmixScrollTableHead.this);
                this.actions = actions;
                setCaption(_delegate.selectAllLabel);
            }

            @Override
            public void execute() {
                if (actions == null || actions.length == 0) {
                    return;
                }

                for (Action action : actions) {
                    if (action instanceof VisibleColumnAction
                            && collapsedColumns.contains(((VisibleColumnAction) action).getColKey())) {
                        action.execute();
                    }
                }
            }

            @Override
            public String getHTML() {
                return "<span id=\"tableSelectAllAction\" class=\"v-off\">" +
                        super.getHTML() +
                        "</span>";
            }
        }

        protected class DeselectAllAction extends Action {

            protected Action[] actions;

            public DeselectAllAction(Action[] actions) {
                super(JmixScrollTableHead.this);
                this.actions = actions;
                setCaption(_delegate.deselectAllLabel);
            }

            @Override
            public void execute() {
                if (actions == null || actions.length == 0) {
                    return;
                }

                Action firstAction = actions[0];
                if (firstAction instanceof VisibleColumnAction
                        && collapsedColumns.contains(((VisibleColumnAction) firstAction).getColKey())) {
                    execute((VisibleColumnAction) firstAction);
                }

                for (int i = 1; i < actions.length; i++) {
                    Action action = actions[i];
                    if (action instanceof VisibleColumnAction
                            && !collapsedColumns.contains(((VisibleColumnAction) action).getColKey())) {
                        execute((VisibleColumnAction) action);
                    }
                }
            }

            protected void execute(VisibleColumnAction action) {
                boolean previousValue = action.isImmediateColumnAdjustment();

                // avoid immediately column adjustment
                action.setImmediateColumnAdjustment(false);

                action.execute();

                // restore value
                action.setImmediateColumnAdjustment(previousValue);
            }

            @Override
            public String getHTML() {
                return "<span id=\"tableDeselectAllAction\" class=\"v-off\">" +
                        super.getHTML() +
                        "</span>";
            }
        }
    }

    protected class JmixScrollTableHeaderCell extends HeaderCell {

        protected int sortClickCounter = 0;

        public JmixScrollTableHeaderCell(String colId, String headerText) {
            super(colId, headerText);

            Element sortIndicator = td.getChild(1).cast();
            DOM.sinkEvents(sortIndicator, Event.ONCONTEXTMENU | DOM.getEventsSunk(sortIndicator));
            Element captionContainer = td.getChild(2).cast();
            DOM.sinkEvents(captionContainer, Event.ONCONTEXTMENU | DOM.getEventsSunk(captionContainer));
        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);

            if (isEnabled() && event.getTypeInt() == Event.ONCONTEXTMENU) {
                if (getStyleName().contains("-header-sortable")) {
                    _delegate.showSortMenu(td, cid);
                }

                event.preventDefault();
                event.stopPropagation();
            }
        }

        @Override
        public void setText(String headerText) {
            if (headerText == null || "".equals(headerText)) {
                super.setText("&nbsp;");
                return;
            }

            TableWidgetDelegate delegate = JmixScrollTableWidget.this._delegate;
            boolean captionAsHtml = delegate.htmlCaptionColumns.contains(cid);
            if (captionAsHtml) {
                captionContainer.setInnerHTML(headerText);
            } else {
                captionContainer.setInnerText(headerText);
            }
        }

        @Override
        protected void sortColumn() {
            // CAUTION copied from superclass
            // Added ability to reset sort order
            boolean reloadDataFromServer = true;

            if (cid.equals(sortColumn)) {
                if (sortAscending) {
                    if (sortClickCounter < 2) {
                        // special case for initial revert sorting instead of reset sort order
                        if (sortClickCounter == 0) {
                            client.updateVariable(paintableId, "sortascending", false, false);
                        } else {
                            reloadDataFromServer = false;
                            sortClickCounter = 0;
                            sortColumn = null;

                            client.updateVariable(paintableId, "resetsortorder", "", true);
                        }
                    } else {
                        client.updateVariable(paintableId, "sortascending", false, false);
                    }
                } else {
                    if (sortClickCounter < 2) {
                        // special case for initial revert sorting instead of reset sort order
                        if (sortClickCounter == 0) {
                            client.updateVariable(paintableId, "sortascending", true, false);
                        } else {
                            reloadDataFromServer = false;
                            sortClickCounter = 0;
                            sortColumn = null;
                            sortAscending = true;

                            client.updateVariable(paintableId, "resetsortorder", "", true);
                        }
                    } else {
                        reloadDataFromServer = false;
                        sortClickCounter = 0;
                        sortColumn = null;
                        sortAscending = true;

                        client.updateVariable(paintableId, "resetsortorder", "", true);
                    }
                }
                sortClickCounter++;
            } else {
                sortClickCounter = 0;

                // set table sorted by this column
                client.updateVariable(paintableId, "sortcolumn", cid, false);
            }

            if (reloadDataFromServer) {
                // get also cache columns at the same request
                scrollBodyPanel.setScrollPosition(0);
                firstvisible = 0;
                rowRequestHandler.setReqFirstRow(0);
                rowRequestHandler.setReqRows((int) (2 * pageLength
                        * cacheRate + pageLength));
                rowRequestHandler.deferRowFetch(); // some validation +
                // defer 250ms
                rowRequestHandler.cancel(); // instead of waiting
                rowRequestHandler.run(); // run immediately
            }
        }

        @Override
        public void setWidth(int w, boolean ensureDefinedWidth) {
            super.setWidth(w, ensureDefinedWidth);

            Style style = this.getElement().getStyle();
            style.setProperty("minWidth", style.getWidth() + "px");
            style.setProperty("maxWidth", style.getWidth() + "px");
        }

        @Override
        public void setWidth(String width) {
            super.setWidth(width);

            Style style = this.getElement().getStyle();
            style.setProperty("minWidth", width);
            style.setProperty("maxWidth", width);
        }

        @Override
        protected boolean leaveRoomForSortIndicator() {
            HeaderCell lastCell = tHead.getHeaderCell(tHead.getVisibleCellCount() - 1);
            return this.equals(lastCell)
                    && (columnSelectorVisible() || presentationsEditIconVisible());
        }

        protected boolean presentationsEditIconVisible() {
            Style style = ((JmixScrollTableHead) tHead).presentationsEditIcon.getElement().getStyle();
            return !Style.Display.NONE.getCssName().equals(style.getDisplay());
        }

        protected boolean columnSelectorVisible() {
            Style style = ((JmixScrollTableHead) tHead).getColumnSelector().getStyle();
            return !Style.Display.NONE.getCssName().equals(style.getDisplay());
        }
    }

    protected class JmixScrollTableBody extends VScrollTableBody {

        protected Widget lastFocusedWidget = null;

        @Override
        protected VScrollTableRow createRow(UIDL uidl, char[] aligns2) {
            if (uidl.hasAttribute("gen_html")) {
                // This is a generated row.
                return new VScrollTableGeneratedRow(uidl, aligns2);
            }
            return new JmixScrollTableRow(uidl, aligns2);
        }

        protected class JmixScrollTableRow extends VScrollTableRow {

            protected String currentColumnKey = null;
            protected boolean selectable = true;

            public JmixScrollTableRow(UIDL uidl, char[] aligns) {
                super(uidl, aligns);
            }

            public List<Widget> getChildWidgets() {
                return childWidgets;
            }

            @Override
            protected void initCellWithWidget(final Widget w, char align,
                                              String style, boolean sorted, TableCellElement td) {
                super.initCellWithWidget(w, align, style, sorted, td);

                td.getFirstChildElement().addClassName(WIDGET_CELL_CLASSNAME);

                if (JmixScrollTableWidget.this.isSelectable()) {
                    // Support for #PL-2080
                    recursiveAddFocusHandler(w, w);
                }
            }

            protected void recursiveAddFocusHandler(final Widget w, final Widget topWidget) {
                if (w instanceof HasWidgets) {
                    for (Widget child : (HasWidgets) w) {
                        recursiveAddFocusHandler(child, topWidget);
                    }
                }

                if (w instanceof HasFocusHandlers) {
                    ((HasFocusHandlers) w).addFocusHandler(e ->
                            handleFocusAndClickEvents(e, topWidget));
                }
            }

            protected void handleFocusAndClickEvents(DomEvent e, Widget topWidget) {
                if (childWidgets.indexOf(topWidget) < 0) {
                    return;
                }

                lastFocusedWidget = ((Widget) e.getSource());

                if (!isSelected()) {
                    deselectAll();

                    toggleSelection();
                    setRowFocus(JmixScrollTableRow.this);

                    sendSelectedRows();
                }
            }

            protected void handleFocusForWidget() {
                if (lastFocusedWidget == null) {
                    return;
                }

                if (isSelected()) {
                    if (lastFocusedWidget instanceof Focusable) {
                        ((Focusable) lastFocusedWidget).focus();
                    } else if (lastFocusedWidget instanceof com.google.gwt.user.client.ui.Focusable) {
                        ((com.google.gwt.user.client.ui.Focusable) lastFocusedWidget).setFocus(true);
                    }
                }

                lastFocusedWidget = null;
            }

            @Override
            public void onBrowserEvent(Event event) {
                boolean isClickableCell = isJmixTableClickableCell(event);
                boolean isClickableCellText = isJmixTableClickableCellText(event);
                if (event.getTypeInt() == Event.ONMOUSEDOWN
                        && event.getButton() == NativeEvent.BUTTON_LEFT
                        && !isAnyModifierKeyPressed(event)
                        && (isClickableCell || isClickableCellText)) {

                    Element eventTarget = event.getEventTarget().cast();
                    Element elementTdOrTr = getElementTdOrTr(eventTarget);

                    int childIndex = DOM.getChildIndex(getElement(), elementTdOrTr);
                    String columnKey = tHead.getHeaderCell(childIndex).getColKey();
                    if (columnKey != null) {
                        WidgetUtil.TextRectangle rect = WidgetUtil.getBoundingClientRect(eventTarget);
                        _delegate.lastClickClientX = (int) Math.ceil(rect.getLeft());
                        _delegate.lastClickClientY = (int) Math.ceil(rect.getBottom());

                        if (_delegate.cellClickListener != null) {
                            _delegate.cellClickListener.onClick(columnKey, rowKey, isClickableCellText);

                            if (isClickableCellText) {
                                // stop the event propagation if the user clicked on cell text to avoid
                                // highlighting the table row
                                event.preventDefault();
                                event.stopPropagation();
                                return;
                            }
                        }
                    }
                }

                if (event.getTypeInt() == Event.ONDBLCLICK && isJmixTableClickableCellText(event)) {
                    return;
                }

                super.onBrowserEvent(event);

                if (event.getTypeInt() == Event.ONMOUSEDOWN) {
                    final Element eventTarget = event.getEventTarget().cast();
                    Widget widget = WidgetUtil.findWidget(eventTarget, null);

                    if (widget != this) {
                        if (widget instanceof Focusable
                                || widget instanceof com.google.gwt.user.client.ui.Focusable) {
                            lastFocusedWidget = widget;
                        }
                    }

                    handleFocusForWidget();
                }
            }

            protected boolean isJmixTableClickableCell(Event event) {
                Element eventTarget = event.getEventTarget().cast();
                Element elementTdOrTr = getElementTdOrTr(eventTarget);

                return elementTdOrTr != null
                        && TableCellElement.TAG_TD.equalsIgnoreCase(elementTdOrTr.getTagName())
                        && elementTdOrTr.hasClassName(TABLE_CLICKABLE_CELL_CONTENT_CLASSNAME);
            }

            protected boolean isJmixTableClickableCellText(Event event) {
                Element eventTarget = event.getEventTarget().cast();
                Element elementTdOrTr = getElementTdOrTr(eventTarget);
                if (elementTdOrTr != null
                        && TableCellElement.TAG_TD.equalsIgnoreCase(elementTdOrTr.getTagName())
                        && !elementTdOrTr.hasClassName(TABLE_CLICKABLE_TEXT_CLASSNAME)) {
                    // found <td>

                    if (SpanElement.TAG.equalsIgnoreCase(eventTarget.getTagName())
                            && eventTarget.hasClassName(TABLE_CLICKABLE_CELL_CLASSNAME)) {
                        // found <span class="jmix-table-clickable-cell">
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected Element getElementTdOrTr(Element eventTarget) {
                Widget widget = WidgetUtil.findWidget(eventTarget, null);
                Widget targetWidget = widget;

                if (widget != this) {
                    /*
                     * This is a workaround to make Labels, read only TextFields
                     * and Embedded in a Table clickable (see #2688). It is
                     * really not a fix as it does not work with a custom read
                     * only components (not extending VLabel/VEmbedded).
                     */
                    while (widget != null && widget.getParent() != this) {
                        widget = widget.getParent();
                    }

                    if (!(widget instanceof VLabel)
                            && !(widget instanceof com.vaadin.client.ui.VLabel)
                            && !(widget instanceof JmixImageWidget)
                            && !(widget instanceof VEmbedded)
                            && !(widget instanceof VTextField && ((VTextField) widget).isReadOnly())
                            && !(targetWidget instanceof Panel)
                            && !(targetWidget instanceof VEmbedded)
                            && !(targetWidget instanceof VLabel)
                            && !(targetWidget instanceof VTextField && ((VTextField) targetWidget).isReadOnly())) {
                        return null;
                    }
                }
                return getTdOrTr(eventTarget);
            }

            @Override
            protected void beforeAddCell(String columnKey) {
                currentColumnKey = columnKey;
            }

            @Override
            protected void afterAddCell(String columnKey) {
                currentColumnKey = null;
            }

            @Override
            protected void initCellWithText(String text, char align, String style, boolean textIsHTML,
                                            boolean sorted, String description, TableCellElement td) {
                super.initCellWithText(text, align, style, textIsHTML, sorted, description, td);

                final Element tdElement = td.cast();
                Tools.textSelectionEnable(tdElement, _delegate.textSelectionEnabled);

                if (_delegate.clickableTableColumns != null
                        && _delegate.clickableTableColumns.contains(currentColumnKey)) {
                    tdElement.addClassName(TABLE_CLICKABLE_CELL_CONTENT_CLASSNAME);
                    Element wrapperElement = tdElement.getFirstChildElement();
                    final Element clickableSpan = DOM.createSpan().cast();
                    clickableSpan.setClassName(TABLE_CLICKABLE_CELL_CLASSNAME);

                    clickableSpan.setInnerText(wrapperElement.getInnerText());

                    wrapperElement.removeAllChildren();
                    DOM.appendChild(wrapperElement, clickableSpan);
                }

                if (_delegate.multiLineCells) {
                    Style wrapperStyle = tdElement.getFirstChildElement().getStyle();
                    wrapperStyle.setWhiteSpace(Style.WhiteSpace.PRE_LINE);
                }
            }

            @Override
            protected void updateCellStyleNames(TableCellElement td, String primaryStyleName) {
                Element container = td.getFirstChild().cast();
                boolean isWidget = container.getClassName() != null
                        && container.getClassName().contains(WIDGET_CELL_CLASSNAME);

                super.updateCellStyleNames(td, primaryStyleName);

                if (isWidget) {
                    container.addClassName(WIDGET_CELL_CLASSNAME);
                }
            }

            @Override
            public void showContextMenu(Event event) {
                if (_delegate.contextMenuEnabled && enabled && (_delegate.customContextMenu != null || actionKeys != null)) {
                    // Show context menu if there are registered action handlers
                    int left = WidgetUtil.getTouchOrMouseClientX(event)
                            + Window.getScrollLeft();
                    int top = WidgetUtil.getTouchOrMouseClientY(event)
                            + Window.getScrollTop();

                    selectRowForContextMenuActions(event);

                    showContextMenu(left, top);
                }
            }

            @Override
            public void showContextMenu(int left, int top) {
                if (_delegate.customContextMenu != null) {
                    _delegate.showContextMenuPopup(left, top);
                } else {
                    super.showContextMenu(left, top);
                }
            }

            protected void selectRowForContextMenuActions(Event event) {
                boolean clickEventSent = handleClickEvent(event, getElement(), false);
                if (JmixScrollTableWidget.this.isSelectable()) {
                    boolean currentlyJustThisRowSelected = selectedRowKeys
                            .size() == 1
                            && selectedRowKeys.contains(getKey());

                    boolean selectionChanged = false;
                    if (!isSelected()) {
                        if (!currentlyJustThisRowSelected) {
                            if (isSingleSelectMode()
                                    || isMultiSelectModeDefault()) {
                                deselectAll();
                            }
                            toggleSelection();
                        } else if ((isSingleSelectMode() || isMultiSelectModeSimple())
                                && nullSelectionAllowed) {
                            toggleSelection();
                        }

                        selectionChanged = true;
                    }

                    if (selectionChanged) {
                        selectionRangeStart = this;
                        setRowFocus(this);

                        // Queue value change
                        sendSelectedRows(true);
                    }
                }
                if (immediate || clickEventSent) {
                    client.sendPendingVariableChanges();
                }
            }

            @Override
            public void toggleSelection() {
                if (selectable) {
                    super.toggleSelection();
                }
            }

            public boolean isSelectable() {
                return selectable;
            }

            @Override
            protected boolean hasContextMenuActions() {
                if (_delegate.contextMenuEnabled && _delegate.customContextMenu != null) {
                    return true;
                }

                return super.hasContextMenuActions();
            }
        }

        public LinkedList<Widget> getRenderedRows() {
            return renderedRows;
        }
    }

    public void requestFocus(final String itemKey, final String columnKey) {
        _delegate.requestFocus(itemKey, columnKey);
    }

    public void showCustomPopup() {
        _delegate.showCustomPopup();
    }

    @Override
    protected boolean isColumnCollapsingEnabled() {
        return (columnOrder.length - 1) > collapsedColumns.size();
    }

    @Override
    public void updateColumnProperties(UIDL uidl) {
        super.updateColumnProperties(uidl);

        if (uidl.hasAttribute("coljmixids")
                && uidl.hasAttribute("vcolorder")) {
            try {
                String[] vcolorder = uidl.getStringArrayAttribute("vcolorder");
                String[] coljmixids = uidl.getStringArrayAttribute("coljmixids");

                Map<String, HeaderCell> headerCellMap = new HashMap<>();
                for (int i = 0; i < getHead().getVisibleCellCount(); i++) {
                    HeaderCell headerCell = getHead().getHeaderCell(i);
                    if (headerCell.getColKey() != null) {
                        headerCellMap.put(headerCell.getColKey(), headerCell);
                    }
                }

                for (int i = 0; i < vcolorder.length; i++) {
                    String key = vcolorder[i];
                    HeaderCell headerCell = headerCellMap.get(key);

                    if (headerCell != null) {
                        headerCell.getElement().setAttribute("j-test-id", "column_" + coljmixids[i]);
                    }
                }
            } catch (Exception e) {
                Logger.getLogger("JmixScrollTableWidget").log(Level.SEVERE,
                        "Unable to init j-test-ids for columns " + e.getMessage());
            }
        }
    }

    @Override
    public void updateBody(UIDL uidl, int firstRow, int reqRows) {
        super.updateBody(uidl, firstRow, reqRows);

        handleUpdateBodyRows();
    }

    @Override
    public void updateRowsInBody(UIDL partialRowUpdates) {
        super.updateRowsInBody(partialRowUpdates);

        handleUpdateBodyRows();
    }

    @Override
    public void initializeRows(UIDL uidl, UIDL rowData) {
        super.initializeRows(uidl, rowData);

        handleUpdateBodyRows();
    }

    protected void handleUpdateBodyRows() {
        _delegate.handleUpdateBody();
    }

    @Override
    public void addAfterBodyUpdateListener(AfterBodyUpdateListener listener) {
        _delegate.addAfterUpdateBodyListener(listener);
    }

    @Override
    public void removeAfterBodyUpdateListener(AfterBodyUpdateListener listener) {
        _delegate.removeAfterUpdateListener(listener);
    }

    @Override
    protected void reOrderColumn(String columnKey, int newIndex) {
        if (_delegate.isAggregationVisible()) {
            client.updateVariable(paintableId, "updateAggregationRow", true, false);
        }

        super.reOrderColumn(columnKey, newIndex);

        if (!client.hasEventListeners(this, TableConstants.COLUMN_REORDER_EVENT_ID)
                && _delegate.isAggregationVisible()) {
            client.sendPendingVariableChanges();
        }
    }

    // CAUTION: copied from com.vaadin.v7.client.ui.VScrollTable.hasVerticalScrollbar
    @Override
    public boolean hasVerticalScrollbar() {
        return scrollBody.getOffsetHeight() > scrollBodyPanel.getOffsetHeight();
    }
}
