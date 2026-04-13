/*
 * Copyright 2026 Haulmont.
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

package io.jmix.messagetemplatesflowui.view.messagetemplatepreview;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.messagetemplates.entity.TemplateType;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;

@ViewController("msgtmp_MessageTemplatePreviewView")
@ViewDescriptor("message-template-preview-view.xml")
@DialogMode(width = "40em", resizable = true)
public class MessageTemplatePreviewView extends StandardView {

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Downloader downloader;

    @ViewComponent
    protected VerticalLayout contentBox;
    @ViewComponent
    protected JmixButton openInNewTabButton;

    protected TemplateType type;
    protected String content;

    @Subscribe
    public void onInit(InitEvent event) {
        downloader.setShowNewWindow(true);
    }

    public void setPreviewContent(TemplateType type, String content) {
        this.type = type;
        this.content = content;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (TemplateType.HTML.equals(type)) {
            initHtmlLayout();
        } else {
            initTextLayout();
        }
    }

    protected void initHtmlLayout() {
        openInNewTabButton.setVisible(true);

        // <div> wrapping required to avoid 'HTML must contain exactly one top-level element' exception
        Html html = new Html("<div>%s</div>".formatted(content));
        contentBox.addAndExpand(html);
    }

    protected void initTextLayout() {
        JmixTextArea textArea = uiComponents.create(JmixTextArea.class);

        textArea.setReadOnly(true);
        textArea.setWidth("60em");
        textArea.setValue(content);
        contentBox.addAndExpand(textArea);
    }

    @Subscribe("openInNewTabButton")
    public void onOpenInNewTabButtonClick(ClickEvent<JmixButton> event) {
        downloader.download(content.getBytes(StandardCharsets.UTF_8), "preview.html", DownloadFormat.HTML);
    }
}
