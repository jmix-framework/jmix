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

package io.jmix.ui.widget.client.tabsheet;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.tabsheet.VDDTabSheet;
import com.vaadin.client.ComputedStyle;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.ui.VTabsheet;
import com.vaadin.client.ui.dd.VDragAndDropManager;
import com.vaadin.client.ui.dd.VDragEvent;
import com.vaadin.shared.ui.tabsheet.TabState;

public class JmixMainTabSheetWidget extends VDDTabSheet {

    protected TabContextMenuHandler tabContextMenuHandler;
    protected JmixTabBar tabBar;

    protected HandlerRegistration dragEndHandler;
    protected HandlerRegistration dropHandler;
    protected HandlerRegistration dragLeaveHandler;

    public JmixMainTabSheetWidget() {
        RootPanel rootPanel = RootPanel.get();

        dragEndHandler = rootPanel.addBitlessDomHandler(event ->
                        handleBadDD(event.getNativeEvent()),
                        DragEndEvent.getType());

        dropHandler = rootPanel.addBitlessDomHandler(event ->
                        handleBadDD(event.getNativeEvent()),
                        DropEvent.getType());

        dragLeaveHandler = rootPanel.addBitlessDomHandler(event -> {
            Element element = event.getRelativeElement();
            if (element == null || element == rootPanel.getElement()) {
                VDragAndDropManager.get().interruptDrag();
            }
        }, DragLeaveEvent.getType());
    }

    @Override
    protected boolean postDropHook(VDragEvent drag) {
        return drag != null;
    }

    protected void handleBadDD(NativeEvent event) {
        Element target = WidgetUtil.getElementUnderMouse(event);
        if (target == null) {
            VDragAndDropManager.get().interruptDrag();
            return;
        }

        Node targetParent = DOM.asOld(target).getParentNode();
        if (!getElement().isOrHasChild(targetParent)) {
            VDragAndDropManager.get().interruptDrag();
        }
    }

    @Override
    protected void updateDragDetails(VDragEvent event) {
        if (event == null) {
            return;
        }
        super.updateDragDetails(event);
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        dragEndHandler.removeHandler();
        dropHandler.removeHandler();
        dragLeaveHandler.removeHandler();
    }

    @Override
    protected void onTabContextMenu(final int tabIndex, ContextMenuEvent event) {
        if (tabContextMenuHandler != null) {
            tabContextMenuHandler.onContextMenu(tabIndex, event);
        }
    }

    public interface TabContextMenuHandler {
        void onContextMenu(final int tabIndex, ContextMenuEvent event);
    }

    @Override
    protected VTabsheet.TabBar createTabBar() {
        tabBar = new JmixTabBar(this);
        return tabBar;
    }

    @Override
    public void renderTab(TabState tabState, int index) {
        super.renderTab(tabState, index);

        VTabsheet.Tab tab = tabBar.getTab(index);
        if (tab.isHiddenOnServer()) {
            tab.removeStyleName("jmix-tab-visible");
            tab.addStyleName("jmix-tab-hidden");
        } else {
            tab.removeStyleName("jmix-tab-hidden");
            tab.addStyleName("jmix-tab-visible");
        }
    }

    public void assignAdditionalCellStyles() {
        assignAdditionalCellStyles(activeTabIndex);
    }

    public void assignAdditionalCellStyles(int navIndex) {
        if (navIndex >= 0) {
            int i = 0;
            boolean firstVisibleAfterSelection = false;
            for (Widget widget : tabBar) {
                VTabsheet.Tab t = (VTabsheet.Tab) widget;
                t.removeStyleName("jmix-tab-sibling-visible");

                if (!firstVisibleAfterSelection
                        && i > navIndex
                        && !t.isHiddenOnServer()) {
                    t.addStyleName("jmix-tab-sibling-visible");
                    firstVisibleAfterSelection = true;
                }

                i++;
            }
        }
    }

    public class JmixTabBar extends VTabsheet.TabBar {
        public JmixTabBar(VTabsheet tabsheet) {
            super(tabsheet);
        }

        @Override
        public VTabsheet.Tab navigateTab(int fromIndex, int toIndex) {
            VTabsheet.Tab navigateTab = super.navigateTab(fromIndex, toIndex);
            if (navigateTab != null) {
                assignAdditionalCellStyles(toIndex);
            }
            return navigateTab;
        }

        // CAUTION copied from VTabsheet.TabBar to override getPreviousVisibleTab() method
        @Override
        public int scrollLeft(int currentFirstVisible) {
            int prevVisible = getPreviousVisibleTab(currentFirstVisible);
            if (prevVisible == -1) {
                return -1;
            }

            Tab newFirst = getTab(prevVisible);
            newFirst.setVisible(true);
            newFirst.recalculateCaptionWidth();

            return prevVisible;
        }

        protected int getPreviousVisibleTab(int i) {
            do {
                i--;
            } while (i >= 0 && (getTab(i) == null || getTab(i).isHiddenOnServer()));

            return i;
        }
    }

    @Override
    public void updateContentNodeHeight() {
        if (!isDynamicHeight()) {
            ComputedStyle fullHeight = new ComputedStyle(getElement());
            double contentHeight = fullHeight.getHeight();

            ComputedStyle tabsCs = new ComputedStyle(tabs);
            contentHeight -= tabsCs.getHeight();

            contentHeight -= deco.getOffsetHeight();

            ComputedStyle cs = new ComputedStyle(contentNode);
            contentHeight -= cs.getPaddingHeight();
            contentHeight -= cs.getBorderHeight();

            if (contentHeight < 0) {
                contentHeight = 0;
            }

            // Set proper values for content element
            double ceilHeight = Math.ceil(contentHeight);

            contentNode.getStyle().setHeight(ceilHeight, Style.Unit.PX);
        } else {
            contentNode.getStyle().clearHeight();
        }
    }

    @Override
    public boolean loadTabSheet(int tabIndex) {
        Widget currentlyDisplayedWidget = getCurrentlyDisplayedWidget();

        boolean loaded = super.loadTabSheet(tabIndex);
        if (loaded) {
            /*
             * We have to set zero opacity in case of chart inside of tab
             * because "visibility" property doesn't work for SVG elements
             */
            currentlyDisplayedWidget.getElement().getStyle().setOpacity(0);
        }
        return loaded;
    }

    @Override
    protected JmixTabSelectionHandler createTabSelectionHandler() {
        return new JmixTabSelectionHandler();
    }

    public class JmixTabSelectionHandler extends VTabsheet.TabSelectionHandler {
        @Override
        public void onFocus(FocusEvent event) {
            super.onFocus(event);
            if (event.getSource() instanceof VTabsheet.Tab) {
                int focusedTabIndex = tabBar.getWidgetIndex((VTabsheet.Tab) event.getSource());
                setFocusedTabIndex(focusedTabIndex);
            }
        }
    }
}
