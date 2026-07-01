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

import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.PredefinedTransformation;

import java.lang.annotation.*;

/**
 * Defines an input parameter - parameter passed from the outside when running a defined report
 *   which can be used as conditions in data sets.
 * Additional associated logic can be declared by creating a method annotated with {@link InputParameterDelegate}.
 * <br/>
 * Model object is {@link io.jmix.reports.entity.ReportInputParameter}.
 * @see ReportDef
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RepeatableInputParameterDef.class)
public @interface InputParameterDef {

    /**
     * Unique alias used to access parameter in datasets.
     */
    String alias();

    /**
     * Human-readable parameter name.
     * <br/>
     * Use <code>msg://</code> format if localization is required.
     * Default message group is inferred from the declaration class's package.
     */
    String name() default "";

    /**
     * Whether the parameter is mandatory.
     */
    boolean required() default false;

    /**
     * Data type of the parameter.
     */
    ParameterType type();

    /**
     * Enumeration class for {@link ParameterType#ENUMERATION} parameters
     */
    Class<?> enumerationClass() default void.class;

    /**
     * String representation of the default value that will be used if no other value is selected by the user.
     * Consider using {@link io.jmix.reports.yarg.structure.DefaultValueProvider} delegate instead for complex types.
     */
    String defaultValue() default "";

    /**
     * Whether the current timestamp will be used as the default parameter value
     *   for {@link ParameterType#DATE}, {@link ParameterType#TIME}, {@link ParameterType#DATETIME} types.
     */
    boolean defaultDateIsCurrent() default false;

    /**
     * Additional parameters for {@link ParameterType#ENTITY} and {@link ParameterType#ENTITY_LIST} types.
     */
    EntityParameterDef entity() default @EntityParameterDef();

    /**
     * Whether predefined transformation should be used for {@link ParameterType#TEXT} parameter
     *   (it is useful if parameter is used in a query condition for SQL and JPQL data sets).
     */
    boolean predefinedTransformationEnabled() default false;

    /**
     * Type of the predefined {@link ParameterType#TEXT} transformation.
     */
    PredefinedTransformation predefinedTransformation() default PredefinedTransformation.CONTAINS;

    /**
     * Whether the request for parameter should be hidden from users.
     */
    boolean hidden() default false;
}
