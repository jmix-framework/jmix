/*
 * Copyright (c) 2020 Haulmont.
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

package facet.input_dialog.screen;

import io.jmix.ui.action.Action;
import io.jmix.ui.app.inputdialog.InputDialog;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.InputDialogFacet;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController
@UiDescriptor("input-dialog-facet-test-screen.xml")
public class InputDialogFacetTestScreen extends Screen {

    @Autowired
    public Button dialogButton;
    @Autowired
    public Action dialogAction;
    @Autowired
    public InputDialogFacet inputDialog;
    @Autowired
    public InputDialogFacet inputDialogCustomActions;
    @Autowired
    public InputDialogFacet inputDialogCustomFields;

    @Install(to = "inputDialog", subject = "closeListener")
    public void onInputDialogClose(InputDialog.InputDialogCloseEvent closeEvent) {
    }

    @Install(to = "inputDialog", subject = "dialogResultHandler")
    public void handleDialogResults(InputDialog.InputDialogResult inputDialogResult) {
    }

    @Install(to = "inputDialog", subject = "validator")
    public ValidationErrors validateDialogResults(InputDialog.ValidationContext context) {
        return new ValidationErrors();
    }
}
