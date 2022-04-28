package io.jmix.flowui.xml.layout.inittask;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasAction;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;

public class AssignActionInitTask<C extends Component & HasAction> extends AbstractAssignActionInitTask<C> {

    public AssignActionInitTask(C component, String actionId, Screen screen) {
        super(component, actionId, screen);
    }

    @Override
    protected boolean hasOwnAction(String id) {
        return false;
    }

    @Override
    protected void addAction(ComponentContext context, Action action) {
        component.setAction(action, false);
    }
}
