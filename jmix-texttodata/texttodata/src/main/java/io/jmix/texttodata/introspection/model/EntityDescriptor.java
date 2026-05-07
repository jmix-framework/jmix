package io.jmix.texttodata.introspection.model;

import java.util.List;

public class EntityDescriptor {

    protected String name;
    protected List<String> localizedNames;

    protected List<EntityPropertyDescriptor> properties;

    public EntityDescriptor(String name, List<String> localizedNames, List<EntityPropertyDescriptor> properties) {
        this.name = name;
        this.localizedNames = localizedNames;
        this.properties = properties;
    }

    public String getName() {
        return name;
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
                ", localizedNames=" + localizedNames +
                ", properties=" + properties +
                '}';
    }
}
