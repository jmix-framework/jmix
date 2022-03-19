/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dashboardsui.screen.dashboard.editor.colspan;

import io.jmix.core.Messages;
import io.jmix.dashboards.model.visualmodel.GridArea;
import io.jmix.dashboards.model.visualmodel.GridCellLayout;
import io.jmix.dashboards.model.visualmodel.GridLayout;
import io.jmix.dashboards.utils.DashboardLayoutUtils;
import io.jmix.dashboards.utils.DashboardLayoutManager;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.Slider;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.jmix.ui.component.Window.CLOSE_ACTION_ID;
import static io.jmix.ui.component.Window.COMMIT_ACTION_ID;

@UiController("dshbrd_Colspan.dialog")
@UiDescriptor("colspan-dialog.xml")
public class ColspanDialog extends Screen {

    public static final String WIDGET = "WIDGET";

    @Autowired
    protected HBoxLayout sliderBox;

    @Autowired
    private Label<String> leftLabel;

    @Autowired
    private Label<String> rightLabel;

    @Autowired
    protected Messages messages;

    @Autowired
    private Slider<Integer> colSpanSlider;
    @Autowired
    private Slider<Integer> rowSpanSlider;
    @Autowired
    private DashboardLayoutManager layoutManager;

    private GridCellLayout layout;
    private GridLayout parent;

    @Subscribe
    public void onInit(InitEvent e) {
        ScreenOptions options = e.getOptions();
        Map<String, Object> params = ((MapScreenOptions) options).getParams();
        layout = (GridCellLayout) params.get(WIDGET);
        parent = (GridLayout) layout.getParent();

        int cols = layout.getColSpan() + 1;
        int maxColSpan = DashboardLayoutUtils.availableColumns(parent, layout);
        initColumnSlider(cols, maxColSpan);

        int rows = layout.getRowSpan() + 1;
        int maxRowSpan = DashboardLayoutUtils.availableRows(parent, layout);
        initRowSlider(rows, maxRowSpan);
    }

    protected void initColumnSlider(int cols, int maxColSpan) {
        colSpanSlider.setMax(maxColSpan);

        colSpanSlider.addValueChangeListener(event ->
                leftLabel.setValue(getCaption("dashboard.columnSpan", colSpanSlider.getValue(), maxColSpan))
        );
        leftLabel.setValue(getCaption("dashboard.columnSpan", cols, maxColSpan));
        colSpanSlider.setValue(cols);
    }

    protected void initRowSlider(int rows, int maxRowSpan) {
        rowSpanSlider.setMax(maxRowSpan);
        rowSpanSlider.addValueChangeListener(event ->
                rightLabel.setValue(getCaption("dashboard.rowSpan", rowSpanSlider.getValue(), maxRowSpan))
        );
        rightLabel.setValue(getCaption("dashboard.rowSpan", rows, maxRowSpan));
        rowSpanSlider.setValue(rows);
    }

    protected String getCaption(String message, Integer value, int maxValue) {
        return messages.formatMessage(ColspanDialog.class, message, value, maxValue);
    }

    @Subscribe("okBtn")
    public void apply(Button.ClickEvent event) {
        layout.setColSpan(colSpanSlider.getValue() - 1);
        layout.setRowSpan(rowSpanSlider.getValue() - 1);
        GridArea gridArea = parent.getGridArea(layout.getColumn(), layout.getRow());
        gridArea.setCol2(gridArea.getCol() + layout.getColSpan());
        gridArea.setRow2(gridArea.getRow() + layout.getRowSpan());
        reorderGridAreas();
        this.close(new StandardCloseAction(COMMIT_ACTION_ID));
    }

    private void reorderGridAreas() {
        Set<GridArea> gridAreas = new HashSet<>();
        for (int col = 0; col < parent.getColumns(); col++) {
            for (int row = 0; row < parent.getRows(); row++) {
                GridArea gridArea = parent.getGridArea(col, row);
                if (gridArea == null) {
                    gridArea = layoutManager.createGridArea(col, row, parent);
                    gridAreas.add(gridArea);
                } else {
                    gridAreas.add(gridArea);
                }
            }
        }
        parent.setAreas(gridAreas);
    }

    @Subscribe("cancelBtn")
    public void cancel(Button.ClickEvent event) {
        close(new StandardCloseAction(CLOSE_ACTION_ID));
    }
}