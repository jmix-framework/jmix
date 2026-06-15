package io.jmix.core.constraint;

import io.jmix.core.accesscontext.AccessContext;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface RowLevelConstraint<T extends AccessContext> extends AccessConstraint<T> {
}
