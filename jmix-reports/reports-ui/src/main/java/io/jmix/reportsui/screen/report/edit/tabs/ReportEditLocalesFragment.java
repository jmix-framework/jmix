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

package io.jmix.reportsui.screen.report.edit.tabs;

import io.jmix.core.Messages;
import io.jmix.ui.Dialogs;
import io.jmix.ui.component.ContentMode;
import io.jmix.ui.component.HasContextHelp;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("report_ReportEditLocales.fragment")
@UiDescriptor("report-edit-locales-fragment.xml")
public class ReportEditLocalesFragment extends ScreenFragment {

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected MessageBundle messageBundle;

    @Install(to = "localeTextField", subject = "contextHelpIconClickHandler")
    protected void localeTextFieldContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messageBundle.getMessage("localeText"))
                .withMessage(messageBundle.getMessage("report.localeTextHelp"))
                .withContentMode(ContentMode.HTML)
                .withModal(false)
                .withWidth("600px")
                .show();
    }
}
