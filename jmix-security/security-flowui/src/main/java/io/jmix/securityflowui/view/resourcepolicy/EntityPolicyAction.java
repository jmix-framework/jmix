package io.jmix.securityflowui.view.resourcepolicy;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

public enum EntityPolicyAction implements EnumClass<String> {

    CREATE(io.jmix.security.model.EntityPolicyAction.CREATE.getId()),
    READ(io.jmix.security.model.EntityPolicyAction.READ.getId()),
    UPDATE(io.jmix.security.model.EntityPolicyAction.UPDATE.getId()),
    DELETE(io.jmix.security.model.EntityPolicyAction.DELETE.getId());

    private final String id;

    EntityPolicyAction(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
}
