package io.jmix.flowui.view.builder;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.Views;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Internal
@Component("flowui_WindowBuilderProcessor")
public class WindowBuilderProcessor extends AbstractWindowBuilderProcessor {

    protected ApplicationContext applicationContext;

    protected ViewRegistry viewRegistry;

    public WindowBuilderProcessor(ApplicationContext applicationContext,
                                  Views views,
                                  ViewRegistry viewRegistry) {
        super(applicationContext, views, viewRegistry);
    }

    public <S extends View<?>> DialogWindow<S> build(WindowBuilder<S> builder) {
        S view = createView(builder);

        DialogWindow<S> dialog = createDialog(view);
        initDialog(builder, dialog);

        return dialog;
    }

    @Override
    protected <S extends View<?>> Class<S> inferViewClass(DialogWindowBuilder<S> builder) {
        throw new IllegalStateException("Can't open a view. " +
                "Either view id or view class must be defined");
    }
}
