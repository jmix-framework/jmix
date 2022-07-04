package io.jmix.flowui.view.navigation;

import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.sys.ViewSupport;
import org.springframework.stereotype.Component;

@Component("flowui_ViewNavigationProcessor")
public class ViewNavigationProcessor extends AbstractNavigationProcessor<ViewNavigator> {

    public ViewNavigationProcessor(ViewSupport viewSupport,
                                   ViewRegistry viewRegistry,
                                   ViewNavigationSupport navigationSupport) {
        super(viewSupport, viewRegistry, navigationSupport);
    }

    @Override
    protected Class<? extends View> inferViewClass(ViewNavigator navigator) {
        throw new IllegalStateException("Can't open a view. " +
                "Either view id or view class must be defined");
    }
}
