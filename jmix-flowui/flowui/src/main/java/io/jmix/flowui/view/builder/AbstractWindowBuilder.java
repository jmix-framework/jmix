package io.jmix.flowui.view.builder;


import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class AbstractWindowBuilder<S extends View<?>> implements DialogWindowBuilder<S> {

    protected final View<?> origin;
    protected final Function<AbstractWindowBuilder<S>, DialogWindow<S>> handler;

    protected String viewId;

    protected Consumer<AfterOpenEvent<S>> afterOpenListener;
    protected Consumer<AfterCloseEvent<S>> afterCloseListener;

    protected AbstractWindowBuilder(View<?> origin,
                                    Function<? extends AbstractWindowBuilder<S>, DialogWindow<S>> handler) {
        this.origin = origin;
        //noinspection unchecked
        this.handler = (Function<AbstractWindowBuilder<S>, DialogWindow<S>>) handler;
    }

    public AbstractWindowBuilder<S> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<S>> listener) {
        this.afterOpenListener = listener;
        return this;
    }

    public AbstractWindowBuilder<S> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<S>> listener) {
        this.afterCloseListener = listener;
        return this;
    }

    @Override
    public View<?> getOrigin() {
        return origin;
    }

    @Override
    public Optional<String> getViewId() {
        return Optional.ofNullable(viewId);
    }

    @Override
    public Optional<Consumer<AfterOpenEvent<S>>> getAfterOpenListener() {
        return Optional.ofNullable(afterOpenListener);
    }

    @Override
    public Optional<Consumer<AfterCloseEvent<S>>> getAfterCloseListener() {
        return Optional.ofNullable(afterCloseListener);
    }

    public DialogWindow<S> build() {
        return handler.apply(this);
    }

    public DialogWindow<S> open() {
        DialogWindow<S> dialogWindow = build();
        dialogWindow.open();
        return dialogWindow;
    }
}
