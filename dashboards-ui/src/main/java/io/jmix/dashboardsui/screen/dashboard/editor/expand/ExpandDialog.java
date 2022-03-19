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

package io.jmix.dashboardsui.screen.dashboard.editor.expand;

import io.jmix.dashboards.model.visualmodel.DashboardLayout;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static io.jmix.ui.component.Window.CLOSE_ACTION_ID;
import static io.jmix.ui.component.Window.COMMIT_ACTION_ID;

@UiController("dshbrd_Expand.dialog")
@UiDescriptor("expand-dialog.xml")
public class ExpandDialog extends Screen {
    public static final String WIDGET = "WIDGET";

    @Autowired
    private EntityPicker<DashboardLayout> expandEntityPicker;

    @Autowired
    private CollectionContainer<DashboardLayout> layoutsDc;

    @Subscribe
    public void onInit(InitEvent event) {
        ScreenOptions options = event.getOptions();

        Map<String, Object> params = Collections.emptyMap();
        if (options instanceof MapScreenOptions) {
            params = ((MapScreenOptions) options).getParams();
        }

        DashboardLayout layout = (DashboardLayout) params.get(WIDGET);

        if (layout == null) {
            close(new StandardCloseAction(CLOSE_ACTION_ID));
            return;
        }

        UUID expandedLayout = layout.getExpand();

        layoutsDc.getMutableItems().addAll(layout.getChildren());

        if (expandedLayout != null) {
            DashboardLayout selectedLayout = layoutsDc.getItem(expandedLayout);
            expandEntityPicker.setValue(selectedLayout);
        }
    }

    public DashboardLayout getExpand() {
        return expandEntityPicker.getValue();
    }

    @Subscribe("okBtn")
    public void apply(Button.ClickEvent event) {
        this.close(new StandardCloseAction(COMMIT_ACTION_ID));
    }

    @Subscribe("cancelBtn")
    public void cancel(Button.ClickEvent event) {
        this.close(new StandardCloseAction(CLOSE_ACTION_ID));
    }
}