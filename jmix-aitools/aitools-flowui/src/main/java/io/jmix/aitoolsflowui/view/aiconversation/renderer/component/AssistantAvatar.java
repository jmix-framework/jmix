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

package io.jmix.aitoolsflowui.view.aiconversation.renderer.component;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

public class AssistantAvatar extends Composite<Div> {

    protected static final String BASE_CN = "timeline-avatar";
    protected static final String ASSISTANT_CN = "timeline-avatar-assistant";
    protected static final String AVATAR_ICON_CN = "timeline-avatar-icon";

    protected static final String ASSISTANT_AVATAR_PATH =
            "io/jmix/aitoolsflowui/icon/ai-assistant-avatar-dot.svg";

    @Override
    protected Div initContent() {
        Div div = createContent();
        div.addClassNames(BASE_CN, ASSISTANT_CN);

        div.add(createIcon());

        return div;
    }

    protected Div createContent() {
        return new Div();
    }

    protected SvgIcon createIcon() {
        DownloadHandler handler = DownloadHandler.fromInputStream(e -> {
            var stream = getClass().getClassLoader().getResourceAsStream(ASSISTANT_AVATAR_PATH);
            return new DownloadResponse(stream, "ai-assistant-avatar-dot.svg", "image/svg+xml", -1);
        });

        SvgIcon svgIcon = new SvgIcon(handler);
        svgIcon.addClassName(AVATAR_ICON_CN);
        return svgIcon;
    }
}
