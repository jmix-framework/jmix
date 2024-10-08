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

    /**
     * Property category.
     * @see StudioProperty.Category
     */
    String category() default Category.OTHER;

    boolean required() default false;

    String defaultValue() default "";

    /**
     * Describes an application property or an element whose default property value
     * should be used as the default value for the current property.
     * <p></p>
     * <b>Supported syntax:</b>
     * <ol>
     *  <li>
     *      {@code parent:someParentProperty}
     *      <p>Example: <i>parent:sortable</i></p>
     *  </li>
     *  <li>
     *      {@code application_property:someApplicationProperty}
     *      <p>Example: <i>application_property:jmix.ui.component.default-trim-enabled</i></p>
     *  </li>
     * </ol>
     */
    String defaultValueRef() default "";

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

    /**
     * Use as type for injected field.
     * Xml attribute value must be a valid class fqn.
     */
    boolean useAsInjectionType() default false;

    /**
     * Specifies component tags that will be taken into account
     * when searching for a reference (if {@code type} equals {@link StudioPropertyType#COMPONENT_REF})
     */
    String[] componentRefTags() default {};

    /**
     * Specifies tag that Studio will use
     * when setting a CDATA value (if {@code type} equals {@link StudioPropertyType#CDATA}).
     * <p></p>
     * By default, CDATA value will be set inside component tag (as nested subtag without tag name).
     */
    String cdataWrapperTag() default "";

    interface Category {
        String GENERAL = "General";
        String DATA_BINDING = "Data Binding";
        String SIZE = "Size";
        String POSITION = "Position";
        String LOOK_AND_FEEL = "Look & Feel";
        String VALIDATION = "Validation";
        String OTHER = "Other";
        String ADDITIONAL = "Additional";
    }
}
