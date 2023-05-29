package io.jmix.quartz.model;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import org.springframework.lang.Nullable;


public enum JobState implements EnumClass<String> {

    NORMAL("normal"),
    PAUSED("paused");

    private String id;

    JobState(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static JobState fromId(String id) {
        for (JobState at : JobState.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}