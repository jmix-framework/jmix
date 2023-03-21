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

package io.jmix.ui.meta;

import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.Screen;

/**
 * Type of UI component property.
 */
public enum PropertyType {
    /**
     * Infer type using parameter of the annotated method.
     */
    AUTO,

    /**
     * Integer type.
     */
    INTEGER,
    /**
     * Long type.
     */
    LONG,
    /**
     * Float type.
     */
    FLOAT,
    /**
     * Double type.
     */
    DOUBLE,
    /**
     * Big decimal type.
     */
    BIG_DECIMAL,
    /**
     * String type.
     */
    STRING,
    /**
     * Boolean type.
     */
    BOOLEAN,
    /**
     * Char type.
     */
    CHARACTER,

    /**
     * Date in standard format: YYYY-MM-DD
     */
    DATE,
    /**
     * Date with time in standard format: YYYY-MM-DD hh:mm:ss
     */
    DATE_TIME,
    /**
     * Time in standard format: hh:mm:ss
     */
    TIME,

    /**
     * Value from a strict list of property options.
     */
    ENUMERATION,

    /**
     * Arbitrary value represented by string with a list of possible options.
     * Similar to {@link PropertyType#ENUMERATION}, but allows text input.
     */
    OPTIONS,

    /**
     * Identifier of a component, action or sub part. Must be a valid Java identifier.
     */
    COMPONENT_ID,

    /**
     * Identifier of a column. Must be a valid Java identifier.
     */
    COLUMN_ID,

    /**
     * Icon path or ID of icon from predefined Jmix icons.
     *
     * @see JmixIcon
     */
    ICON_ID,
    /**
     * Size value, e.g. width or height
     */
    SIZE,
    /**
     * String value or message key with msg:// or mainMsg:// prefix.
     */
    LOCALIZED_STRING,
    /**
     * JPA QL string.
     */
    JPA_QUERY,

    /**
     * Fetch plan name.
     */
    FETCH_PLAN,

    /**
     * Name of Entity meta class.
     */
    ENTITY_NAME,
    /**
     * Names of Entities meta class.
     */
    ENTITY_NAMES,
    /**
     * FQN of Entity class.
     */
    ENTITY_CLASS,
    /**
     * FQN of Enum class.
     */
    ENUM_CLASS,

    /**
     * FQN of Java class.
     */
    JAVA_CLASS_NAME,

    /**
     * CSS classes separated with space symbol.
     */
    CSS_CLASSNAME_LIST,
    /**
     * Inline CSS properties.
     */
    CSS_BLOCK,

    /**
     * Spring Bean ID.
     */
    BEAN_REF,

    /**
     * ID of a component defined in screen.
     */
    COMPONENT_REF,
    /**
     * ID of a datasource.
     */
    DATASOURCE_REF,
    /**
     * ID of a collection datasource.
     */
    COLLECTION_DATASOURCE_REF,
    /**
     * ID of a data loader.
     */
    DATALOADER_REF,
    /**
     * ID of a data container
     */
    DATACONTAINER_REF,
    /**
     * ID of a collection data container.
     */
    COLLECTION_DATACONTAINER_REF,
    /**
     * Name of a data model property.
     */
    PROPERTY_REF,
    /**
     * Entity property path.
     */
    PROPERTY_PATH_REF,
    /**
     * ID of a datatype.
     *
     * @see Datatype
     */
    DATATYPE_ID,

    /**
     * Keyboard shortcut.
     */
    SHORTCUT,

    /**
     * FQN of screen class.
     */
    SCREEN_CLASS_NAME,

    /**
     * Screen ID
     */
    SCREEN_ID,

    /**
     * Standard launch modes of {@link Screen}s.
     */
    SCREEN_OPEN_MODE,

    /**
     * Date format.
     */
    DATE_FORMAT,

    /**
     * Margin info.
     */
    MARGIN,

    /**
     * HTML.
     */
    HTML,

    /**
     * Path to file.
     */
    FILE_REF,

    /**
     * JavaScript Function.
     */
    JS_FUNCTION
}