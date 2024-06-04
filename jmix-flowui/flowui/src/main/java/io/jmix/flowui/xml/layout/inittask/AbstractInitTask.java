/*
 * Copyright 2024 Haulmont.
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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentLoader;

public abstract class AbstractInitTask implements ComponentLoader.InitTask {

    @Override
    public void execute(ComponentLoader.ComponentContext context, View<?> view) {
        execute(context);
    }

    /**
     * Gets a context origin component if it can contain nexted components.
     *
     * @param context a context object
     * @return a context origin component if it can contain nexted components
     * @throws GuiDevelopmentException if origin cannot contain components
     */
    protected Component getOrigin(ComponentLoader.Context context) {
        Component origin = context.getOrigin();

        if (origin instanceof View<?> view) {
            Component content = view.getContent();
            if (!(UiComponentUtils.isContainer(content)
                    || content instanceof AppLayout)) {
                throw new GuiDevelopmentException(view.getClass().getSimpleName() +
                        "'s content cannot contain components", context);
            }
        } else if (!(origin instanceof Fragment)
                && !UiComponentUtils.isContainer(origin)) {
            throw new GuiDevelopmentException(origin.getClass().getSimpleName() +
                    " cannot contain components", context);
        }

        return origin;
    }
}
