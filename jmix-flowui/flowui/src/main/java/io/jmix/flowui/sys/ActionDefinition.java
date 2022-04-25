package io.jmix.flowui.sys;

public final class ActionDefinition {

    private final String id;
    private final String actionClass;

    public ActionDefinition(String id, String actionClass) {
        this.id = id;
        this.actionClass = actionClass;
    }

    public String getId() {
        return id;
    }

    public String getActionClass() {
        return actionClass;
    }

    @Override
    public String toString() {
        return "ActionDefinition{" +
                "id='" + id + '\'' +
                ", actionClass='" + actionClass + '\'' +
                '}';
    }
}