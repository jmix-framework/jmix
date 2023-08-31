/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.app.bulk;

import io.jmix.core.metamodel.model.MetaProperty;

import javax.annotation.Nullable;

public class ManagedField {

    protected final String fqn;
    protected final String parentFqn;

    protected final String localizedName;

    protected final MetaProperty metaProperty;

    public ManagedField(String fqn, MetaProperty metaProperty, String localizedName, @Nullable String parentFqn) {
        this.fqn = fqn;
        this.metaProperty = metaProperty;
        this.localizedName = localizedName;
        this.parentFqn = parentFqn;
    }

    public String getFqn() {
        return fqn;
    }

    @Nullable
    public String getParentFqn() {
        return parentFqn;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public MetaProperty getMetaProperty() {
        return metaProperty;
    }
}
