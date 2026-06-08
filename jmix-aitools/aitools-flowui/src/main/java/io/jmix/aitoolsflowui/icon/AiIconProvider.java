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

/**
 * Strategy interface for providing AI assistant icon components.
 * <p>
 * Implement and register this bean to replace the default sparkle-burst / dot
 * glyphs with custom icons throughout the chat UI.
 * <p>
 * <b>Important:</b> each method MUST return a fresh {@link Component} instance
 * on every call. A Vaadin component can only be attached to the DOM once; the
 * icons are rendered in multiple places simultaneously (hero section, recent
 * conversation cards, history panel, timeline rows), so reusing the same
 * instance would silently detach it from the earlier location.
 */
public interface AiIconProvider {

    /**
     * Creates the brand mark icon used on the chat hub hero section and on
     * each conversation card (recent list and history panel).
     * <p>
     * Must return a fresh component on every call.
     *
     * @return a new component representing the AI brand mark
     */
    Component createMarkIcon();

    /**
     * Creates the assistant avatar glyph used in timeline message rows next
     * to each assistant (and thinking-indicator) message.
     * <p>
     * Must return a fresh component on every call.
     *
     * @return a new component representing the assistant avatar glyph
     */
    Component createAvatarIcon();
}
