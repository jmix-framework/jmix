/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package io.jmix.ui.app.core.dev;

import io.jmix.ui.component.Button;
import io.jmix.ui.component.TextArea;
import io.jmix.ui.screen.DialogMode;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@DialogMode(
        resizable = true,
        width = "600",
        height = "400")
@UiController("ui_LayoutAnalyzerScreen")
@UiDescriptor("layout-analyzer.xml")
public class LayoutAnalyzerScreen extends Screen {

    private static final Logger log = LoggerFactory.getLogger(LayoutAnalyzerScreen.class);

    @Autowired
    protected TextArea<String> analyzeResultBox;

    public void setLayoutTips(List<LayoutTip> layoutTips) {
        if (CollectionUtils.isNotEmpty(layoutTips)) {
            StringBuilder analysisText = new StringBuilder();
            for (LayoutTip tip : layoutTips) {
                analysisText.append("[")
                        .append(tip.errorType.name()).append("] ")
                        .append(tip.componentPath).append("\n")
                        .append(tip.message).append("\n\n");
            }
            String analysisLog = analysisText.toString().trim();

            log.info("Analyze layout\n{}", analysisLog);

            analyzeResultBox.setValue(analysisLog);
        }
    }

    @Subscribe("closeBtn")
    protected void onCloseButtonClick(Button.ClickEvent event) {
        close(WINDOW_CLOSE_ACTION);
    }
}
