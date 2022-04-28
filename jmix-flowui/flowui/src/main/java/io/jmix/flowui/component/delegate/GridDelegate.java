package io.jmix.flowui.component.delegate;

import io.jmix.flowui.component.grid.JmixGrid;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("flowui_GridDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GridDelegate<E> extends AbstractGridDelegate<JmixGrid<E>, E> {

    public GridDelegate(JmixGrid<E> component) {
        super(component);
    }
}
