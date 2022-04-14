package io.jmix.eclipselink.impl.lazyloading;

import org.eclipse.persistence.indirection.IndirectSet;

/**
 * Use for kotlin non-null references to prevent eager instantiation of lazy-loaded fields.
 */
public class NotInstantiatedSet<E> extends IndirectSet<E> {
    @Override
    public boolean isInstantiated() {
        return false;
    }
}
