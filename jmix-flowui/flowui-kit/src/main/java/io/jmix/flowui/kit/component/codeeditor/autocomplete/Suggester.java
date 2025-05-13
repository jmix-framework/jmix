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

import java.util.List;

/**
 * Functional interface for creating code completion suggestions for the {@link JmixCodeEditor} component.
 * Autocompletions are suggested based on the current state of the client-side of the component
 * described by {@link SuggestionContext}.
 */
@FunctionalInterface
public interface Suggester {

    /**
     * @param context the current state of the client-side component
     * @return life of suggestions for the current state of the component
     * @see Suggestion
     */
    List<Suggestion> getSuggestions(SuggestionContext context);

    /**
     * A class that describes the current state of the client-side of the {@link JmixCodeEditor} component used
     * to prepare suggestions.
     */
    class SuggestionContext {

        protected String text;
        protected int cursorPosition;
        protected String prefix;

        /**
         * @param text           current value from the client-side of the editor (may not match the server-side value)
         * @param cursorPosition current cursor position in the editor
         * @param prefix         literal before current cursor position
         */
        public SuggestionContext(String text, int cursorPosition, String prefix) {
            this.text = text;
            this.cursorPosition = cursorPosition;
            this.prefix = prefix;
        }

        /**
         * @return current value from client-side of the editor
         */
        public String getText() {
            return text;
        }

        /**
         * @return current cursor position in the editor
         */
        public int getCursorPosition() {
            return cursorPosition;
        }

        /**
         * @return literal before current cursor position
         */
        public String getPrefix() {
            return prefix;
        }
    }
}
