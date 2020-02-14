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
package io.jmix.core;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Defines a {@link FetchPlan} property. Each view property corresponds to a
 * {@link io.jmix.core.metamodel.model.MetaProperty} with the same name.
 *
 */
public class FetchPlanProperty implements Serializable {

    private static final long serialVersionUID = 4098678639930287203L;

    private String name;

    private FetchPlan view;

    private FetchMode fetchMode = FetchMode.AUTO;

    public FetchPlanProperty(String name, @Nullable FetchPlan view) {
        this(name, view, FetchMode.AUTO);
    }

    @Deprecated
    public FetchPlanProperty(String name, @Nullable FetchPlan view, boolean lazy) {
        this.name = name;
        this.view = view;
    }

    public FetchPlanProperty(String name, @Nullable FetchPlan view, FetchMode fetchMode) {
        this.name = name;
        this.view = view;
        this.fetchMode = fetchMode;
    }

    /**
     * @return property name that is a metaclass attribute name
     */
    public String getName() {
        return name;
    }

    /**
     * @return fetch plan of the property if the corresponding meta class attribute is a reference
     */
    @Nullable
    public FetchPlan getFetchPlan() {
        return view;
    }

    /**
     * @return fetch mode if the property is a reference
     */
    public FetchMode getFetchMode() {
        return fetchMode;
    }

    @Override
    public String toString() {
        return name;
    }
}
