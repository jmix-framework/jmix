package io.jmix.flowui.xml.layout.inittask;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.HasActions;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AssignActionsInitTask<C extends Component & HasActions> extends AbstractAssignActionInitTask<C> {

    public AssignActionsInitTask(C component, String actionId, Screen screen) {
        super(component, actionId, screen);
    }

    @Override
    protected boolean hasOwnAction(String id) {
        return component.getAction(id) != null;
    }

    @Override
    protected void addAction(ComponentContext context, Action action) {
        List<Action> existingActions = new ArrayList<>(component.getActions());
        for (Action existingAction : existingActions) {
            // Comparing the id of an existing action with the full ID (including path) of the action to be added
            if (Objects.equals(existingAction.getId(), actionId)) {
                int index = existingActions.indexOf(existingAction);
                component.removeAction(existingAction);
                component.addAction(action, index);
                break;
            }
        }
    }
}
