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

import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.Orientation;

import java.lang.annotation.*;

/**
 * Defines a band - element of the report data structure.
 * This annotation can be used several times on the report class to define all report bands.
 * <br/>
 * Model object is {@link BandDefinition}.
 * @see ReportDef
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RepeatableBandDef.class)
public @interface BandDef {

    /**
     * Unique band name within the report.
     */
    String name() default "";

    /**
     * Each report must have exactly one root band definition.
     * @return true if this is a root band
     */
    boolean root() default false;

    /**
     * Name of the parent band - which is other band defined in this report.
     */
    String parent() default "";

    /**
     * @return band orientation, applicable to spreadsheet-like output types.
     */
    Orientation orientation() default Orientation.HORIZONTAL;

    /**
     * Renders the band in streaming mode — rows are read one by one from a database cursor, so memory stays
     * bounded regardless of the row count. Applies only to a first-level horizontal band with a single SQL or
     * JPQL dataset and an XLSX/CSV output; the restrictions are enforced by the streaming engine at run time.
     *
     * @return {@code true} to enable streaming for this band
     */
    boolean streaming() default false;

    /**
     * Datasets of the band. May be empty if the band has no data.
     */
    DataSetDef[] dataSets() default {};
}
