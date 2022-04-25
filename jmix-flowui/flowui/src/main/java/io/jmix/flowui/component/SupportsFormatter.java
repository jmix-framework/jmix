package io.jmix.flowui.component;

import io.jmix.flowui.component.formatter.Formatter;

import javax.annotation.Nullable;

public interface SupportsFormatter<V> {

    @Nullable
    Formatter<V> getFormatter();

    void setFormatter(@Nullable Formatter<? super V> formatter);
}
