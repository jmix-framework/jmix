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

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioFacet;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Client-side component that connects a {@link Button} and {@link TextField} or {@link TextArea} input.
 * Copies the text content to the clipboard on button click.
 */
@StudioFacet(
        xmlElement = "clipboardTrigger",
        caption = "ClipboardTrigger",
        description = "Copies the text content of the input to the clipboard on button click",
        category = "Facets",
        defaultProperty = "input",
        icon = "io/jmix/ui/icon/facet/clipboardTrigger.svg",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/facets/clipboard-trigger.html"
)
public interface ClipboardTrigger extends Facet {

    /**
     * Sets input field: {@link TextField} or {@link TextArea}.
     *
     * @param input input field
     */
    @StudioProperty(type = PropertyType.COMPONENT_REF,
            options = "io.jmix.ui.component.TextInputField")
    void setInput(@Nullable TextInputField<?> input);

    /**
     * @return input field
     */
    @Nullable
    TextInputField<?> getInput();

    /**
     * Sets target button component.
     *
     * @param button button
     */
    @StudioProperty(type = PropertyType.COMPONENT_REF,
            options = "io.jmix.ui.component.Button")
    void setButton(@Nullable Button button);

    /**
     * @return button
     */
    @Nullable
    Button getButton();

    /**
     * @return true if clipboard copying is supported by web browser
     */
    boolean isSupportedByWebBrowser();

    /**
     * Adds {@link CopyEvent} listener.
     *
     * @param listener listener
     * @return subscription
     */
    Subscription addCopyListener(Consumer<CopyEvent> listener);

    /**
     * Event that is fired when the text content of the input has been copied to the clipboard.
     *
     * @see #addCopyListener(Consumer)
     */
    class CopyEvent extends EventObject {
        private final boolean success;

        public CopyEvent(ClipboardTrigger source, boolean success) {
            super(source);
            this.success = success;
        }

        @Override
        public ClipboardTrigger getSource() {
            return (ClipboardTrigger) super.getSource();
        }

        /**
         * @return true if the text content is set to the client-side clipboard
         */
        public boolean isSuccess() {
            return success;
        }
    }
}