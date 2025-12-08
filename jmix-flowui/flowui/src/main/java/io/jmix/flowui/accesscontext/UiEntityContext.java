/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.accesscontext;

import io.jmix.core.accesscontext.AccessContext;
import io.jmix.core.metamodel.model.MetaClass;

import org.springframework.lang.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines an authorization context specific to operations on a particular entity.
 * Provides control over access permissions for create, view, edit, and delete operations
 * on the associated entity.
 */
public class UiEntityContext implements AccessContext {

    protected final MetaClass entityClass;

    protected boolean createPermitted = true;
    protected boolean viewPermitted = true;
    protected boolean editPermitted = true;
    protected boolean deletePermitted = true;

    public UiEntityContext(MetaClass entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Returns the metadata object representing the entity associated with the current context.
     *
     * @return the {@link MetaClass} instance representing the entity
     */
    public MetaClass getEntityClass() {
        return entityClass;
    }

    /**
     * Determines whether the "create" operation is permitted within the current context.
     *
     * @return {@code true} if the "create" operation is allowed, {@code false} otherwise
     */
    public boolean isCreatePermitted() {
        return createPermitted;
    }

    /**
     * Denies the "create" operation within the current context.
     * This method sets the permission state for the "create" operation to false,
     * explicitly restricting the creation of the associated entity.
     */
    public void setCreateDenied() {
        this.createPermitted = false;
    }

    /**
     * Determines whether the "view" operation is permitted for the associated entity
     * within the current context.
     *
     * @return {@code true} if the "view" operation is allowed, {@code false} otherwise
     */
    public boolean isViewPermitted() {
        return viewPermitted;
    }

    /**
     * Denies the "view" operation for the associated entity within the current context.
     * This method sets the permission state for the "view" operation to false,
     * explicitly restricting the ability to view the entity.
     */
    public void setViewDenied() {
        this.viewPermitted = false;
    }

    /**
     * Determines whether the "edit" operation is permitted for the associated entity
     * within the current context.
     *
     * @return {@code true} if the "edit" operation is allowed, {@code false} otherwise
     */
    public boolean isEditPermitted() {
        return editPermitted;
    }

    /**
     * Denies the "edit" operation for the associated entity within the current context.
     * This method sets the permission state for the "edit" operation to false,
     * explicitly restricting the editing of the associated entity.
     */
    public void setEditDenied() {
        this.editPermitted = false;
    }

    /**
     * Determines whether the "delete" operation is permitted within the current context.
     *
     * @return {@code true} if the "delete" operation is allowed, {@code false} otherwise
     */
    public boolean isDeletePermitted() {
        return deletePermitted;
    }

    /**
     * Denies the "delete" operation for the associated entity within the current context.
     * This method sets the permission state for the "delete" operation to false,
     * explicitly restricting the deletion of the associated entity.
     */
    public void setDeleteDenied() {
        this.deletePermitted = false;
    }

    @Nullable
    @Override
    public String explainConstraints() {
        List<String> deniedOperations = new ArrayList<>(4);
        if (!createPermitted)
            deniedOperations.add("create");
        if (!viewPermitted)
            deniedOperations.add("view");
        if (!editPermitted)
            deniedOperations.add("edit");
        if (!deletePermitted)
            deniedOperations.add("delete");
        if (!deniedOperations.isEmpty()) {
            return "entity '" + entityClass.getName() + "' " + String.join(", ", deniedOperations);
        }
        return null;
    }
}
