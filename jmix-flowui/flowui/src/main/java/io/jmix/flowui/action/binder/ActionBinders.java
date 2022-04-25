package io.jmix.flowui.action.binder;

import com.vaadin.flow.component.Component;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("flowui_ActionBinders")
public class ActionBinders {

    protected ObjectProvider<ActionBinder> actionBinderObjectProvider;

    @Autowired
    public ActionBinders(ObjectProvider<ActionBinder> actionBinderObjectProvider) {
        this.actionBinderObjectProvider = actionBinderObjectProvider;
    }

    @SuppressWarnings("unchecked")
    public <C extends Component> ActionBinder<C> binder(C component) {
        return (ActionBinder<C>) actionBinderObjectProvider.getObject(component);
    }
}
