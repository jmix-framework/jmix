package io.jmix.flowui.kit.component;


import io.jmix.flowui.kit.component.formatter.Formatter;

import javax.annotation.Nullable;

public interface SupportsFormatter<V> {

    @Nullable
    Formatter<V> getFormatter();

    void setFormatter(@Nullable Formatter<? super V> formatter);
}
