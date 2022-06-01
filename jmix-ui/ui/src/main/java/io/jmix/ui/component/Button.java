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
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;

@StudioComponent(
        caption = "Button",
        category = "Components",
        xmlElement = "button",
        icon = "io/jmix/ui/icon/component/button.svg",
        canvasTextProperty = "caption",
        canvasBehaviour = CanvasBehaviour.BUTTON,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/button.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "primary", type = PropertyType.BOOLEAN, defaultValue = "false"),
        }
)
public interface Button extends Component, Component.HasCaption, Component.BelongToFrame, ActionOwner,
        Component.HasIcon, Component.Focusable, HasHtmlCaption, HasHtmlDescription,
        HasHtmlSanitizer {
    String NAME = "button";

    /**
     * Determines if a button is automatically disabled when clicked. If this is
     * set to true the button will be automatically disabled when clicked,
     * typically to prevent (accidental) extra clicks on a button.
     *
     * @param disableOnClick disable on click option.
     */
    @StudioProperty(defaultValue = "false")
    void setDisableOnClick(boolean disableOnClick);

    /**
     * @return true if the button is disabled when clicked.
     */
    boolean isDisableOnClick();

    /**
     * @return action's shortcut
     */
    @Nullable
    KeyCombination getShortcutCombination();

    /**
     * Sets shortcut combination.
     *
     * @param shortcut key combination
     */
    void setShortcutCombination(@Nullable KeyCombination shortcut);

    /**
     * Sets shortcut from string representation.
     *
     * @param shortcut string of type "Modifiers-Key", e.g. "Alt-N". Case-insensitive.
     */
    @StudioProperty(type = PropertyType.SHORTCUT)
    void setShortcut(@Nullable String shortcut);

    /**
     * Simulates a button click, notifying all server-side listeners.
     * <p>
     * No action is taken if the button is disabled.
     */
    void click();

    Subscription addClickListener(Consumer<ClickEvent> listener);

    /**
     * Event sent when the button is clicked.
     */
    class ClickEvent extends EventObject {
        public ClickEvent(Button source) {
            super(source);
        }

        @Override
        public Button getSource() {
            return (Button) super.getSource();
        }
    }
}