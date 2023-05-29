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

import jakarta.annotation.Nullable;

/**
 * An access context to check permissions on arbitrary named functionality.
 */
public class SpecificOperationAccessContext implements AccessContext {
    protected boolean permitted = true;
    protected final String name;

    public SpecificOperationAccessContext(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDenied() {
        permitted = false;
    }

    public boolean isPermitted() {
        return permitted;
    }

    @Nullable
    @Override
    public String explainConstraints() {
        return !permitted ? name : null;
    }
}
