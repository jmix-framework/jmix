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

package io.jmix.samples.ui.screen.component;

import io.jmix.core.MetadataTools;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.*;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

@UiDescriptor("component-samples.xml")
@UiController("component-samples")
@Route("components")
public class ComponentSamples extends Screen {

    @Autowired
    private CheckBoxGroup<String> checkBoxGroup;

    @Autowired
    private TwinColumn<String> twinColumn;

    @Autowired
    private MultiSelectList<String> multiSelectList;

    @Autowired
    private SingleSelectList<String> singleSelectList;

    @Autowired
    private RadioButtonGroup<String> radioButtonGroup;

    @Autowired
    private VBoxLayout othersVBox;

    @Autowired
    private UiComponents uiComponents;

    @Autowired
    private Dialogs dialogs;
    @Autowired
    private Notifications notifications;
    @Autowired
    private MetadataTools metadataTools;

//    private ListEditor<String> listEditor;

    @Subscribe
    private void onInit(InitEvent initEvent) {
        List<String> options = Arrays.asList("Value 1", "Value 2", "Value 3", "Value 4");
        checkBoxGroup.setOptionsList(options);
        twinColumn.setOptionsList(options);
        multiSelectList.setOptionsList(options);
        singleSelectList.setOptionsList(options);
        radioButtonGroup.setOptionsList(options);

//        listEditor = uiComponents.create(ListEditor.NAME);
//        listEditor.setOptionsList(options);
//        listEditor.setCaption("ListEditor");
//        othersVBox.add(listEditor);
        multiSelectList.addDoubleClickListener(event -> notifications.create().withCaption("DoubleClick: " + event.getItem()).show());
        singleSelectList.addDoubleClickListener(event -> notifications.create().withCaption("DoubleClick: " + event.getItem()).show());
    }

    @Subscribe("inputDialogBtn")
    private void onInputDialogBtnClick(Button.ClickEvent event) {
        dialogs.createInputDialog(this)
                .withCaption("Input Dialog")
                .withParameters(
                        InputParameter.booleanParameter("boolParam")
                                .withCaption("Boolean parameter")
                                .withDefaultValue(true)
                                .withRequired(true),
                        InputParameter.intParameter("intParam")
                                .withCaption("Integer parameter")
                                .withRequired(true)
                )
                .withActions(DialogActions.OK_CANCEL)
                .show();
    }

    @Subscribe("messageDialogBtn")
    private void onMessageDialogBtnClick(Button.ClickEvent event) {
        dialogs.createMessageDialog()
                .withCaption("Message Dialog")
                .withMessage("Message")
                .withModal(true)
                .withCloseOnClickOutside(true)
                .show();
    }

    @Subscribe("optionDialogBtn")
    private void onOptionDialogBtnClick(Button.ClickEvent event) {
        dialogs.createOptionDialog()
                .withCaption("Option Dialog")
                .withMessage("Message")
                .withActions(new DialogAction(DialogAction.Type.OK),
                        new DialogAction(DialogAction.Type.CANCEL))
                .show();
    }

    @Subscribe("notificationBtn")
    protected void onNotificationBtnClick(Button.ClickEvent event) {
        notifications.create(Notifications.NotificationType.TRAY)
                .withCaption("Notification")
                .show();
    }
}
