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

import com.google.gwt.dom.client.Element;
import io.jmix.ui.widget.JmixGroupTable;
import io.jmix.ui.widget.client.table.JmixScrollTableConnector;
import io.jmix.ui.widget.client.table.JmixTableServerRpc;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.TooltipInfo;
import com.vaadin.client.UIDL;
import com.vaadin.client.WidgetUtil;
import com.vaadin.shared.ui.Connect;
import com.vaadin.v7.client.ui.VScrollTable;

@Connect(JmixGroupTable.class)
public class JmixGroupTableConnector extends JmixScrollTableConnector {

    @Override
    public JmixGroupTableWidget getWidget() {
        return (JmixGroupTableWidget) super.getWidget();
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if (uidl.hasVariable("groupColumns")) {
            getWidget().updateGroupColumns(uidl.getStringArrayVariableAsSet("groupColumns"));
        } else {
            getWidget().updateGroupColumns(null);
        }

        VScrollTable.VScrollTableBody.VScrollTableRow row = getWidget().focusedRow;

        super.updateFromUIDL(uidl, client);

        if (row instanceof JmixGroupTableWidget.JmixGroupTableBody.JmixGroupTableGroupRow) {
            getWidget().setRowFocus(
                    getWidget().getRenderedGroupRowByKey(
                            ((JmixGroupTableWidget.JmixGroupTableBody.JmixGroupTableGroupRow) row).getGroupKey()
                    )
            );
        }
    }

    @Override
    public TooltipInfo getTooltipInfo(Element element) {
        if (element != getWidget().getElement()) {
            Object node = WidgetUtil.findWidget(
                    element,
                    JmixGroupTableWidget.JmixGroupTableBody.JmixGroupTableRow.class);

            if (node != null) {
                JmixGroupTableWidget.JmixGroupTableBody.JmixGroupTableRow row
                        = (JmixGroupTableWidget.JmixGroupTableBody.JmixGroupTableRow) node;
                return row.getTooltip(element);
            }

            node = WidgetUtil.findWidget(
                    element,
                    JmixGroupTableWidget.JmixGroupTableBody.JmixGroupTableGroupRow.class);

            if (node != null) {
                JmixGroupTableWidget.JmixGroupTableBody.JmixGroupTableGroupRow row
                        = (JmixGroupTableWidget.JmixGroupTableBody.JmixGroupTableGroupRow) node;
                return row.getTooltip(element);
            }
        }

        return super.getTooltipInfo(element);
    }

    @Override
    protected void init() {
        super.init();

        getWidget()._delegate.groupAggregationInputHandler = (columnKey, groupKey, value, isFocused) -> {
            getRpcProxy(JmixTableServerRpc.class).onAggregationGroupInputChange(columnKey, groupKey, value, isFocused);
        };
    }

    @Override
    protected void updateAdditionalRowData(UIDL uidl) {
        super.updateAdditionalRowData(uidl);

        UIDL groupRow = uidl.getChildByTagName("groupRows");
        if (groupRow != null) {
            getWidget().updateGroupRowsWithAggregation(groupRow);
        }
    }
}
