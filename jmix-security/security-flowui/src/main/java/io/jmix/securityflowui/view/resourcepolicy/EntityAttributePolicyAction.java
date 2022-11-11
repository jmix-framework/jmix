package io.jmix.securityflowui.view.resourcepolicy;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

public enum EntityAttributePolicyAction implements EnumClass<String> {

    VIEW(io.jmix.security.model.EntityAttributePolicyAction.VIEW.getId()),
    MODIFY(io.jmix.security.model.EntityAttributePolicyAction.MODIFY.getId());

    private final String id;

    EntityAttributePolicyAction(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
}
