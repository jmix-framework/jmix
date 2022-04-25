package io.jmix.flowui.data;

import javax.annotation.Nullable;

public interface SupportsListOptions<V> {

    @Nullable
    Options<V> getListOptions();

    void setListOptions(@Nullable Options<V> options);
}
