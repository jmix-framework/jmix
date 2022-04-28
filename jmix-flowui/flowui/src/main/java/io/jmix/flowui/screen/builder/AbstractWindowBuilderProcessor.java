package io.jmix.flowui.screen.builder;

import io.jmix.flowui.Screens;
import io.jmix.flowui.screen.DialogWindow;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenRegistry;
import io.jmix.flowui.sys.BeanUtil;
import org.springframework.context.ApplicationContext;

public abstract class AbstractWindowBuilderProcessor {

    protected ApplicationContext applicationContext;

    protected Screens screens;
    protected ScreenRegistry screenRegistry;

    public AbstractWindowBuilderProcessor(ApplicationContext applicationContext,
                                          Screens screens,
                                          ScreenRegistry screenRegistry) {
        this.applicationContext = applicationContext;
        this.screens = screens;
        this.screenRegistry = screenRegistry;
    }

    protected <S extends Screen> DialogWindow<S> createDialog(S screen) {
        DialogWindow<S> dialogWindow = new DialogWindow<>(screen);
        BeanUtil.autowireContext(applicationContext, dialogWindow);

        return dialogWindow;
    }

    protected <S extends Screen> S createScreen(DialogWindowBuilder<S> builder) {
        Class<S> screenClass = getScreenClass(builder);
        return screens.create(screenClass);
    }

    @SuppressWarnings("unchecked")
    protected <S extends Screen> Class<S> getScreenClass(DialogWindowBuilder<S> builder) {
        if (builder.getScreenId().isPresent()) {
            String screenId = builder.getScreenId().get();
            return (Class<S>) screenRegistry.getScreenInfo(screenId).getControllerClass();
        } else if (builder instanceof DialogWindowClassBuilder
                && ((DialogWindowClassBuilder<?>) builder).getScreenClass().isPresent()) {
            return ((DialogWindowClassBuilder<S>) builder).getScreenClass().get();
        } else {
            return inferScreenClass(builder);
        }
    }

    protected abstract <S extends Screen> Class<S> inferScreenClass(DialogWindowBuilder<S> builder);

    protected <S extends Screen> void initDialog(DialogWindowBuilder<S> builder, DialogWindow<S> dialog) {
        builder.getAfterOpenListener().ifPresent(dialog::addAfterOpenListener);
        builder.getAfterCloseListener().ifPresent(dialog::addAfterCloseListener);
    }
}
