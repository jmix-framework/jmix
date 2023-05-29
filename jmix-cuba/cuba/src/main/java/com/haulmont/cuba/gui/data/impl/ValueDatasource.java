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

package com.haulmont.cuba.gui.data.impl;

import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.MetaClass;

/**
 * Base interface of datasources that work with {@link io.jmix.core.entity.KeyValueEntity}.
 *
 * @deprecated Use {@link io.jmix.ui.model.KeyValueContainer} APIs instead.
 */
@Deprecated
public interface ValueDatasource {

    /**
     * @return meta-class of entities located in the datasource
     */
    MetaClass getMetaClass();

    /**
     * Sets the name of a property that represents the entity id.
     */
    ValueDatasource setIdName(String name);

    /**
     * Adds a string property to the meta-class of this datasource.
     * @return this instance for chaining
     */
    ValueDatasource addProperty(String name);

    /**
     * Adds a property of the given Java class to the meta-class of this datasource.
     * The Java class can be an entity or a datatype.
     * @return this instance for chaining
     */
    ValueDatasource addProperty(String name, Class aClass);

    /**
     * Adds a property of the given datatype to the meta-class of this datasource.
     * @return this instance for chaining
     */
    ValueDatasource addProperty(String name, Datatype datatype);
}
