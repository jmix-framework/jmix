package io.jmix.core.constraint;

import io.jmix.core.accesscontext.AccessContext;

/**
 * Base interface of classes that make authorization decisions.
 *
 * @param <T> access context for which this constraint is applied
 */
public interface AccessConstraint<T extends AccessContext> {

    /**
     * Returns the access constraint type.
     */
    Class<T> getContextType();

    /**
     * Applies the constraint to the given access context.
     */
    void applyTo(T context);
}
