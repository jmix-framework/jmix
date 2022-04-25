package io.jmix.flowui.screen.builder;


import io.jmix.flowui.screen.Screen;

import java.util.Optional;

public interface DialogWindowClassBuilder<S extends Screen> extends DialogWindowBuilder<S> {

    Optional<Class<S>> getScreenClass();
}
