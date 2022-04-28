package io.jmix.flowui.component;

import io.jmix.core.metamodel.datatype.Datatype;

import javax.annotation.Nullable;

public interface SupportsDatatype<V> {

    @Nullable
    Datatype<V> getDatatype();

    void setDatatype(@Nullable Datatype<V> datatype);
}
