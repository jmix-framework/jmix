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

package io.jmix.dashboardsui.screen.dashboard.editor.grid;

import io.jmix.core.Messages;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Slider;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.ui.component.Window.CLOSE_ACTION_ID;
import static io.jmix.ui.component.Window.COMMIT_ACTION_ID;

@UiController("dshbrd_GridCreation.dialog")
@UiDescriptor("grid-creation-dialog.xml")
public class GridCreationDialog extends Screen {
    public static final String SCREEN_NAME = "dshbrd_GridCreation.dialog";
    @Autowired
    protected Messages messages;

    @Autowired
    protected Slider<Integer> rows;

    @Autowired
    protected Slider<Integer> cols;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent e) {
        initRowSlider();
        initColumnSlider();
    }

    protected void initRowSlider() {
        rows.setValue(2);
        rows.setCaption(getCaption("dashboard.gridRowCount", 2));
        rows.setSizeFull();
        rows.setCaptionAsHtml(true);
        rows.addValueChangeListener(event -> rows.setCaption(getCaption("dashboard.gridRowCount", rows.getValue())));
    }

    protected void initColumnSlider() {
        cols.setValue(2);
        cols.setCaption(getCaption("dashboard.gridColumnCount", 2));
        cols.setSizeFull();
        cols.setCaptionAsHtml(true);
        cols.addValueChangeListener(event -> cols.setCaption(getCaption("dashboard.gridColumnCount", cols.getValue())));
    }

    protected String getCaption(String message, int value) {
        return messages.formatMessage(GridCreationDialog.class, message, value);
    }

    @Subscribe("okBtn")
    public void apply(Button.ClickEvent event) {
        this.close(new StandardCloseAction(COMMIT_ACTION_ID));
    }

    @Subscribe("cancelBtn")
    public void cancel(Button.ClickEvent event) {
        this.close(new StandardCloseAction(CLOSE_ACTION_ID));
    }

    public int getRows() {
        return rows.getValue();
    }

    public int getCols() {
        return cols.getValue();
    }

}