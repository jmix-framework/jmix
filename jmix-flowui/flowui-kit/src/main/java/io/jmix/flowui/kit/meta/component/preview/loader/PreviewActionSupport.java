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

package io.jmix.flowui.kit.meta.component.preview.loader;

import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.xml.layout.support.BaseComponentLoaderSupport;
import io.jmix.flowui.kit.xml.layout.support.BaseLoaderSupport;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

/**
 * Shared helpers for preview loaders that build {@code action}-backed menu items from XML:
 * {@code msg://} text resolution, {@link BaseAction} construction from a declarative
 * {@code <action>} element, and best-effort {@code ref} resolution against an
 * {@code <action id="...">} declared elsewhere in the view.
 * <p>
 * Extracted from {@link StudioDropdownButtonPreviewLoader}. Public only so it is visible to
 * {@code StudioStandardComponentsPreviewLoader}, which lives in the parent
 * {@code io.jmix.flowui.kit.meta.component.preview} package where package-private statics here
 * would not be reachable; internal to preview loaders, not part of the public preview loader API.
 */
public final class PreviewActionSupport {

    private static final String MESSAGE_REF_PREFIX = "msg://";
    private static final String ACTION_ELEMENT = "action";

    private PreviewActionSupport() {
    }

    /**
     * Resolves a {@code msg://} message reference through the environment, falling back to
     * the raw value when the reference isn't a message key or the environment can't resolve it
     * (e.g. {@link StudioPreviewEnvironment#NOOP}). Null-safe: a {@code null} value passes through.
     */
    @Nullable
    public static String resolveText(StudioPreviewEnvironment environment, @Nullable String value) {
        if (value == null || !value.startsWith(MESSAGE_REF_PREFIX)) {
            return value;
        }
        String resolved = environment.resolveMessage(value);
        return resolved != null ? resolved : value;
    }

    /**
     * Builds a {@link BaseAction} from a declarative {@code <action>} element: {@code id} attribute
     * (falling back to {@code fallbackId}), {@code text} (resolved via {@link #resolveText}),
     * {@code icon} (via {@link BaseComponentLoaderSupport#loadIconSetIcon(Element)}), and
     * {@code enabled}.
     */
    public static BaseAction buildAction(Element actionElement, String fallbackId, StudioPreviewEnvironment environment) {
        String actionId = BaseLoaderSupport.loadString(actionElement, "id").orElse(fallbackId);
        BaseAction action = new BaseAction(actionId);
        BaseLoaderSupport.loadString(actionElement, "text")
                .ifPresent(text -> action.withText(resolveText(environment, text)));
        BaseComponentLoaderSupport.loadIconSetIcon(actionElement).ifPresent(action::setIcon);
        BaseLoaderSupport.loadBoolean(actionElement, "enabled", action::setEnabled);
        return action;
    }

    /**
     * Recursively searches {@code parent}'s descendants for an {@code <action id="...">} element
     * matching {@code actionId} (covers e.g. an {@code <actions>} block declared in the view).
     *
     * @return the matching element, or {@code null} if none is found
     */
    @Nullable
    public static Element findDescendantAction(Element parent, String actionId) {
        for (Element child : parent.elements()) {
            if (ACTION_ELEMENT.equals(child.getName()) && actionId.equals(child.attributeValue("id"))) {
                return child;
            }
            Element found = findDescendantAction(child, actionId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
