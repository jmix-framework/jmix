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
import io.jmix.flowui.component.ComponentContainer;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;

import javax.annotation.Nullable;
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
    public void execute(ComponentContext context, View<?> view) {
        if (!(view.getContent() instanceof ComponentContainer)
                && !(view.getContent() instanceof AppLayout)) {
            throw new GuiDevelopmentException("View cannot contain components", context.getFullFrameId());
        }

        getFocusComponent().ifPresent(focusable ->
                focusable.getElement().setProperty("autofocus", true));
    }

    protected Optional<Focusable<?>> getFocusComponent() {
        if (focusComponentId != null) {
            return findFocusComponent(view, focusComponentId);
        } else {
            return findFocusComponent(view);
        }
    }
}
