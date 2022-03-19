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
package io.jmix.dashboardsui.screen.widget;

import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.dashboards.model.parameter.Parameter;
import io.jmix.dashboards.model.Widget;
import io.jmix.dashboardsui.dashboard.tools.AccessConstraintsHelper;
import io.jmix.dashboardsui.repository.WidgetRepository;
import io.jmix.dashboardsui.repository.WidgetTypeInfo;
import io.jmix.dashboardsui.screen.parameter.ParametersFragment;
import io.jmix.ui.Fragments;
import io.jmix.ui.component.OrderedContainer;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UiController("dshbrd_Widget.edit")
@UiDescriptor("widget-edit.xml")
@EditedEntityContainer("widgetDc")
public class WidgetEdit extends StandardEditor<Widget> {
    public static final String SCREEN_NAME = "dshbrd_Widget.edit";
    public static final String ITEM_DC = "ITEM_DC";

    @Autowired
    protected InstanceContainer<Widget> widgetDc;
    @Autowired
    protected OrderedContainer widgetEditBox;
    @Autowired
    protected ParametersFragment paramsFragment;
    @Autowired
    protected WidgetRepository widgetRepository;
    @Autowired
    protected AccessConstraintsHelper accessHelper;
    @Autowired
    protected Fragments fragments;

    @Autowired
    @Qualifier("form.caption")
    protected TextField<String> widgetCaption;

    @Autowired
    @Qualifier("form.widgetId")
    protected TextField<String> widgetId;

    protected List<WidgetTypeInfo> typesInfo;
    protected ScreenFragment widgetEditFragment;

    @Autowired
    private Messages messages;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        typesInfo = widgetRepository.getWidgetTypesInfo();
        setWidgetType();
        initParametersFragment();
        widgetCaption.addValueChangeListener(v -> {
            if (StringUtils.isEmpty(widgetId.getValue())) {
                widgetId.setValue(v.getValue());
            }
        });
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<Widget> event) {
        Widget entity = event.getEntity();
        if (StringUtils.isEmpty(entity.getCreatedBy())) {
            entity.setCreatedBy(accessHelper.getCurrentUsername());
        }
    }

    protected void setWidgetType() {
        String browseFragmentId = widgetDc.getItem().getFragmentId();

        WidgetTypeInfo widgetType = typesInfo.stream()
                .filter(typeInfo -> browseFragmentId.equals(typeInfo.getFragmentId()))
                .findFirst().orElseThrow(() -> new RuntimeException("Unknown widget type"));


        Map<String, Object> params = new HashMap<>(ParamsMap.of(ITEM_DC, widgetDc));
        params.putAll(widgetRepository.getWidgetParams(widgetDc.getItem()));
        if (StringUtils.isNotEmpty(widgetType.getEditFragmentId())) {
            widgetEditFragment = fragments.create(this,
                    widgetType.getEditFragmentId(),
                    new MapScreenOptions(params))
                    .init();
            widgetEditBox.removeAll();
            widgetEditBox.add(widgetEditFragment.getFragment());
        }
    }

    protected void initParametersFragment() {
        paramsFragment.init(ParamsMap.of(
                ParametersFragment.PARAMETERS,
                getEditedEntity().getParameters()
        ));
    }


    @Subscribe(target = Target.DATA_CONTEXT)
    protected void preCommit(DataContext.PreCommitEvent event) {
        List<Parameter> parameters = paramsFragment.getParameters();
        getEditedEntity().setParameters(parameters);
        if (widgetEditFragment != null) {
            widgetRepository.serializeWidgetFields(widgetEditFragment, widgetDc.getItem());
        }
    }

    @Override
    protected void validateAdditionalRules(ValidationErrors errors) {
        super.validateAdditionalRules(errors);

        if (errors.isEmpty()) {
            Widget widget = widgetDc.getItem();
            if (widget.getDashboard() != null) {
                List<Widget> dashboardWidgets = widget.getDashboard().getWidgets();
                long cnt = dashboardWidgets.stream()
                        .filter(w -> !w.getId().equals(widget.getId()) && w.getWidgetId().equals(widget.getWidgetId()))
                        .count();
                if (cnt > 0) {
                    errors.add(widgetId, messages.getMessage(WidgetEdit.class, "uniqueWidgetId"));
                }
            }
        }
    }
}