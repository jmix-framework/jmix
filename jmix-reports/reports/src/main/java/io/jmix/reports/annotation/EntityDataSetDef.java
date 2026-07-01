/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reports.annotation;

import io.jmix.reports.entity.DataSetType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Additional parameters for a data set definition of {@link DataSetType#SINGLE} and {@link DataSetType#MULTI} types.
 * The entities are always reloaded from the database. Fetch plan can be provided by the {@link #fetchPlanName()} attribute
 * or by the {@link io.jmix.reports.delegate.FetchPlanProvider} delegate method.
 * <br/>
 * Model object is {@link io.jmix.reports.entity.DataSet}.
 * @see DataSetDef
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityDataSetDef {

    /**
     * Alias of the input parameter (defined with {@link InputParameterDef}) used as data source for this data set.
     */
    String parameterAlias() default "";

    /**
     * Name of the parameter entity's nested collection attribute,
     *   if this collection should be used as data source for a {@link io.jmix.reports.entity.DataSetType#MULTI} data set.
     */
    String nestedCollectionAttribute() default "";

    /**
     * Name of the globally defined fetch plan to reload entity(-ies) from the data store.
     */
    String fetchPlanName() default "";
}
