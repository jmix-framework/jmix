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

package io.jmix.dashboardsui.screen.dashboard.editor.weight;

import io.jmix.core.Messages;
import io.jmix.dashboards.model.visualmodel.DashboardLayout;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.component.Slider;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.jmix.ui.component.Window.CLOSE_ACTION_ID;
import static io.jmix.ui.component.Window.COMMIT_ACTION_ID;

@UiController("dshbrd_Weight.dialog")
@UiDescriptor("weight-dialog.xml")
public class WeightDialog extends Screen {
    public static final String WIDGET = "WIDGET";

    @Autowired
    protected HBoxLayout sliderBox;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Slider<Integer> weightSlider;

    private DashboardLayout layout;

    @Subscribe
    public void onInit(InitEvent e) {
        MapScreenOptions options = (MapScreenOptions) e.getOptions();
        Map<String, Object> params = options.getParams();

        layout = (DashboardLayout) params.get(WIDGET);

        initWeightSlider();
    }

    private void initWeightSlider() {
        int initWeight = layout.getWeight();

        weightSlider.setValue(initWeight);
        weightSlider.setCaption(getCaption(initWeight));
        weightSlider.setWidthFull();
        weightSlider.setCaptionAsHtml(true);

        weightSlider.addValueChangeListener(event -> weightSlider.setCaption(getCaption(weightSlider.getValue())));
    }

    protected String getCaption(int weight) {
        return messages.formatMessage(WeightDialog.class, "dashboard.weight", weight);
    }

    public Integer getWeight() {
        return weightSlider.getValue();
    }

    @Subscribe("okBtn")
    public void apply(Button.ClickEvent event) {
        layout.setWeight(weightSlider.getValue());
        this.close(new StandardCloseAction(COMMIT_ACTION_ID));
    }

    @Subscribe("cancelBtn")
    public void cancel(Button.ClickEvent event) {
        this.close(new StandardCloseAction(CLOSE_ACTION_ID));
    }
}