/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.xml.layout.inittask;

import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static io.jmix.flowui.component.UiComponentUtils.findFocusComponent;

public class FocusComponentInitTask implements ComponentLoader.InitTask {

    protected String focusComponentId;
    protected View<?> view;

    public FocusComponentInitTask(@Nullable String focusComponentId, View<?> view) {
        this.focusComponentId = focusComponentId;
        this.view = view;
    }

    @Override
    public void execute(ComponentLoader.Context context) {
        if (!(UiComponentUtils.isContainer(view.getContent())
                || view.getContent() instanceof AppLayout)) {
            throw new GuiDevelopmentException(View.class.getSimpleName() + " cannot contain components", context);
        }

        getFocusComponent().ifPresent(this::doFocus);
    }

    protected void doFocus(Focusable<?> focusable) {
        if (focusable instanceof Grid<?>) {
            // Using a workaround to focus a Grid, see https://github.com/vaadin/flow-components/issues/2180
            focusable.getElement().executeJs("""
                        setTimeout(function() {
                            $0.shadowRoot.querySelector("tr").focus();
                        }, 100);
                    """);
        } else {
            // Call `focus` explicitly because not all `com.vaadin.flow.component.Focusable`
            // components support `autofocus`.
            focusable.focus();
            focusable.getElement().setProperty("autofocus", true);
        }
    }

    protected Optional<Focusable<?>> getFocusComponent() {
        if (focusComponentId != null) {
            return findFocusComponent(view, focusComponentId);
        } else {
            return findFocusComponent(view);
        }
    }
}
