package io.jmix.flowui.view.builder;

import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.view.View;

import java.util.Optional;
import java.util.function.Consumer;

public interface DialogWindowBuilder<S extends View<?>> {

    View<?> getOrigin();

    Optional<String> getViewId();

    Optional<Consumer<AfterOpenEvent<S>>> getAfterOpenListener();

    Optional<Consumer<AfterCloseEvent<S>>> getAfterCloseListener();
}
