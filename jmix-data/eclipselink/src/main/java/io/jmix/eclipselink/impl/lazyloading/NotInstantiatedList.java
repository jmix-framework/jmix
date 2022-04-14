package io.jmix.eclipselink.impl.lazyloading;

import org.eclipse.persistence.indirection.IndirectList;

/**
 * Use for kotlin non-null references to prevent eager instantiation of lazy-loaded fields.
 */
public class NotInstantiatedList<E> extends IndirectList<E> {
    @Override
    public boolean isInstantiated() {
        return false;
    }
}
