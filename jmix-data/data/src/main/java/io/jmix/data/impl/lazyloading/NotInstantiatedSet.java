package io.jmix.data.impl.lazyloading;

import io.jmix.core.entity.NoValueCollection;
import org.eclipse.persistence.indirection.IndirectSet;

/**
 * Use for kotlin non-null references to prevent eager instantiation of lazy-loaded fields.
 *
 * @see NoValueCollection
 */
public class NotInstantiatedSet<E> extends IndirectSet<E> implements NoValueCollection {
    @Override
    public boolean isInstantiated() {
        return false;
    }
}
