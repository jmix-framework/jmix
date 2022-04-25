package io.jmix.flowui.component;

import io.jmix.core.metamodel.model.MetaClass;

import javax.annotation.Nullable;

public interface SupportsMetaClass {

    @Nullable
    MetaClass getMetaClass();

    void setMetaClass(@Nullable MetaClass metaClass);
}
