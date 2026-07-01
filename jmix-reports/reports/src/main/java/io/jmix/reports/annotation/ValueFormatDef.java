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

import java.lang.annotation.*;

/**
 * Defines a value format - element of the report structure.
 * Value formats are used to fine-tune output formatting for any field produced by the report.
 * <br/>
 * This annotation can be used several times on the report class if it has several value formats.
 * Additional associated logic can be declared by creating a method annotated with {@link ValueFormatDelegate}.
 * <br/>
 * Model object is {@link io.jmix.reports.entity.ReportValueFormat}.
 * @see ReportDef
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RepeatableValueFormatDef.class)
public @interface ValueFormatDef {

    /**
     * Name of the report band containing the field.
     */
    String band();

    /**
     * Name of the field to be formatted.
     */
    String field();

    /**
     * Field format.
     * For number values specify the format according to the {@link java.text.DecimalFormat} rules,
     * for dates - {@link java.text.SimpleDateFormat}.
     * Built-in formats for inserting an image, html blocks and others are also available - check the documentation.
     */
    String format() default "";
}
