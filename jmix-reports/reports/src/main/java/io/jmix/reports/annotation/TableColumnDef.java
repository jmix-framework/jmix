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

import io.jmix.reports.entity.table.TemplateTableColumn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Part of the table template definition.
 * Represents a column in the resulting table that takes its values from a band data by key.
 * <br/>
 * Model object is {@link TemplateTableColumn}.
 * @see TableBandDef
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableColumnDef {

    /**
     * Name of a dataset property, used to fetch values from the band data.
     */
    String key();

    /**
     * Column caption.
     * <br/>
     * Use <code>msg://</code> format if localization is required.
     * Default message group is inferred from the declaration class's package.
     */
    String caption();
}