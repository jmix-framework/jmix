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

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * An access context to check permissions on entity attributes when serializing entities to/from JSON.
 */
public class ExportImportEntityContext implements AccessContext {
    protected final MetaClass entityClass;
    protected Set<String> notImported;
    protected Set<String> notExported;

    public ExportImportEntityContext(MetaClass entityClass) {
        this.entityClass = entityClass;
    }

    public MetaClass getEntityClass() {
        return entityClass;
    }

    public boolean canImported(String attribute) {
        return notImported == null || !notImported.contains(attribute);
    }

    public boolean canExported(String attribute) {
        return notExported == null || !notExported.contains(attribute);
    }

    public void notImportedAttribute(String name) {
        if (notImported == null) {
            notImported = new HashSet<>();
        }
        notImported.add(name);
    }

    public void notExportedAttribute(String name) {
        if (notExported == null) {
            notExported = new HashSet<>();
        }
        notExported.add(name);
    }

    @Nullable
    @Override
    public String explainConstraints() {
        String message = "";
        if (notExported != null && !notExported.isEmpty()) {
            message += " not exported = " + notExported;
        }
        if (notImported != null && !notImported.isEmpty()) {
            message += " not imported = " + notImported;
        }
        if (!message.isEmpty()) {
            return entityClass.getName() + ": " + message;
        }
        return null;
    }
}
