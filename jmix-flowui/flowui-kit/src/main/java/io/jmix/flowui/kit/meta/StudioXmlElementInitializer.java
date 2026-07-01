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

package io.jmix.flowui.kit.meta;

/**
 * Meta description that describes additional element initialization.
 */
@StudioAPI
public @interface StudioXmlElementInitializer {

    /**
     * Root parent path for child element initializers.
     */
    String ROOT_PARENT_PATH = "";

    /**
     * Descriptions of child elements to set to element tag.
     *
     * @see ChildXmlElementInitializer
     */
    ChildXmlElementInitializer[] childElementInitializers() default {};

    /**
     * Descriptions of attributes to set to element tag.
     *
     * @see AttributeInitializer
     */
    AttributeInitializer[] attributeInitializers() default {};

    /**
     * Just a human-readable preview of the resulting XML element with applied initializers.
     * <p><b>NOTE: </b>This is not used by Studio and is not required to be provided.
     */
    String preview() default "undefined";

    /**
     * Path-based descriptions of child elements to set to element tag.
     */
    @interface ChildXmlElementInitializer {

        /**
         * Unique path of the initializer node relative to the root.
         * <p><b>NOTE: </b>This is not an id attribute or tag name.
         * <p>Path can be any string that uniquely identifies the initializer node among all child element initializers.
         */
        String path();

        /**
         * Parent node path.
         * <p>It is used to group child element initializers by parent nodes and to build hierarchical structure.
         * <p><b>NOTE: </b>This is not an id attribute or tag name. This is link to unique {@link #path()} of parent initializer.
         * <p>Use {@link StudioXmlElementInitializer#ROOT_PARENT_PATH} for direct root children.
         */
        String parentPath() default ROOT_PARENT_PATH;

        /**
         * XML element FQN to initialize.
         */
        String qualifiedName();

        /**
         * List of attribute initializers to set to element tag.
         *
         * @see AttributeInitializer
         */
        AttributeInitializer[] attributeInitializers() default {};
    }

    @interface AttributeInitializer {

        /**
         * XML attribute FQN.
         */
        String qualifiedName();

        /**
         * Value of XML attribute.
         */
        String attributeValue();
    }
}
