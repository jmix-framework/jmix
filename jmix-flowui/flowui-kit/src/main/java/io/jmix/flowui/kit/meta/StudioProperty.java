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

package io.jmix.flowui.kit.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE_PARAMETER)
public @interface StudioProperty {

    String xmlAttribute();

    StudioPropertyType type();

    /**
     * Fully-qualified name of property class
     */
    String classFqn() default "";

    String name() default "";

    boolean required() default false;

    String defaultValue() default "";

    String initialValue() default "";

    String[] options() default {};

    String setMethod() default "";

    String setParameterFqn() default "";

    String addMethod() default "";

    String addParameterFqn() default "";

    String removeMethod() default "";

    String removeParameterFqn() default "";

    /**
     * Specifies name of the type parameter for the component that is provided by the property.<br>
     * The actual class for the type parameter can be resolved for the following property types:
     * {@link StudioPropertyType#ENTITY_CLASS},
     * {@link StudioPropertyType#ENUM_CLASS},
     * {@link StudioPropertyType#COMPONENT_REF},
     * {@link StudioPropertyType#PROPERTY_REF},
     * {@link StudioPropertyType#DATA_CONTAINER_REF},
     * {@link StudioPropertyType#COLLECTION_DATA_CONTAINER_REF}
     *
     * @return name of the type parameter
     */
    String typeParameter() default "";
}
