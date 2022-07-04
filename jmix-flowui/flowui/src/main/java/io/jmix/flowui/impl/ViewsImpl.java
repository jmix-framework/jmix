package io.jmix.flowui.impl;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import io.jmix.flowui.Views;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.stereotype.Component;

@Component("flowui_Views")
public class ViewsImpl implements Views {

    protected ViewRegistry viewRegistry;

    public ViewsImpl(ViewRegistry viewRegistry) {
        this.viewRegistry = viewRegistry;
    }

    @Override
    public View create(String viewId) {
        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
        return create(viewInfo.getControllerClass());
    }

    @Override
    public <T extends View> T create(Class<T> viewClass) {
        return Instantiator.get(UI.getCurrent()).getOrCreate(viewClass);
    }
}
