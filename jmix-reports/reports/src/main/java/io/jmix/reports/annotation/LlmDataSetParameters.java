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
 * Additional parameters for a data set definition of {@link DataSetType#LLM} type. Such a data set states in
 * plain language what the band should contain, and the AI Tools add-on turns that into a JPQL query and
 * executes it.
 * <br/>
 * A report defined in code keeps no generated query — the definition is rebuilt from the annotations at every
 * startup — so the query is generated anew on every report run, always. There is no attribute to switch that
 * off, because there would be nothing to run instead. The price is a model call per run: slower than a query,
 * billable, not reproducible, and capped by the add-on's own row limits. Use the type here for a band whose
 * query genuinely has to follow the run's parameters; a band with a query known in advance belongs in
 * {@link DataSetType#JPQL}.
 * <br/>
 * Model object is {@link io.jmix.reports.entity.DataSet}.
 *
 * @see DataSetDef
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LlmDataSetParameters {

    /**
     * What the data set should return, in plain language. Required for the type.
     * <br/>
     * The description may refer to the report's input parameters by their aliases, to the fields of parent
     * bands as <code>&lt;band&gt;_&lt;field&gt;</code>, and to the columns of the axes of a cross-tab band as
     * <code>&lt;axis&gt;_&lt;field&gt;</code>. Name the columns the template expects: a template prints band
     * fields by the names the generated query gives them.
     */
    String prompt() default "";

    /**
     * Row limit for the query. <code>0</code> leaves it unset, so the add-on's own limit applies; a negative
     * value names no number of rows and is rejected when the report is built.
     * <br/>
     * The add-on caps this by its properties (20 rows by default, 100 at most), so a larger number is silently
     * reduced to that cap.
     */
    int maxResults() default 0;
}
