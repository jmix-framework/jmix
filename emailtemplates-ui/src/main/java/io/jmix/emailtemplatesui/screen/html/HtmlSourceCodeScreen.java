/*
 * Copyright 2020 Haulmont.
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

package io.jmix.emailtemplatesui.screen.html;


import io.jmix.emailtemplates.utils.HtmlTemplateUtils;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("emltmp_HtmlSourceCodeScreen")
@UiDescriptor("html-source-code-screen.xml")
public class HtmlSourceCodeScreen extends Screen {

    @WindowParam
    protected String html;

    @Autowired
    private SourceCodeEditor sourceCode;

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (StringUtils.isNotEmpty(html)) {
            sourceCode.setValue(HtmlTemplateUtils.prettyPrintHTML(html));
        }
    }

    public String getValue() {
        return sourceCode.getValue();
    }

    @Subscribe("windowCommit")
    private void onWindowCommitClick(Button.ClickEvent event) {
        close(WINDOW_COMMIT_AND_CLOSE_ACTION);
    }

    @Subscribe("windowClose")
    private void onWindowCloseClick(Button.ClickEvent event) {
        close(WINDOW_CLOSE_ACTION);
    }


}