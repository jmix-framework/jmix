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

package io.jmix.tabbedmode.component.breadcrumbs;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;

/**
 * Server-side component for the {@code <jmix-breadcrumb>} element.
 * <p>
 * To use it, add it to the {@link JmixBreadcrumbs}.
 */
@Tag("jmix-breadcrumb")
@JsModule("./src/breadcrumbs/jmix-breadcrumb.js")
public class JmixBreadcrumb extends Component implements HasAriaLabel, HasEnabled, Focusable<JmixBreadcrumb> {

    protected Registration clickRegistration;

    public JmixBreadcrumb() {
    }

    /**
     * Returns the text of the breadcrumb component.
     *
     * @return the text of the breadcrumb component
     */
    public String getText() {
        return getElement().getProperty("text", "");
    }

    /**
     * Sets the text of the breadcrumb component.
     *
     * @param text the text of the breadcrumb component
     */
    public void setText(String text) {
        getElement().setProperty("text", text);
    }

    /**
     * Sets a click handler for the breadcrumb component.
     *
     * @param listener the listener to handle click events on the breadcrumb
     */
    public void setClickHandler(ComponentEventListener<ClickEvent<JmixBreadcrumb>> listener) {
        if (clickRegistration != null) {
            clickRegistration.remove();
            clickRegistration = null;
        }

        clickRegistration = getElement().addEventListener("click", event ->
                        listener.onComponentEvent(new ClickEvent<>(this, true)))
                // language=javascript
                .setFilter("""
                        typeof element.nextElementSibling !== 'undefined'
                            && element.nextElementSibling !== null
                        """);
    }

    /**
     * Represents a click event triggered on a {@link JmixBreadcrumb} component.
     *
     * @param <C> the type of the component on which the event is triggered
     */
    public static class ClickEvent<C extends JmixBreadcrumb> extends ComponentEvent<C> {

        /**
         * Creates a new click event.
         *
         * @param source     the source component
         * @param fromClient {@code true} if the event originated from the client
         *                   side, {@code false} otherwise
         */
        public ClickEvent(C source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}
