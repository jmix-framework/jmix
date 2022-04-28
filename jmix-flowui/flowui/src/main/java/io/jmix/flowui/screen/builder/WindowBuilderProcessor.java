package io.jmix.flowui.screen.builder;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.Screens;
import io.jmix.flowui.screen.DialogWindow;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Internal
@Component("flowui_WindowBuilderProcessor")
public class WindowBuilderProcessor extends AbstractWindowBuilderProcessor {

    protected ApplicationContext applicationContext;

    protected Screens screens;
    protected ScreenRegistry screenRegistry;

    public WindowBuilderProcessor(ApplicationContext applicationContext,
                                  Screens screens,
                                  ScreenRegistry screenRegistry) {
        super(applicationContext, screens, screenRegistry);
    }

    public <S extends Screen> DialogWindow<S> buildScreen(WindowBuilder<S> builder) {
        S screen = createScreen(builder);

        DialogWindow<S> dialog = createDialog(screen);
        initDialog(builder, dialog);

        return dialog;
    }

    @Override
    protected <S extends Screen> Class<S> inferScreenClass(DialogWindowBuilder<S> builder) {
        throw new IllegalStateException("Can't open a screen. " +
                "Either screen id or screen class must be defined");
    }
}
