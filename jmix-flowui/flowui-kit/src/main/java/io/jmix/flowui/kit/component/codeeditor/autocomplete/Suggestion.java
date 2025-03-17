/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.kit.component.codeeditor.autocomplete;

import io.jmix.flowui.kit.component.codeeditor.JmixCodeEditor;
import jakarta.annotation.Nullable;

import java.io.Serializable;

/**
 * Class for single suggestion from {@link Suggester} for {@link JmixCodeEditor} component.
 */
public class Suggestion implements Serializable {

    protected String displayText;
    protected String suggestionText;
    protected String descriptionText;

    /**
     * @param displayText    text displayed in the popup window for suggestions
     * @param suggestionText the value inserted into the editor if the suggestion is selected
     */
    public Suggestion(String displayText, String suggestionText) {
        this(displayText, suggestionText, null);
    }

    /**
     * @param displayText     text displayed in the popup window for suggestions
     * @param suggestionText  the value inserted into the editor if the suggestion is selected
     * @param descriptionText the hint text that appears next to the {@link #displayText} in the popup window
     *                        for suggestions
     */
    public Suggestion(String displayText, String suggestionText, @Nullable String descriptionText) {
        this.displayText = displayText;
        this.suggestionText = suggestionText;
        this.descriptionText = descriptionText;
    }

    /**
     * @return text displayed in the popup window for suggestions
     */
    public String getDisplayText() {
        return displayText;
    }

    /**
     * @return the value inserted into the editor if the suggestion is selected
     */
    public String getSuggestionText() {
        return suggestionText;
    }

    /**
     * @return the hint text that appears next to the {@link #displayText} in the popup window
     * for suggestions
     */
    public String getDescriptionText() {
        return descriptionText;
    }
}
