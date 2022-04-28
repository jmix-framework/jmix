package io.jmix.flowui.screen.builder;

import io.jmix.flowui.screen.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.screen.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.screen.Screen;

import java.util.Optional;
import java.util.function.Consumer;

public interface DialogWindowBuilder<S extends Screen> {

    Screen getOrigin();

    Optional<String> getScreenId();

    Optional<Consumer<AfterOpenEvent<S>>> getAfterOpenListener();

    Optional<Consumer<AfterCloseEvent<S>>> getAfterCloseListener();
}
