/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.ui.model;

import io.jmix.core.EntitySet;
import io.jmix.core.SaveContext;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.JmixEntity;
import io.jmix.ui.screen.InstallSubject;
import io.jmix.ui.screen.Subscribe;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EventObject;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Interface for tracking changes in entities loaded to the client tier.
 * <p>
 * Within {@code DataContext}, an entity with the given identifier is represented by a single object instance, no matter
 * where and how many times it is used in object graphs.
 */
@SuppressWarnings("rawtypes")
@InstallSubject("commitDelegate")
public interface DataContext {

    /**
     * Returns an entity instance by its class and id.
     * @return entity instance or null if there is no such entity in the context
     */
    @Nullable
    <T extends JmixEntity> T find(Class<T> entityClass, Object entityId);

    /**
     * Returns the instance of entity with the same id if it exists in this context.
     * @return entity instance or null if there is no such entity in the context
     */
    @Nullable
    <T extends JmixEntity> T find(T entity);

    /**
     * Returns true if the context contains the given entity (distinguished by its class and id).
     */
    boolean contains(JmixEntity entity);

    /**
     * Merge the given entity into the context. The whole object graph with all references will be merged.
     * <p>
     * If an entity with the same identifier already exists in the context, the passed entity state is copied into
     * it and the existing instance is returned. Otherwise, a copy of the passed instance is registered in the context
     * and returned.
     * <p>
     * If the given instance is new and the the context doesn't contain an instance with the same identifier, the context
     * will save the new instance on {@link #commit()}. Otherwise, even if some attributes of the merged instance are changed
     * as a result of copying the state of the passed instance, the merged instance will not be committed. Such modifications
     * are considered as a result of loading more fresh state from the database.
     * <p>
     * WARNING: use the returned value because it is always a different object instance.
     * The only case when you get the same instance is if the input was previously returned from the same context as a
     * result of {@link #find(Class, Object)} or {@code merge()}.
     *
     * @return the instance which is tracked by the context
     */
    @CheckReturnValue
    <T extends JmixEntity> T merge(T entity);

    /**
     * Merge the given entities into the context. The whole object graph for each element of the collection with all
     * references will be merged.
     * <p>
     * Same as {@link #merge(JmixEntity)} but for a collection of instances.
     *
     * @return set of instances tracked by the context
     * @see #merge(JmixEntity)
     */
    @CheckReturnValue
    EntitySet merge(Collection<? extends JmixEntity> entities);

    /**
     * Removes the entity from the context and registers it as deleted. The entity will be removed from the data store
     * upon subsequent call to {@link #commit()}.
     * <p>
     * If the given entity is not in the context, nothing happens.
     */
    void remove(JmixEntity entity);

    /**
     * Removes the entity from the context so the context stops tracking it.
     * <p>
     * If the given entity is not in the context, nothing happens.
     */
    void evict(JmixEntity entity);

    /**
     * Clears the lists of created/modified/deleted entities and evicts these entities.
     */
    void evictModified();

    /**
     * Evicts all tracked entities.
     *
     * @see #evict(JmixEntity)
     */
    void clear();

    /**
     * Creates an entity instance and merge it into the context.
     * <p>
     * Same as:
     * <pre>
     * Foo foo = dataContext.merge(metadata.create(Foo.class));
     * </pre>
     * @param entityClass entity class
     * @return a new instance which is tracked by the context
     */
    <T extends JmixEntity> T create(Class<T> entityClass);

    /**
     * Returns true if the context has detected changes in the tracked entities.
     */
    boolean hasChanges();

    /**
     * Returns true if the context has detected changes in the given entity.
     */
    boolean isModified(JmixEntity entity);

    /**
     * Registers or unregisters the given entity as modified.
     *
     * @param entity   entity instance which is already merged into the context
     * @param modified true to register or false to unregister
     */
    void setModified(JmixEntity entity, boolean modified);

    /**
     * Returns an immutable set of entities registered as modified.
     */
    Set<JmixEntity> getModified();

    /**
     * Returns true if the context has registered removal of the given entity.
     */
    boolean isRemoved(JmixEntity entity);

    /**
     * Returns an immutable set of entities registered for removal.
     */
    Set<JmixEntity> getRemoved();

    /**
     * Commits changed and removed instances to the middleware. After successful commit, the context contains
     * updated instances returned from the middleware.
     *
     * @see #setParent(DataContext)
     * @return set of committed and merged back to the context instances. Does not contain removed instances.
     */
    EntitySet commit();

    /**
     * Returns a parent context, if any. If the parent context is set, {@link #commit()} method merges the changed instances
     * to it instead of sending to the middleware.
     */
    @Nullable
    DataContext getParent();

    /**
     * Sets the parent context. If the parent context is set, {@link #commit()} method merges the changed instances
     * to it instead of sending to the middleware.
     */
    void setParent(DataContext parentContext);

    /**
     * Event sent when the context detects changes in an entity, a new instance is merged or an entity is removed.
     * <p>
     * In this event listener, you can react to changes of tracked entities, for example:
     * <pre>
     *     &#64;Subscribe(target = Target.DATA_CONTEXT)
     *     protected void onChange(DataContext.ChangeEvent event) {
     *         log.debug("Changed entity: " + event.getEntity());
     *         indicatorLabel.setValue("Changed");
     *     }
     * </pre>
     */
    class ChangeEvent extends EventObject {

