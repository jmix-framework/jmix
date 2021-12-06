package io.jmix.security.model;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * Represents row level policy in-memory predicate. The predicate expects an entity instance as an argument. Use {@link
 * RowLevelBiPredicate} if you need to use Spring beans inside the function.
 */
@FunctionalInterface
public interface RowLevelPredicate<T> extends Predicate<T>, Serializable {
}