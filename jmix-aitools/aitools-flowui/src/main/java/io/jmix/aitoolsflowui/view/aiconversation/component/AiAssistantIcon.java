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

package io.jmix.aitoolsflowui.view.aiconversation.component;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import org.jspecify.annotations.NullMarked;

/**
 * The AI assistant brand mark (a sparkle burst, without the avatar's centre
 * dot). Reused on the chat starter — the hero glyph and the per-conversation
 * card icon (recent list and history panel). The timeline avatar uses the
 * dot variant ({@code ai-assistant-avatar-dot.svg}) instead.
 * <p>
 * The glyph paints with {@code currentColor}, so size and colour are driven by
 * CSS on the element (e.g. {@code width}/{@code height}/{@code color}).
 */
@NullMarked
public class AiAssistantIcon extends Composite<SvgIcon> {

    protected static final String BASE_CN = "ai-assistant-mark";

    protected static final String MARK_PATH = "io/jmix/aitoolsflowui/icon/ai-assistant-mark.svg";

    @Override
    protected SvgIcon initContent() {
        return createContent();
    }

    protected SvgIcon createContent() {
        InputStreamDownloadHandler handler = DownloadHandler.fromInputStream(e -> {
            var stream = AiAssistantIcon.class.getClassLoader().getResourceAsStream(MARK_PATH);
            return new DownloadResponse(stream, "ai-assistant-mark.svg", "image/svg+xml", -1);
        });

        SvgIcon svgIcon = new SvgIcon();
        svgIcon.addClassNames(BASE_CN);
        svgIcon.setSrc(handler);
        return svgIcon;
    }
}
