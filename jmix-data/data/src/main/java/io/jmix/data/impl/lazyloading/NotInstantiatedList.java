package io.jmix.data.impl.lazyloading;

import io.jmix.core.entity.NoValueCollection;
import org.eclipse.persistence.indirection.IndirectList;

/**
 * Use for kotlin non-null references to prevent eager instantiation of lazy-loaded fields.
 *
 * @see NoValueCollection
 */
public class NotInstantiatedList<E> extends IndirectList<E> implements NoValueCollection {
    @Override
    public boolean isInstantiated() {
        return false;
    }
}
