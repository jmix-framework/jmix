package io.jmix.texttodata.introspection.model;

import org.jspecify.annotations.Nullable;

import java.util.List;

public class DatatypePropertyDescriptor extends AbstractEntityPropertyDescriptor {

    public DatatypePropertyDescriptor(String name,
                                      List<String> localizedNames,
                                      String javaType,
                                      String propertyType,
                                      @Nullable Boolean identifier,
                                      @Nullable String comment) {
        super(name, localizedNames, javaType, propertyType, identifier, comment);
    }
}
