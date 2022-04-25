package io.jmix.flowui.xml.layout.inittask;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.HasActions;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenActions;
import io.jmix.flowui.sys.ValuePathHelper;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class AbstractAssignActionInitTask<C extends Component> implements ComponentLoader.InitTask {

    protected C component;
    protected String actionId;
    protected Screen screen;

    public AbstractAssignActionInitTask(C component, String actionId, Screen screen) {
        this.component = component;
        this.actionId = actionId;
        this.screen = screen;
    }

    @Override
    public void execute(ComponentContext context, Screen screen) {
        String[] elements = ValuePathHelper.parse(actionId);
        if (elements.length > 1) {
            String id = elements[elements.length - 1];

            String prefix = ValuePathHelper.pathPrefix(elements);
            Component holder = screen.getContent().findComponent(prefix).orElse(null);
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
        ScreenActions screenActions = context.getScreenActions();
        Action action = screenActions.getAction(actionId);
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
}
