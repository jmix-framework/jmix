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
import io.jmix.core.metamodel.model.MetaPropertyPath;

import org.springframework.lang.Nullable;

/**
 * An access context to check permissions on entity attributes.
 */
public class EntityAttributeContext implements AccessContext {
    protected final MetaPropertyPath propertyPath;

    protected boolean viewPermitted = true;
    protected boolean modifyPermitted = true;

    public EntityAttributeContext(MetaPropertyPath propertyPath) {
        this.propertyPath = propertyPath;
    }

    public EntityAttributeContext(MetaClass metaClass, String attribute) {
        this(metaClass.getPropertyPath(attribute));
    }

    public MetaPropertyPath getPropertyPath() {
        return propertyPath;
    }

    public boolean canModify() {
        return modifyPermitted;
    }

    public void setModifyDenied() {
        this.modifyPermitted = false;
    }

    public boolean canView() {
        return viewPermitted;
    }

    public void setViewDenied() {
        this.viewPermitted = false;
    }

    @Nullable
    @Override
    public String explainConstraints() {
        if (!viewPermitted || !modifyPermitted) {
            String denied = !modifyPermitted ? "modification" : "";
            if (!viewPermitted) {
                if (!denied.isEmpty())
                    denied += ", ";
                denied += "view";
            }
            return propertyPath.getMetaClass().getName() + "." + propertyPath.toString() + " " + denied;
        }
        return null;
    }
}
