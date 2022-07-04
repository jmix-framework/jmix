package io.jmix.flowui.view.builder;


import io.jmix.flowui.view.View;

import java.util.Optional;

public interface DialogWindowClassBuilder<S extends View<?>> extends DialogWindowBuilder<S> {

    Optional<Class<S>> getViewClass();
}
