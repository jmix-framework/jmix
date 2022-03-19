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

package io.jmix.charts.screen;

import io.jmix.ui.WindowParam;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import io.jmix.charts.component.CustomChart;
import io.jmix.charts.model.chart.impl.AbstractChart;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("ui_JsonChartFragment")
@UiDescriptor("json-chart-fragment.xml")
public class JsonChartFragment extends ScreenFragment {

    @WindowParam(required = true)
    protected String chartJson;

    @Autowired
    protected CustomChart reportJsonChart;

    @Subscribe
    public void onInit(InitEvent event) {
        reportJsonChart.setConfiguration(new BasicChart());
        reportJsonChart.setNativeJson(chartJson);
    }

    /**
     * Used for default initialization in
     * ChartImpl.JmixAmchartsSceneExt#setupDefaults(AbstractChart)
     */
    protected static class BasicChart extends AbstractChart<BasicChart> {
    }
}