package io.jmix.quartz.model;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum ScheduleType implements EnumClass<String> {

    CRON_EXPRESSION("cronExpression"),
    SIMPLE("simple");

    private String id;

    ScheduleType(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ScheduleType fromId(String id) {
        for (ScheduleType at : ScheduleType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}