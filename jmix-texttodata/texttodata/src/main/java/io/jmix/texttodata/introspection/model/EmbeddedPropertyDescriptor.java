package io.jmix.texttodata.introspection.model;

import org.jspecify.annotations.Nullable;

import java.util.List;

public class EmbeddedPropertyDescriptor extends AbstractEntityPropertyDescriptor {

    public EmbeddedPropertyDescriptor(String name,
                                      List<String> localizedNames,
                                      String javaType,
                                      String propertyType,
                                      @Nullable Boolean identifier,
                                      @Nullable String comment) {
        super(name, localizedNames, javaType, propertyType, identifier, comment);
    }

    public Boolean getEmbedded() {
        return true;
    }

    @Override
    protected String fieldsToString() {
        return super.fieldsToString() +
                ", embedded=true";
    }
}
