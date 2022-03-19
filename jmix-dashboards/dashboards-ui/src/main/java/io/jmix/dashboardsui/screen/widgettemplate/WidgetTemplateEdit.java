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
package io.jmix.dashboardsui.screen.widgettemplate;

import io.jmix.core.EntityStates;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.dashboards.entity.WidgetTemplate;
import io.jmix.dashboards.model.Widget;
import io.jmix.dashboards.converter.JsonConverter;
import io.jmix.dashboardsui.dashboard.tools.AccessConstraintsHelper;
import io.jmix.dashboardsui.dashboard.tools.WidgetUtils;
import io.jmix.dashboardsui.repository.WidgetRepository;
import io.jmix.dashboardsui.repository.WidgetTypeInfo;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.*;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Optional;

import static io.jmix.ui.component.Window.COMMIT_ACTION_ID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@UiController("dshbrd_WidgetTemplate.edit")
@UiDescriptor("widget-template-edit.xml")
@EditedEntityContainer("widgetTemplateDc")
public class WidgetTemplateEdit extends StandardEditor<WidgetTemplate> {

    @Autowired
    protected Form form;

    @Autowired
    private CheckBox isAvailableForAllUsersChkBox;

    @Autowired
    protected WidgetRepository widgetRepository;

    @Autowired
    protected UiComponents components;

    @Autowired
    protected InstanceContainer<WidgetTemplate> widgetTemplateDc;

    @Autowired
    protected JsonConverter converter;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected WidgetUtils widgetUtils;

    @Autowired
    protected AccessConstraintsHelper accessHelper;

    @Autowired
    protected ScreenBuilders screenBuilders;

    protected ComboBox<String> widgetTypeComboBox;
    protected Button editWidgetButton;

    protected boolean openWidgetEditor = false;

    @Autowired
    private Messages messages;

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private DataContext dataContext;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        form.add(generateWidgetTypeField(), 0, 2);

        if (entityStates.isNew(getEditedEntity())) {
            getEditedEntity().setCreatedBy(accessHelper.getCurrentUsername());
            if (getEditedEntity().getWidgetModel() != null) {
                widgetTypeComboBox.setEditable(false);
            }
        } else {
            widgetTypeComboBox.setEditable(false);
        }

        if (StringUtils.isNotEmpty(getEditedEntity().getWidgetModel())) {
            Widget widget = converter.widgetFromJson(getEditedEntity().getWidgetModel());
            setWidgetTypeComboBoxValue(widget, widgetTypeComboBox);
        }

        if (!accessHelper.getCurrentUsername().equals(getEditedEntity().getCreatedBy())) {
            isAvailableForAllUsersChkBox.setVisible(false);
        }
        openWidgetEditor = true;
    }

    public Component generateWidgetTypeField() {
        HBoxLayout hBoxLayout = components.create(HBoxLayout.class);
        hBoxLayout.setSpacing(true);
        hBoxLayout.setWidth("250px");
        hBoxLayout.setCaption(messages.getMessage(WidgetTemplateEdit.class, "widgetType"));

        this.widgetTypeComboBox = createWidgetTypeComboBox();
        this.editWidgetButton = createEditWidgetButton();

        hBoxLayout.add(widgetTypeComboBox);
        hBoxLayout.add(editWidgetButton);
        return hBoxLayout;
    }

    private ComboBox<String> createWidgetTypeComboBox() {
        ComboBox<String> widgetTypeComboBox = components.create(ComboBox.TYPE_STRING);
        widgetTypeComboBox.setWidth("100%");
        widgetTypeComboBox.setOptionsMap(widgetUtils.getWidgetCaptions());
        widgetTypeComboBox.addValueChangeListener(e -> {
            String browserFragmentId = e.getValue();
            if (browserFragmentId != null) {
                Widget widget = dataContext.create(Widget.class);
                widget.setFragmentId(browserFragmentId);
                widget.setName(widgetUtils.getWidgetType(e.getValue()));
                widget.setCreatedBy(accessHelper.getCurrentUsername());
                openWidgetEditor(widget);
            }
        });
        return widgetTypeComboBox;
    }

    private Button createEditWidgetButton() {
        Button editWidgetButton = components.create(Button.class);
        editWidgetButton.setWidth("100%");
        editWidgetButton.setCaption(messages.getMessage(WidgetTemplateEdit.class, "customize"));
        editWidgetButton.setIconFromSet(JmixIcon.GEAR);
        editWidgetButton.setAction(new BaseAction("openWidgetEditor") {
            @Override
            public void actionPerform(Component component) {
                WidgetTemplate widgetTemplate = widgetTemplateDc.getItem();
                if (StringUtils.isNotEmpty(widgetTemplate.getWidgetModel())) {
                    Widget widget = converter.widgetFromJson(widgetTemplateDc.getItem().getWidgetModel());
                    widget = dataContext.merge(widget);
                    openWidgetEditor(widget);
                }
            }
        });
        return editWidgetButton;
    }

    protected void openWidgetEditor(Widget widget) {
        if (openWidgetEditor) {
            screenBuilders.editor(Widget.class, this)
                    .withOpenMode(OpenMode.THIS_TAB)
                    .editEntity(widget)
                    .withParentDataContext(dataContext)
                    .build()
                    .show()
                    .addAfterCloseListener(e -> {
                        StandardCloseAction closeAction = (StandardCloseAction) e.getCloseAction();
                        if (COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                            WidgetTemplate widgetTemplate = widgetTemplateDc.getItem();
                            widgetTemplate.setWidgetModel(converter.widgetToJson(widget));
                        } else {
                            Widget prevWidget = converter.widgetFromJson(widgetTemplateDc.getItem().getWidgetModel());
                            openWidgetEditor = false;
                            setWidgetTypeComboBoxValue(prevWidget, widgetTypeComboBox);
                            openWidgetEditor = true;
                        }
                    });
        }
    }

    protected void setWidgetTypeComboBoxValue(@Nullable Widget widget, ComboBox<String> comboBox) {
        if (widget == null) {
            comboBox.setValue(null);
            return;
        }
        String browseFragmentId = widget.getFragmentId();

        Optional<WidgetTypeInfo> widgetTypeOpt = widgetRepository.getWidgetTypesInfo().stream()
                .filter(typeInfo -> browseFragmentId.equals(typeInfo.getFragmentId()))
                .findFirst();

        if (widgetTypeOpt.isPresent()) {
            String itemCaption = widgetTypeOpt.get().getFragmentId();

            if (isNotBlank(itemCaption)) {
                comboBox.setValue(itemCaption);
            }
        }
    }

    @Override
    protected void validateAdditionalRules(ValidationErrors errors) {
        WidgetTemplate widgetTemplate = widgetTemplateDc.getItem();
        if (StringUtils.isEmpty(widgetTemplate.getWidgetModel())) {
            errors.add(widgetTypeComboBox, messages.getMessage(WidgetTemplateEdit.class, "emptyWidgetError"));
        }
    }
}