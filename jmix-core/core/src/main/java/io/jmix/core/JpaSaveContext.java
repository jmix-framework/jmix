package io.jmix.core;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Stores cascaded entities in addition to standard {@link SaveContext} data
 */
public class JpaSaveContext extends SaveContext {
    private static final long serialVersionUID = -915625536778688868L;

    protected Collection<Object> cascadeAffectedEntities = new LinkedHashSet<>();


    public JpaSaveContext(SaveContext context) {
        this.entitiesToSave = context.entitiesToSave;
        this.entitiesToRemove = context.entitiesToRemove;

        this.fetchPlans = context.fetchPlans;
        this.softDeletion = context.softDeletion;
        this.discardSaved = context.discardSaved;
        this.joinTransaction = context.joinTransaction;
        this.accessConstraints = context.accessConstraints;
        this.hints = context.hints;
    }

    /**
     * @return entities from {@code entitiesToSave} and {@code entitiesToRemove} collections that has been added
     * because of cascade operations.
     */
    public Collection<Object> getCascadeAffectedEntities() {
        return cascadeAffectedEntities;
    }
}
