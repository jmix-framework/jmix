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

package io.jmix.aitoolsflowui.view.chat.renderer.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import io.jmix.aitoolsflowui.icon.AiIconProvider;
import org.jspecify.annotations.Nullable;

/**
 * Badge element shown in the timeline avatar column for assistant messages.
 * The inner glyph is injected via {@link #setIcon(Component)} rather than
 * created here, so callers can supply a custom icon from an
 * {@link AiIconProvider}.
 */
public class AssistantAvatar extends Composite<Div> {

    protected static final String BASE_CN = "timeline-avatar";
    protected static final String ASSISTANT_CN = "timeline-avatar-assistant";
    protected static final String AVATAR_ICON_CN = "timeline-avatar-icon";

    @Override
    protected Div initContent() {
        Div div = new Div();
        div.addClassNames(BASE_CN, ASSISTANT_CN);
        return div;
    }

    public void setIcon(@Nullable Component icon) {
        getContent().removeAll();
        if (icon != null) {
            icon.getElement().getClassList().add(AVATAR_ICON_CN);
            getContent().add(icon);
        }
    }
}
