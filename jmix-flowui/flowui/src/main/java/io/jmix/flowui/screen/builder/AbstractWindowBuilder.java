package io.jmix.flowui.screen.builder;


import io.jmix.flowui.screen.DialogWindow;
import io.jmix.flowui.screen.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.screen.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class AbstractWindowBuilder<S extends Screen> implements DialogWindowBuilder<S> {

    protected final Screen origin;
    protected final Function<AbstractWindowBuilder<S>, DialogWindow<S>> handler;

    protected String screenId;

    protected Consumer<AfterOpenEvent<S>> afterOpenListener;
    protected Consumer<AfterCloseEvent<S>> afterCloseListener;

    protected AbstractWindowBuilder(Screen origin,
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
    public Screen getOrigin() {
        return origin;
    }

    @Override
    public Optional<String> getScreenId() {
        return Optional.ofNullable(screenId);
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
