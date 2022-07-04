package io.jmix.flowui.xml.layout.inittask;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasAction;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;

public class AssignActionInitTask<C extends Component & HasAction> extends AbstractAssignActionInitTask<C> {

    public AssignActionInitTask(C component, String actionId, View view) {
        super(component, actionId, view);
    }

    @Override
    protected boolean hasOwnAction(String id) {
        return false;
    }

    @Override
    protected void addAction(ComponentContext context, Action action) {
        component.setAction(action, true);
    }
}
