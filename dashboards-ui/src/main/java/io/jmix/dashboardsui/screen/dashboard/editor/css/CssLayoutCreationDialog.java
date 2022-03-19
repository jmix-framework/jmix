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

package io.jmix.dashboardsui.screen.dashboard.editor.css;

import io.jmix.ui.component.Button;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.TextField;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.ui.component.Window.CLOSE_ACTION_ID;
import static io.jmix.ui.component.Window.COMMIT_ACTION_ID;

@UiController("dshbrd_CssLayoutCreation.dialog")
@UiDescriptor("css-creation-dialog.xml")
public class CssLayoutCreationDialog extends Screen {
    public static final String SCREEN_NAME = "dshbrd_CssLayoutCreation.dialog";

    @Autowired
    private CheckBox responsive;
    @Autowired
    private TextField<String> styleName;

    @Subscribe("okBtn")
    public void apply(Button.ClickEvent event) {
        close(new StandardCloseAction(COMMIT_ACTION_ID));
    }

    @Subscribe("cancelBtn")
    public void cancel(Button.ClickEvent event) {
        close(new StandardCloseAction(CLOSE_ACTION_ID));
    }

    public String getCssStyleName() {
        return styleName.getValue();
    }

    public Boolean getResponsive() {
        return responsive.getValue();
    }

}