package io.jmix.core.constraint;

import io.jmix.core.accesscontext.AccessContext;

public interface AccessConstraint<T extends AccessContext> {

    Class<T> getContextType();

    void applyTo(T context);
}
