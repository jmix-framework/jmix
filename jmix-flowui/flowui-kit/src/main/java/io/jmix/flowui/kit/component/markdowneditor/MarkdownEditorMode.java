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

package io.jmix.flowui.kit.component.markdowneditor;

/**
 * The display mode of the {@link JmixMarkdownEditor}.
 */
public enum MarkdownEditorMode {

    /**
     * Edit mode — the user can type and format Markdown.
     */
    EDIT("edit"),

    /**
     * Preview mode — the Markdown is rendered as formatted HTML.
     */
    PREVIEW("preview");

    private final String propertyValue;

    MarkdownEditorMode(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    /**
     * Returns the web-component property value for this mode.
     *
     * @return the property value string
     */
    public String getPropertyValue() {
        return propertyValue;
    }

    /**
     * Returns the {@code Mode} corresponding to the given web-component
     * property value, falling back to {@link #EDIT} for unrecognised values.
     *
     * @param value the property value string
     * @return the matching {@code Mode}
     */
    public static MarkdownEditorMode fromPropertyValue(String value) {
        for (MarkdownEditorMode mode : values()) {
            if (mode.getPropertyValue().equals(value)) {
                return mode;
            }
        }
        return EDIT;
    }
}
