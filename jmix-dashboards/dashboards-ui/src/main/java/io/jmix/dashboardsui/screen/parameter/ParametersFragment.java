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

package io.jmix.dashboardsui.screen.parameter;

import io.jmix.dashboards.model.DashboardModel;
import io.jmix.dashboards.model.parameter.Parameter;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.jmix.dashboardsui.screen.dashboard.editor.canvas.CanvasFragment.DASHBOARD_MODEL;
import static io.jmix.ui.model.CollectionChangeType.REFRESH;

@UiController("dshbrd_Parameters.fragment")
@UiDescriptor("parameters-fragment.xml")
public class ParametersFragment extends ScreenFragment {
    public static final String PARAMETERS = "PARAMETERS";
    public static final String SCREEN_NAME = "dshbrd_Parameters.fragment";

    @Autowired
    protected CollectionContainer<Parameter> parametersDc;
    @Autowired
    protected Table<Parameter> parametersTable;
    @WindowParam(name = DASHBOARD_MODEL)
    protected DashboardModel dashboardModel;

    @Subscribe
    public void onInit(InitEvent event) {
        ScreenOptions options = event.getOptions();
        Map<String, Object> params = Collections.emptyMap();
        if (options instanceof MapScreenOptions) {
            params = ((MapScreenOptions) options).getParams();
        }

        init(params);
    }

    public void init(Map<String, Object> params) {
        initDc(params);
    }

    public List<Parameter> getParameters() {
        return new ArrayList<>(parametersDc.getItems());
    }

    public CollectionContainer<Parameter> getParametersDc() {
        return parametersDc;
    }

    protected void initDc(Map<String, Object> params) {
        List<Parameter> parameters = (List<Parameter>) params.get(PARAMETERS);

        if (parameters == null) {
            parameters = new ArrayList<>();
        }

        parametersDc.getMutableItems().addAll(parameters);

        parametersDc.addCollectionChangeListener(event -> {
            if (REFRESH != event.getChangeType()) {
                if (dashboardModel != null) {//if edit dashboard params
                    dashboardModel.setParameters(new ArrayList<>(event.getSource().getItems()));
                }
            }
        });
    }

    @Install(to = "parametersTable.value", subject = "columnGenerator")
    public Component generateValueCell(Parameter parameter) {
        String valueText = parameter.getValue() == null ? StringUtils.EMPTY : parameter.getValue().toString();
        return new Table.PlainTextCell(valueText);
    }
}
