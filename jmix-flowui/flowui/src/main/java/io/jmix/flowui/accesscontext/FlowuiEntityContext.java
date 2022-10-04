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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FlowuiEntityContext implements AccessContext {

    protected final MetaClass entityClass;

    protected boolean createPermitted = true;
    protected boolean viewPermitted = true;
    protected boolean editPermitted = true;
    protected boolean deletePermitted = true;

    public FlowuiEntityContext(MetaClass entityClass) {
        this.entityClass = entityClass;
    }

    public MetaClass getEntityClass() {
        return entityClass;
    }

    public boolean isCreatePermitted() {
        return createPermitted;
    }

    public void setCreateDenied() {
        this.createPermitted = false;
    }

    public boolean isViewPermitted() {
        return viewPermitted;
    }

    public void setViewDenied() {
        this.viewPermitted = false;
    }

    public boolean isEditPermitted() {
        return editPermitted;
    }

    public void setEditDenied() {
        this.editPermitted = false;
    }

    public boolean isDeletePermitted() {
        return deletePermitted;
    }

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
