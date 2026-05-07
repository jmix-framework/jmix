package io.jmix.texttodata.introspection.model;

import org.jspecify.annotations.Nullable;

import java.util.List;

public class EnumPropertyDescriptor extends AbstractEntityPropertyDescriptor {

    protected EnumClassDescriptor enumType;

    protected String enumStorageMode;

    public EnumPropertyDescriptor(String name,
                                  List<String> localizedNames,
                                  String javaType,
                                  String propertyType,
                                  @Nullable Boolean identifier,
                                  Boolean persistent,
                                  Boolean mandatory,
                                  @Nullable String comment,
                                  EnumClassDescriptor enumType,
                                  @Nullable String enumStorageMode) {
        super(name, localizedNames, javaType, propertyType, identifier, persistent, mandatory, comment);

        this.enumType = enumType;
        this.enumStorageMode = enumStorageMode;
    }

    public EnumClassDescriptor getEnumType() {
        return enumType;
    }

    @Nullable
    public String getEnumStorageMode() {
        return enumStorageMode;
    }

    @Override
    protected String fieldsToString() {
        return super.fieldsToString() +
                ", enumType=" + enumType +
                ", enumStorageMode='" + enumStorageMode + '\'';
    }
}
