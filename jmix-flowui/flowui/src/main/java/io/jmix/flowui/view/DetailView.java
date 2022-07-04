package io.jmix.flowui.view;

import io.jmix.core.pessimisticlocking.PessimisticLock;
import io.jmix.flowui.util.OperationResult;

/**
 * Interface for detail view controllers.
 *
 * @param <E> type of entity
 */
public interface DetailView<E> extends ChangeTracker {

    OperationResult commit();

    OperationResult closeWithCommit();

    OperationResult closeWithDiscard();

    /**
     * @return currently edited entity instance
     */
    E getEditedEntity();

    /**
     * Sets entity instance to view.
     *
     * @param entity entity to edit
     */
    void setEntityToEdit(E entity);

    /**
     * @return lock status of currently edited entity instance. Possible variants:
     * <ul>
     *     <li>{@link PessimisticLockStatus#NOT_SUPPORTED} - if the entity does not support pessimistic lock.</li>
     *     <li>{@link PessimisticLockStatus#LOCKED} - if the entity instance is successfully locked.</li>
     *     <li>{@link PessimisticLockStatus#FAILED} - if the entity instance has been locked when the view is
     *         opened.</li>
     * </ul>
     * @see PessimisticLock
     */
    PessimisticLockStatus getPessimisticLockStatus();
}
