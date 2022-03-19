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

package spec.haulmont.cuba.web.facets.optiondialog.screens;

import com.haulmont.cuba.gui.components.OptionDialogFacet;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController
@UiDescriptor("option-dialog-facet-test-screen.xml")
public class OptionDialogFacetTestScreen extends Screen {

    @Autowired
    public Action dialogAction;
    @Autowired
    public Button dialogButton;
    @Autowired
    public OptionDialogFacet optionDialog;

    @Install(subject = "actionHandler", to = "optionDialog.ok")
    protected void onOkActionPerform() {
    }
}
