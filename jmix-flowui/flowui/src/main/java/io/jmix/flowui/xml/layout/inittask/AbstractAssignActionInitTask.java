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
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.sys.ValuePathHelper;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentLoader.Context;
import org.springframework.lang.Nullable;

import java.util.function.Consumer;

public abstract class AbstractAssignActionInitTask<C extends Component> extends AbstractInitTask {

    protected C component;
    protected String actionId;

    protected Consumer<Action> afterExecuteHandler;

    @Deprecated(since = "2.3", forRemoval = true)
    public AbstractAssignActionInitTask(C component, String actionId, View<?> view) {
        this(component, actionId);
    }

    public AbstractAssignActionInitTask(C component, String actionId) {
        this.component = component;
        this.actionId = actionId;
    }

    @Override
    public void execute(Context context) {
        Component origin = getOrigin(context);

        String[] elements = ValuePathHelper.parse(actionId);
        if (elements.length > 1) {
            String id = elements[elements.length - 1];

            String prefix = ValuePathHelper.pathPrefix(elements);
            Component holder = UiComponentUtils.findComponent(origin, prefix).orElse(null);
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
            runAfterExecuteHandler(action);
        } else if (elements.length == 1) {
            String id = elements[0];
            Action action = getActionRecursively(context, id);
            if (action == null) {
                if (!hasOwnAction(id)) {
                    String message = getExceptionMessage(id);
                    throw new GuiDevelopmentException(message, context);
                }
            } else {
                addAction(context, action);
                runAfterExecuteHandler(action);
            }
        } else {
            throw new GuiDevelopmentException("Empty action name", context);
        }
    }

    protected abstract boolean hasOwnAction(String id);

    protected abstract void addAction(Context context, Action action);

    public void setAfterExecuteHandler(@Nullable Consumer<Action> afterExecuteHandler) {
        this.afterExecuteHandler = afterExecuteHandler;
    }

    protected void runAfterExecuteHandler(Action action) {
        if (afterExecuteHandler != null) {
            afterExecuteHandler.accept(action);
        }
    }

    @Nullable
    protected Action getActionRecursively(Context context, String actionId) {
        HasActions actionsHolder = context.getActionsHolder();
        Action action = actionsHolder.getAction(actionId);
        if (action == null) {
            Context parentContext = context.getParentContext();
            if (parentContext != null) {
                return getActionRecursively(parentContext, actionId);
            }
        }

        return action;
    }

    protected String getExceptionMessage(String id) {
        return String.format("Can't find action with %s id", id);
    }
}
