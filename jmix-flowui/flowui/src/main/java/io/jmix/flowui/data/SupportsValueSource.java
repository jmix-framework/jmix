package io.jmix.flowui.data;

import javax.annotation.Nullable;

public interface SupportsValueSource<V> {

    @Nullable
    ValueSource<V> getValueSource();

    void setValueSource(@Nullable ValueSource<V> valueSource);
}
