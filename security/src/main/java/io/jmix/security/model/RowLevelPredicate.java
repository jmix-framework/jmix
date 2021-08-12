package io.jmix.security.model;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * Represents row level policy in-memory predicate
 */
@FunctionalInterface
public interface RowLevelPredicate<T> extends Predicate<T>, Serializable {
}
