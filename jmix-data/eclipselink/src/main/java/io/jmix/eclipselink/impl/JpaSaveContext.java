package io.jmix.eclipselink.impl;

import io.jmix.core.SaveContext;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Stores cascaded entities in addition to standard {@link SaveContext} data
 */
public class JpaSaveContext extends SaveContext {
    private static final long serialVersionUID = -915625536778688868L;

    protected Collection<Object> cascadeAffectedEntities = new LinkedHashSet<>();


    public JpaSaveContext(SaveContext context) {
        this.entitiesToSave = context.getEntitiesToSave();
        this.entitiesToRemove = context.getEntitiesToRemove();

        this.fetchPlans = context.getFetchPlans();
        this.discardSaved = context.isDiscardSaved();
        this.joinTransaction = context.isJoinTransaction();
        this.accessConstraints = context.getAccessConstraints();
        this.hints = context.getHints();
    }

    /**
     * @return entities from {@code entitiesToSave} and {@code entitiesToRemove} collections that has been added
     * because of cascade operations.
     */
    public Collection<Object> getCascadeAffectedEntities() {
        return cascadeAffectedEntities;
    }
}
