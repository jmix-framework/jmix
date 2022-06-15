package io.jmix.flowui.data;

import javax.annotation.Nullable;

public interface SupportsOptions<V> {

    @Nullable
    Options<V> getOptions();

    void setOptions(@Nullable Options<V> options);

    // TODO: gg, add setOptionsEnum
}
