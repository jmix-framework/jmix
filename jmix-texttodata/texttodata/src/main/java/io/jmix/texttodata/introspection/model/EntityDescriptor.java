package io.jmix.texttodata.introspection.model;

import org.jspecify.annotations.Nullable;

import java.util.List;

public class EntityDescriptor {

    protected String name;
    protected List<String> localizedNames;

    protected List<EntityPropertyDescriptor> properties;

    protected String comment;

    public EntityDescriptor(String name,
                            List<String> localizedNames,
                            List<EntityPropertyDescriptor> properties,
                            @Nullable String comment) {
        this.name = name;
        this.localizedNames = localizedNames;
        this.properties = properties;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getComment() {
        return comment;
    }

    public List<String> getLocalizedNames() {
        return localizedNames;
    }

    public List<EntityPropertyDescriptor> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                ", localizedNames=" + localizedNames +
                ", properties=" + properties +
                '}';
    }
}
