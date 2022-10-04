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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import io.jmix.flowui.component.ComponentContainer;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.sys.ValuePathHelper;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewActions;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class AbstractAssignActionInitTask<C extends Component> implements ComponentLoader.InitTask {

    protected C component;
    protected String actionId;
    protected View<?> view;

    public AbstractAssignActionInitTask(C component, String actionId, View<?> view) {
        this.component = component;
        this.actionId = actionId;
        this.view = view;
    }

    @Override
    public void execute(ComponentContext context, View<?> view) {
        if (!(view.getContent() instanceof ComponentContainer)
                && !(view.getContent() instanceof AppLayout)) {
            throw new GuiDevelopmentException("View cannot contain components", context.getFullFrameId());
        }

        String[] elements = ValuePathHelper.parse(actionId);
        if (elements.length > 1) {
            String id = elements[elements.length - 1];

            String prefix = ValuePathHelper.pathPrefix(elements);
            Component holder = getComponent(view, prefix).orElse(null);
            if (holder == null) {
                throw new GuiDevelopmentException(
                        String.format("Can't find component: %s for action: %s", prefix, actionId),
                        context, "Component ID", prefix);
            }

            if (!(holder instanceof HasActions)) {
                throw new GuiDevelopmentException(String.format(
                        "Component '%s' can't contain actions", holder.getId()), context,
                        "Holder ID", holder.getId());
            }

            Action action = ((HasActions) holder).getAction(id);
            if (action == null) {
                throw new GuiDevelopmentException(String.format(
                        "Can't find action '%s' in '%s'", id, holder.getId()), context,
                        "Holder ID", holder.getId());
            }

            addAction(context, action);
        } else if (elements.length == 1) {
            String id = elements[0];
            Action action = getActionRecursively(context, id);

            if (action == null) {
                if (!hasOwnAction(id)) {
                    String message = getExceptionMessage(id);
                    throw new GuiDevelopmentException(message, context.getFullFrameId());
                }
            } else {
                addAction(context, action);
            }
        } else {
            throw new GuiDevelopmentException("Empty action name", context.getFullFrameId());
        }
    }

    protected abstract boolean hasOwnAction(String id);

    protected abstract void addAction(ComponentContext context, Action action);

    @Nullable
    protected Action getActionRecursively(ComponentContext context, String actionId) {
        ViewActions viewActions = context.getViewActions();
        Action action = viewActions.getAction(actionId);
        if (action == null) {
            Optional<ComponentContext> parentContext = context.getParent();
            if (parentContext.isPresent()) {
                return getActionRecursively(parentContext.get(), actionId);
            }
        }
        return action;
    }

    protected String getExceptionMessage(String id) {
        return String.format("Can't find action with %s id", id);
    }

    protected Optional<Component> getComponent(View<?> view, String id) {
        return UiComponentUtils.findComponent(view, id);
    }
}
