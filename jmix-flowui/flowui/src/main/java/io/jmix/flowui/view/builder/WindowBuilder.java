package io.jmix.flowui.view.builder;

import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class WindowBuilder<S extends View<?>> extends AbstractWindowBuilder<S> {

    protected Class<S> viewClass;

    public WindowBuilder(View<?> origin,
                         Class<S> viewClass,
                         Function<? extends WindowBuilder<S>, DialogWindow<S>> handler) {
        super(origin, handler);

        this.viewClass = viewClass;
    }

    public WindowBuilder(View<?> origin,
                         String viewId,
                         Function<? extends WindowBuilder<S>, DialogWindow<S>> handler) {
        super(origin, handler);

        this.viewId = viewId;
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

    public Optional<String> getViewId() {
        return Optional.ofNullable(viewId);
    }

    public Optional<Class<S>> getViewClass() {
        return Optional.ofNullable(viewClass);
    }
}
