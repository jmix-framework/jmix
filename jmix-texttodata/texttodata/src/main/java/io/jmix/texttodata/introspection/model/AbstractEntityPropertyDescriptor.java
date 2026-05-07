package io.jmix.texttodata.introspection.model;

import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class AbstractEntityPropertyDescriptor implements EntityPropertyDescriptor {

    protected String name;

    protected List<String> localizedNames;

    protected String javaType;

    protected String propertyType;

    protected Boolean identifier;

    protected String comment;

    public AbstractEntityPropertyDescriptor(String name,
                                            List<String> localizedNames,
                                            String javaType,
                                            String propertyType,
                                            @Nullable Boolean identifier,
                                            @Nullable String comment) {
        this.name = name;
        this.localizedNames = localizedNames;
        this.javaType = javaType;
        this.propertyType = propertyType;
        this.identifier = identifier;
        this.comment = comment;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getLocalizedNames() {
        return localizedNames;
    }

    @Override
    public String getJavaType() {
        return javaType;
    }

    @Override
    public String getPropertyType() {
        return propertyType;
    }

    @Nullable
    @Override
    public Boolean getIdentifier() {
        return identifier;
    }

    @Nullable
    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + fieldsToString() + '}';
    }

    protected String fieldsToString() {
        return "name='" + name + '\'' +
                ", localizedNames=" + localizedNames +
                ", javaType='" + javaType + '\'' +
                ", propertyType='" + propertyType + '\'' +
                ", identifier=" + identifier +
                ", comment='" + comment + '\'';
    }
}
