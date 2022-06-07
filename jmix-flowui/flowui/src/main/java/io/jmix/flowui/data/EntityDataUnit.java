package io.jmix.flowui.data;

import io.jmix.core.metamodel.model.MetaClass;

import javax.annotation.Nullable;

public interface EntityDataUnit extends DataUnit {

    /**
     * @return {@link MetaClass} of an entity contained in the source
     */
    @Nullable
    MetaClass getEntityMetaClass();
}
