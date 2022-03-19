package test_support.entity;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;

public enum CarType implements EnumClass<String> {

    SEDAN("SEDAN"),
    HATCHBACK("HATCHBACK");

    private String id;

    CarType(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static CarType fromId(String id) {
        for (CarType at : CarType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}