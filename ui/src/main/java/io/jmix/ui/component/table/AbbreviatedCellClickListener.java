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

package io.jmix.ui.component.table;

import com.google.common.base.Strings;
import com.vaadin.ui.VerticalLayout;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.App;
import io.jmix.ui.component.Table;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.widget.JmixEnhancedTable;
import io.jmix.ui.widget.JmixResizableTextAreaWrapper;
import io.jmix.ui.widget.JmixTextArea;
import io.jmix.ui.widget.client.resizabletextarea.ResizeDirection;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

import static io.jmix.ui.component.impl.AbstractTable.MAX_TEXT_LENGTH_GAP;

@SuppressWarnings("rawtypes")
public class AbbreviatedCellClickListener implements Consumer<Table.Column.ClickEvent> {

    protected MetadataTools metadataTools;

    public AbbreviatedCellClickListener(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Override
    public void accept(Table.Column.ClickEvent clickEvent) {
        if (!clickEvent.isText() || clickEvent.getSource().getMaxTextLength() == null) {
            return;
        }

        Table.Column<?> column = clickEvent.getSource();
        Table owner = column.getOwner();
        if (owner == null || owner.getFrame() == null) {
            return;
        }

        Object rowItem = clickEvent.getItem();
        MetaPropertyPath mpp = column.getMetaPropertyPathNN();
        Object itemValue = EntityValues.getValueEx(rowItem, mpp);
        String stringItemValue = metadataTools.format(itemValue, mpp.getMetaProperty());

        boolean isMultiLineCell = StringUtils.contains(stringItemValue, "\n");
        if (StringUtils.isEmpty(stringItemValue)
                || (stringItemValue.length() <= column.getMaxTextLength() + MAX_TEXT_LENGTH_GAP
                && !isMultiLineCell)) {
            return;
        }

        VerticalLayout layout = createRootLayout();
        JmixTextArea textArea = createTextArea(stringItemValue);
        layout.addComponent(createContent(textArea));

        owner.withUnwrapped(JmixEnhancedTable.class, enhancedTable -> {
            enhancedTable.showCustomPopup(layout);
            enhancedTable.setCustomPopupAutoClose(false);
        });
    }

    protected VerticalLayout createRootLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.setWidthUndefined();
        layout.setStyleName("jmix-table-view-textcut");
        return layout;
    }

    protected JmixTextArea createTextArea(String stringItemValue) {
        JmixTextArea textArea = new JmixTextArea();
        textArea.setValue(Strings.nullToEmpty(stringItemValue));
        textArea.setReadOnly(true);
        return textArea;
    }

    protected JmixResizableTextAreaWrapper createContent(JmixTextArea textArea) {
        JmixResizableTextAreaWrapper content = new JmixResizableTextAreaWrapper(textArea);
        content.setResizableDirection(ResizeDirection.BOTH);

        ThemeConstants theme = App.getInstance().getThemeConstants();
        content.setWidth(theme.get("jmix.ui.Table.abbreviatedPopupWidth"));
        content.setHeight(theme.get("jmix.ui.Table.abbreviatedPopupHeight"));

        return content;
    }
}
