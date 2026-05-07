package io.jmix.texttodata.introspection.model;

import org.jspecify.annotations.Nullable;

import java.util.List;

public interface EntityPropertyDescriptor {

    String getName();

    List<String> getLocalizedNames();

    String getJavaType();

    String getPropertyType();

    @Nullable
    Boolean getIdentifier();

    Boolean getPersistent();

    Boolean getMandatory();

    @Nullable
    String getComment();
}
