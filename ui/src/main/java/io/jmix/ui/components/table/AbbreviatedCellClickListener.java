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

package io.jmix.ui.components.table;

import com.google.common.base.Strings;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.dynamicattributes.DynamicAttributesTools;
import io.jmix.ui.dynamicattributes.DynamicAttributesUtils;
import com.vaadin.ui.VerticalLayout;
import io.jmix.core.entity.Entity;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.App;
import io.jmix.ui.components.Table;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.widgets.CubaEnhancedTable;
import io.jmix.ui.widgets.CubaResizableTextAreaWrapper;
import io.jmix.ui.widgets.CubaTextArea;
import io.jmix.ui.widgets.client.resizabletextarea.ResizeDirection;
import org.apache.commons.lang3.StringUtils;

import static io.jmix.ui.components.impl.WebAbstractTable.MAX_TEXT_LENGTH_GAP;

public class AbbreviatedCellClickListener implements Table.CellClickListener {

    protected Table table;
    protected DynamicAttributesTools dynamicAttributesTools;

    public AbbreviatedCellClickListener(Table table, DynamicAttributesTools dynamicAttributesTools) {
        this.table = table;
        this.dynamicAttributesTools = dynamicAttributesTools;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onClick(Entity item, String columnId) {
        Table.Column column = table.getColumn(columnId);
        MetaProperty metaProperty;
        String value;
        if (DynamicAttributesUtils.isDynamicAttribute(columnId)) {
            // todo dynamic attributes
            MetaClass metaClass = null/* = item.getMetaClass()*/;
            metaProperty = dynamicAttributesTools.getMetaPropertyPath(metaClass, columnId).getMetaProperty();
            value = dynamicAttributesTools.getDynamicAttributeValueAsString(metaProperty, item.getValueEx(columnId));
        } else {
            value = item.getValueEx(columnId);
        }
        if (column.getMaxTextLength() != null) {
            boolean isMultiLineCell = StringUtils.contains(value, "\n");
            if (value == null || (value.length() <= column.getMaxTextLength() + MAX_TEXT_LENGTH_GAP
                    && !isMultiLineCell)) {
                // todo artamonov if we click with CTRL and Table is multiselect then we lose previous selected items
                if (!table.getSelected().contains(item)) {
                    table.setSelected(item);
                }
                // do not show popup view
                return;
            }
        }

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.setWidthUndefined();
        layout.setStyleName("c-table-view-textcut");

        CubaTextArea textArea = new CubaTextArea();
        textArea.setValue(Strings.nullToEmpty(value));
        textArea.setReadOnly(true);

        CubaResizableTextAreaWrapper content = new CubaResizableTextAreaWrapper(textArea);
        content.setResizableDirection(ResizeDirection.BOTH);

        // todo implement injection for ThemeConstains in components
        ThemeConstants theme = App.getInstance().getThemeConstants();
        if (theme != null) {
            content.setWidth(theme.get("cuba.web.Table.abbreviatedPopupWidth"));
            content.setHeight(theme.get("cuba.web.Table.abbreviatedPopupHeight"));
        } else {
            content.setWidth("320px");
            content.setHeight("200px");
        }

        layout.addComponent(content);

        table.withUnwrapped(CubaEnhancedTable.class, enhancedTable -> {
            enhancedTable.showCustomPopup(layout);
            enhancedTable.setCustomPopupAutoClose(false);
        });
    }
}
