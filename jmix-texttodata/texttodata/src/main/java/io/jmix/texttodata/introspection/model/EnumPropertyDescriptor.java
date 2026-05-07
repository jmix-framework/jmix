package io.jmix.texttodata.introspection.model;

import org.jspecify.annotations.Nullable;

import java.util.List;

public class EnumPropertyDescriptor extends AbstractEntityPropertyDescriptor {

    protected EnumClassDescriptor enumType;

    public EnumPropertyDescriptor(String name,
                                  List<String> localizedNames,
                                  String javaType,
                                  String propertyType,
                                  @Nullable Boolean identifier,
                                  @Nullable String comment,
                                  EnumClassDescriptor enumType) {
        super(name, localizedNames, javaType, propertyType, identifier, comment);

        this.enumType = enumType;
    }

    public EnumClassDescriptor getEnumType() {
        return enumType;
    }

    @Override
    protected String fieldsToString() {
        return super.fieldsToString() +
                ", enumType=" + enumType;
    }
}