        private final JmixEntity entity;

        public ChangeEvent(DataContext dataContext, JmixEntity entity) {
            super(dataContext);
            this.entity = entity;
        }

        /**
         * The data context which sent the event.
         */
        @Override
        public DataContext getSource() {
            return (DataContext) super.getSource();
        }

        /**
         * Returns the changed entity.
         */
        public JmixEntity getEntity() {
            return entity;
        }
    }

    /**
     * Adds a listener to {@link ChangeEvent}.
     */
    Subscription addChangeListener(Consumer<ChangeEvent> listener);

    /**
     * Event sent before committing changes.
     * <p>
     * In this event listener, you can add arbitrary entity instances to the committed collections returned by
     * {@link #getModifiedInstances()} and {@link #getRemovedInstances()} methods, for example:
     * <pre>
     *     &#64;Subscribe(target = Target.DATA_CONTEXT)
     *     protected void onPreCommit(DataContext.PreCommitEvent event) {
     *         event.getModifiedInstances().add(customer);
     *     }
     * </pre>
     *
     * You can also prevent commit using the {@link #preventCommit()} method of the event, for example:
     * <pre>
     *     &#64;Subscribe(target = Target.DATA_CONTEXT)
     *     protected void onPreCommit(DataContext.PreCommitEvent event) {
     *         if (doNotCommit()) {
     *             event.preventCommit();
     *         }
     *     }
     * </pre>
     *
     * @see #addPreCommitListener(Consumer)
     */
    class PreCommitEvent extends EventObject {

        private final Collection<JmixEntity> modifiedInstances;
        private final Collection<JmixEntity> removedInstances;
        private boolean commitPrevented;

        public PreCommitEvent(DataContext dataContext, Collection<JmixEntity> modified, Collection<JmixEntity> removed) {
            super(dataContext);
            this.modifiedInstances = modified;
            this.removedInstances = removed;
        }

        /**
         * The data context which sent the event.
         */
        @Override
        public DataContext getSource() {
            return (DataContext) super.getSource();
        }

        /**
         * Returns the collection of modified instances.
         */
        public Collection<JmixEntity> getModifiedInstances() {
            return modifiedInstances;
        }

        /**
         * Returns the collection of removed instances.
         */
        public Collection<JmixEntity> getRemovedInstances() {
            return removedInstances;
        }

        /**
         * Invoke this method if you want to abort the commit.
         */
        public void preventCommit() {
            commitPrevented = true;
        }

        /**
         * Returns true if {@link #preventCommit()} method was called and commit will be aborted.
         */
        public boolean isCommitPrevented() {
            return commitPrevented;
        }
    }

    /**
     * Adds a listener to {@link PreCommitEvent}.
     * <p>
     * You can also add an event listener declaratively using a controller method annotated with {@link Subscribe}:
     * <pre>
     *    &#64;Subscribe(target = Target.DATA_CONTEXT)
     *    protected void onPreCommit(DataContext.PreCommitEvent event) {
     *       // handle event here
     *    }
     * </pre>
     *
     * @param listener listener
     * @return subscription
     */
    Subscription addPreCommitListener(Consumer<PreCommitEvent> listener);

    /**
     * Event sent after committing changes.
     * <p>
     * In this event listener, you can get the collection of committed entities returned from the middle tier, for example:
     * <pre>
     *     &#64;Subscribe(target = Target.DATA_CONTEXT)
     *     protected void onPostCommit(DataContext.PostCommitEvent event) {
     *         log.debug("Committed: " + event.getCommittedInstances());
     *     }
     * </pre>
     *
     * @see #addPostCommitListener(Consumer)
     */
    class PostCommitEvent extends EventObject {

        private final Collection<JmixEntity> committedInstances;

        public PostCommitEvent(DataContext dataContext, Collection<JmixEntity> committedInstances) {
            super(dataContext);
            this.committedInstances = committedInstances;
        }

        /**
         * The data context which sent the event.
         */
        @Override
        public DataContext getSource() {
            return (DataContext) super.getSource();
        }

        /**
         * Returns the collection of committed entities.
         */
        public EntitySet getCommittedInstances() {
            return EntitySet.of(committedInstances);
        }
    }

    /**
     * Adds a listener to {@link PostCommitEvent}.
     * <p>
     * You can also add an event listener declaratively using a controller method annotated with {@link Subscribe}:
     * <pre>
     *    &#64;Subscribe(target = Target.DATA_CONTEXT)
     *    protected void onPostCommit(DataContext.PostCommitEvent event) {
     *       // handle event here
     *    }
     * </pre>
     *
     * @param listener listener
     * @return subscription
     */
    Subscription addPostCommitListener(Consumer<PostCommitEvent> listener);

    /**
     * Returns a function which will be used to commit data instead of standard implementation.
     */
    @Nullable
    Function<SaveContext, Set<JmixEntity>> getCommitDelegate();

    /**
     * Sets a function which will be used to commit data instead of standard implementation.
     */
    void setCommitDelegate(Function<SaveContext, Set<JmixEntity>> delegate);
}
