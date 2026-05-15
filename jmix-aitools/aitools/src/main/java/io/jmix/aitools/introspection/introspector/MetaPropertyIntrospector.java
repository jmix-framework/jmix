package io.jmix.aitools.introspection.introspector;

import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.aitools.introspection.model.EntityPropertyDescriptor;
import org.jspecify.annotations.Nullable;

public interface MetaPropertyIntrospector {

    /**
     * Checks if this introspector can handle the given {@link MetaProperty} type.
     *
     * @param property the {@link MetaProperty} to check
     * @return true if this introspector can handle this property type
     */
    boolean supports(MetaProperty property);

    /**
     * Introspects a {@link MetaProperty} to an AI-optimized property descriptor.
     *
     * @param property the {@link MetaProperty} to introspect
     * @return {@link EntityPropertyDescriptor} representation of this property or {@code null} if this introspector
     * cannot handle it
     */
    @Nullable
    EntityPropertyDescriptor introspect(MetaProperty property);
}
