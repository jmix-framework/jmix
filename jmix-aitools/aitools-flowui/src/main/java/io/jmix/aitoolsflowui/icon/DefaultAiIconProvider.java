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

package io.jmix.aitoolsflowui.icon;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;

/**
 * Default implementation of {@link AiIconProvider} that uses the built-in
 * sparkle-burst SVG mark and the dot-variant avatar shipped with the add-on.
 */
public class DefaultAiIconProvider implements AiIconProvider {

    protected static final String MARK_PATH = "io/jmix/aitoolsflowui/icon/ai-assistant-mark.svg";
    protected static final String AVATAR_ICON_PATH = "io/jmix/aitoolsflowui/icon/ai-assistant-avatar-dot.svg";

    @Override
    public Component createMarkIcon() {
        InputStreamDownloadHandler handler = DownloadHandler.fromInputStream(e -> {
            var stream = getClass().getClassLoader().getResourceAsStream(MARK_PATH);
            return new DownloadResponse(stream, "ai-assistant-mark.svg", "image/svg+xml", -1);
        });
        return new SvgIcon(handler);
    }

    @Override
    public Component createAvatarIcon() {
        DownloadHandler handler = DownloadHandler.fromInputStream(e -> {
            var stream = getClass().getClassLoader().getResourceAsStream(AVATAR_ICON_PATH);
            return new DownloadResponse(stream, "ai-assistant-avatar-dot.svg", "image/svg+xml", -1);
        });
        return new SvgIcon(handler);
    }
}
