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

@Tag("jmix-breadcrumb")
@JsModule("./src/breadcrumbs/jmix-breadcrumb.js")
public class JmixBreadcrumb extends Component implements HasAriaLabel, HasEnabled, Focusable<JmixBreadcrumb> {

    protected Registration clickRegistration;

    public JmixBreadcrumb() {
    }

    public String getText() {
        return getElement().getProperty("text", "");
    }

    public void setText(String text) {
        getElement().setProperty("text", text);
    }

    public JmixBreadcrumb withText(String text) {
        setText(text);
        return this;
    }

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

    public JmixBreadcrumb withClickHandler(ComponentEventListener<ClickEvent<JmixBreadcrumb>> listener) {
        setClickHandler(listener);
        return this;
    }

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
