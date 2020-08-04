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

package io.jmix.ui.context;

import io.jmix.core.context.AccessContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;

public class UiEntityAttributeContext implements AccessContext {
    protected final MetaPropertyPath propertyPath;

    protected boolean viewPermitted = true;
    protected boolean modifyPermitted = true;

    public UiEntityAttributeContext(MetaPropertyPath propertyPath) {
        this.propertyPath = propertyPath;
    }

    public UiEntityAttributeContext(MetaClass metaClass, String attribute) {
        this(metaClass.getPropertyPath(attribute));
    }

    public MetaPropertyPath getPropertyPath() {
        return propertyPath;
    }

    public boolean isModifyPermitted() {
        return modifyPermitted;
    }

    public void setModifyDenied() {
        this.modifyPermitted = false;
    }

    public boolean isViewPermitted() {
        return viewPermitted;
    }

    public void setViewDenied() {
        this.viewPermitted = false;
    }
}
