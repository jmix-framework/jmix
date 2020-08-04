package io.jmix.core.constraint;

import io.jmix.core.context.AccessContext;

public interface AccessConstraint<T extends AccessContext> {

    Class<T> getContextType();

    void applyTo(T context);
}
