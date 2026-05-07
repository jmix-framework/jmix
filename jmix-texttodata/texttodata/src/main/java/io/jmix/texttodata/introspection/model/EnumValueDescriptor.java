package io.jmix.texttodata.introspection.model;

import java.util.List;

public class EnumValueDescriptor {

    protected Object id;

    protected String name;

    protected List<String> localizedName;

    public EnumValueDescriptor(Object id, String name, List<String> localizedName) {
        this.id = id;
        this.name = name;
        this.localizedName = localizedName;
    }

    public Object getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getLocalizedName() {
        return localizedName;
    }

    @Override
    public String toString() {
        return "EnumValueDescriptor{" +
                "id=" + id +
                ", name='" + name + "'" +
                ", localizedName=" + localizedName +
                '}';
    }
}
