package io.jmix.flowui.view.navigation;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.sys.ViewSupport;
import org.springframework.stereotype.Component;

@Internal
@Component("flowui_ListViewNavigationProcessor")
public class ListViewNavigationProcessor extends AbstractNavigationProcessor<ListViewNavigator<?>> {

    public ListViewNavigationProcessor(ViewSupport viewSupport,
                                       ViewRegistry viewRegistry,
                                       ViewNavigationSupport navigationSupport) {
        super(viewSupport, viewRegistry, navigationSupport);
    }

    @Override
    protected Class<? extends View> inferViewClass(ListViewNavigator<?> navigator) {
        return viewRegistry.getLookupViewInfo(navigator.getEntityClass()).getControllerClass();
    }
}
