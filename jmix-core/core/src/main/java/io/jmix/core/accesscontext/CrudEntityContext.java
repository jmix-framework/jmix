/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.accesscontext;

import io.jmix.core.metamodel.model.MetaClass;

import org.springframework.lang.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * An access context to check permissions on entity operations.
 */
public class CrudEntityContext implements AccessContext {

    protected final MetaClass entityClass;
    protected boolean createPermitted = true;
    protected boolean readPermitted = true;
    protected boolean updatePermitted = true;
    protected boolean deletePermitted = true;

    public CrudEntityContext(MetaClass entityClass) {
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

    public boolean isReadPermitted() {
        return readPermitted;
    }

    public void setReadDenied() {
        this.readPermitted = false;
    }

    public boolean isUpdatePermitted() {
        return updatePermitted;
    }

    public void setUpdateDenied() {
        this.updatePermitted = false;
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
        if (!readPermitted)
            deniedOperations.add("read");
        if (!updatePermitted)
            deniedOperations.add("update");
        if (!deletePermitted)
            deniedOperations.add("delete");
        if (!deniedOperations.isEmpty()) {
            return "entity '" + entityClass.getName() + "' " + String.join(", ", deniedOperations);
        }
        return null;
    }
}
