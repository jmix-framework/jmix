package io.jmix.quartz.model;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import jakarta.annotation.Nullable;


public enum JobSource implements EnumClass<String> {

    USER_DEFINED("userDefined"),
    PREDEFINED("predefined");

    private String id;

    JobSource(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static JobSource fromId(String id) {
        for (JobSource at : JobSource.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}