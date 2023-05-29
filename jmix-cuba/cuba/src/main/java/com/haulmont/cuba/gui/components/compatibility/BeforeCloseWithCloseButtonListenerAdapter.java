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

package com.haulmont.cuba.gui.components.compatibility;

import com.haulmont.cuba.gui.components.Window;
import io.jmix.ui.component.CloseOriginType;

import java.util.function.Consumer;

@Deprecated
public class BeforeCloseWithCloseButtonListenerAdapter implements Consumer<Window.BeforeCloseEvent> {

    protected final Consumer<Window.BeforeCloseWithCloseButtonEvent> listener;

    public BeforeCloseWithCloseButtonListenerAdapter(Consumer<Window.BeforeCloseWithCloseButtonEvent> listener) {
        this.listener = listener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BeforeCloseWithCloseButtonListenerAdapter that = (BeforeCloseWithCloseButtonListenerAdapter) o;

        return listener.equals(that.listener);
    }

    @Override
    public int hashCode() {
        return listener.hashCode();
    }

    @Override
    public void accept(Window.BeforeCloseEvent beforeCloseEvent) {
        Window.CloseOrigin closeOrigin = beforeCloseEvent.getCloseOrigin();
        if (closeOrigin == CloseOriginType.CLOSE_BUTTON
                || closeOrigin == CloseOriginType.BREADCRUMBS) {

            listener.accept(new Window.BeforeCloseWithCloseButtonEvent(beforeCloseEvent.getSource()));
        }
    }
}
