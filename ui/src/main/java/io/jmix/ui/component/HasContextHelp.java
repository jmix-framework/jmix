/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component;

import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * An interface implemented by components that can provide a context help.
 */
public interface HasContextHelp {
    /**
     * @return context help text
     */
    @Nullable
    String getContextHelpText();

    /**
     * Sets context help text. If set, then a special icon will be added for a field.
     *
     * @param contextHelpText context help text to be set
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setContextHelpText(@Nullable String contextHelpText);

    /**
     * @return true if field accepts context help text in HTML format, false otherwise
     */
    boolean isContextHelpTextHtmlEnabled();

    /**
     * Defines if context help text can be presented as HTML.
     *
     * @param enabled true if field accepts context help text in HTML format, false otherwise
     */
    @StudioProperty(defaultValue = "false")
    void setContextHelpTextHtmlEnabled(boolean enabled);

    /**
     * @return a context help icon click handler
     */
    @Nullable
    Consumer<ContextHelpIconClickEvent> getContextHelpIconClickHandler();

    /**
     * Sets a context help icon click handler. If set, then a special
     * icon will be added for a field. Click handler has priority over
     * context help text, i.e. no tooltip with context help text will be shown
     * if click listener is set.
     *
     * @param handler the handler to set
     */
    void setContextHelpIconClickHandler(@Nullable Consumer<ContextHelpIconClickEvent> handler);

    /**
     * Describes context help icon click event.
     */
    class ContextHelpIconClickEvent extends EventObject {

        /**
         * Constructor for a context help icon click event.
         *
         * @param component the Component from which this event originates
         */
        public ContextHelpIconClickEvent(HasContextHelp component) {
            super(component);
        }

        @Override
        public HasContextHelp getSource() {
            return (HasContextHelp) super.getSource();
        }
    }
}