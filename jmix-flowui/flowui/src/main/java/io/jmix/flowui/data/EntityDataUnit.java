package io.jmix.flowui.data;

import io.jmix.core.metamodel.model.MetaClass;

public interface EntityDataUnit extends DataUnit {

    /**
     * @return {@link MetaClass} of an entity contained in the source
     */
    MetaClass getEntityMetaClass();
}
