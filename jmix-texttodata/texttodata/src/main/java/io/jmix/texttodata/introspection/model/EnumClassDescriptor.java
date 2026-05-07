package io.jmix.texttodata.introspection.model;

import java.util.List;
import java.util.Map;

public class EnumClassDescriptor {

    protected String name;

    protected List<String> localizedNames;

    protected Map<String, EnumValueDescriptor> constants;

    public EnumClassDescriptor(String name,
                               List<String> localizedNames,
                               Map<String, EnumValueDescriptor> constants) {
        this.name = name;
        this.localizedNames = localizedNames;
        this.constants = constants;
    }

    public String getName() {
        return name;
    }

    public List<String> getLocalizedNames() {
        return localizedNames;
    }

    public Map<String, EnumValueDescriptor> getConstants() {
        return constants;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                ", localizedNames=" + localizedNames +
                ", constants=" + constants +
                '}';
    }
}
