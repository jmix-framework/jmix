package io.jmix.flowui.component.delegate;

import io.jmix.flowui.component.grid.DataGrid;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("flowui_GridDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GridDelegate<E> extends AbstractGridDelegate<DataGrid<E>, E> {

    public GridDelegate(DataGrid<E> component) {
        super(component);
    }
}
