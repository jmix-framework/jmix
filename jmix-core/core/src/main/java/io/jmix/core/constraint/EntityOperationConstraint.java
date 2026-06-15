package io.jmix.core.constraint;

import io.jmix.core.accesscontext.AccessContext;

import org.jspecify.annotations.NullMarked;

/**
 * An ancestor of all constraints that check access to entity operations and attributes
 * based on the resource policies.
 *
 * @param <T> the type of the access context
 */
@NullMarked
public interface EntityOperationConstraint<T extends AccessContext> extends AccessConstraint<T> {
}
