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

package io.jmix.flowui.facet.impl;

import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.view.View;
import org.springframework.lang.Nullable;

/**
 * An abstract implementation of the {@link Facet} interface, providing
 * base functionality for non-visual components associated with views.
 */
public abstract class AbstractFacet implements Facet {

    protected String id;
    protected View<?> owner;

    @Nullable
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nullable
    @Override
    public View<?> getOwner() {
        return owner;
    }

    @Override
    public void setOwner(@Nullable View<?> owner) {
        this.owner = owner;
    }
}
