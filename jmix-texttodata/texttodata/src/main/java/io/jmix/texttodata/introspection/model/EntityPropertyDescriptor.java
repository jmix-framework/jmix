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

    @Nullable
    String getComment();
}
