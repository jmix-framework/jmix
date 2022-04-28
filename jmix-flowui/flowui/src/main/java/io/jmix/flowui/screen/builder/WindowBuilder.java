package io.jmix.flowui.screen.builder;

import io.jmix.flowui.screen.DialogWindow;
import io.jmix.flowui.screen.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.screen.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class WindowBuilder<S extends Screen> extends AbstractWindowBuilder<S> {

    protected Class<S> screenClass;

    public WindowBuilder(Screen origin,
                         Class<S> screenClass,
                         Function<? extends WindowBuilder<S>, DialogWindow<S>> handler) {
        super(origin, handler);

        this.screenClass = screenClass;
    }

    public WindowBuilder(Screen origin,
                         String screenId,
                         Function<? extends WindowBuilder<S>, DialogWindow<S>> handler) {
        super(origin, handler);

        this.screenId = screenId;
    }

    @Override
    public WindowBuilder<S> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<S>> listener) {
        super.withAfterOpenListener(listener);
        return this;
    }

    @Override
    public WindowBuilder<S> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<S>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    public Optional<String> getScreenId() {
        return Optional.ofNullable(screenId);
    }

    public Optional<Class<S>> getScreenClass() {
        return Optional.ofNullable(screenClass);
    }
}
