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

package io.jmix.ui.widget.client.fieldgrouplayout;

import io.jmix.ui.widget.client.gridlayout.JmixGridLayoutWidget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ui.VGridLayout;
import com.vaadin.client.ui.layout.ComponentConnectorLayoutSlot;

public class JmixFieldGroupLayoutWidget extends JmixGridLayoutWidget {

    public static final String CLASSNAME = "jmix-fglayout";

    protected boolean useInlineCaption = true;

    public JmixFieldGroupLayoutWidget() {
        setStyleName(CLASSNAME);
    }

    public class JmixFieldGroupLayoutCell extends JmixGridLayoutCell {
        public JmixFieldGroupLayoutCell(int row, int col) {
            super(row, col);
        }

        @Override
        protected ComponentConnectorLayoutSlot createComponentConnectorLayoutSlot(ComponentConnector component) {
            JmixFieldGroupLayoutComponentSlot slot =
                    new JmixFieldGroupLayoutComponentSlot(JmixFieldGroupLayoutWidget.CLASSNAME, component, getConnector());
            slot.setCaptionInline(useInlineCaption);
            return slot;
        }
    }

    @Override
    public VGridLayout.Cell createNewCell(int row, int col) {
        // CAUTION copied from VGridLayout.createNewCell(int row, int col)
        VGridLayout.Cell cell = new JmixFieldGroupLayoutCell(row, col);
        cells[col][row] = cell;
        return cell;
    }
}
