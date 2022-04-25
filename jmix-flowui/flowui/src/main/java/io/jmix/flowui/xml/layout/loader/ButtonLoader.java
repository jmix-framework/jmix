package io.jmix.flowui.xml.layout.loader;

import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.xml.layout.inittask.AssignActionInitTask;
import org.dom4j.Element;

public class ButtonLoader extends AbstractComponentLoader<JmixButton> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(JmixButton.class);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        loadText(resultComponent, element);
        loadAction(resultComponent, element);
        // TODO: gg, implement
    }

    protected void loadAction(JmixButton component, Element element) {
        loadString(element, "action")
                .ifPresent(actionId -> getComponentContext().addInitTask(
                        new AssignActionInitTask<>(component, actionId, getComponentContext().getScreen())
                ));
    }
}
