package io.jmix.core.entity;

/**
 * Common placeholders interface for null values. Used to support kotlin nullability and prevent eager loading of
 * reference before value holder wrapped by {@code JpaLazyLoadingListener}.
 * Such early instantiated lazy-loaded fields may have reference attributes not covered by lazy-loading value holders
 * which leads to unfetched attribute exceptions.
 */
public interface NoValueCollection {
}
